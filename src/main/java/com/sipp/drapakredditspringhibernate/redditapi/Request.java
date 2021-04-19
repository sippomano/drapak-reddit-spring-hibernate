package com.sipp.drapakredditspringhibernate.redditapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sipp.drapakredditspringhibernate.model.Comment;
import com.sipp.drapakredditspringhibernate.model.Post;
import com.sipp.drapakredditspringhibernate.repo.CommentRepository;
import com.sipp.drapakredditspringhibernate.repo.PostRepository;
import com.sipp.drapakredditspringhibernate.redditapi.authentication.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Slf4j
@Component
public class Request {

    private final Token token;
    private final WebClient client;
    private final PostRepository postRepository;
    private final ExecutorService refreshThreadExecutor;

    private final Set <String> postCategories;
    private final String[] subreddits;
    private final String postPrefix;
    private final String limitParamName;
    private final String limitParamValue;

    @Autowired
    public Request(Token token, WebClient.Builder webClientBuilder, PostRepository postRepository, @Value("${reddit.request.url.base}") String url,
                   @Value("${reddit.header.useragent.name}") String userAgentHeaderName, @Value("${reddit.header.useragent.value}") String userAgentHeaderValue,
                   @Value("${reddit.request.categories}") String postCategories, @Value("${reddit.request.subreddits}") String subreddits, @Value("${reddit.request.url.prefix}") String postPrefix,
                   @Value("${reddit.request.param.limit.name}") String limitParamName, @Value("${reddit.request.param.limit.value}") String limitParamValue) {
        log.info("in Request constructor");
        this.token = token;
        this.client = webClientBuilder.baseUrl(url)
                .defaultHeader(userAgentHeaderName, userAgentHeaderValue)
                .build();
        this.postRepository = postRepository;
        this.refreshThreadExecutor = Executors.newSingleThreadExecutor();
        this.postCategories = Arrays.stream(postCategories.split(","))
                .map(s -> "/" + s)
                .collect(Collectors.toSet());
        this.subreddits = subreddits.split(",");
        this.postPrefix = postPrefix;
        this.limitParamName = limitParamName;
        this.limitParamValue = limitParamValue;
    }

    @PostConstruct
    private void init() {
        refreshThreadExecutor.submit(() -> {
            while (true) {
                refreshPostAndCommentData();
            }
        });
    }

    private Set<Comment> getCommentsForPost(String permalink) throws IOException {
        log.info("in getCommentsForPost");
        String response = client
                .get()
                .uri(permalink)
                .header(HttpHeaders.AUTHORIZATION, token.getTokenHeaderValue())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info(response);
        Set<Comment> comments = Parser.parseCommentTree(response);
        log.info("Adding" + comments.size() + " comments for post: " + permalink);
        return comments;
    }

    private Set<Post> getPostsForAllCategories(String subreddit) throws IOException, InterruptedException{
        Set<Post> allPosts = new HashSet<>();
        for (String postCategory : postCategories) {
            String response = client
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(postPrefix + subreddit + postCategory)
                            .queryParam(limitParamName, limitParamValue)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, token.getTokenHeaderValue())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Set<Post> categoryPosts = Parser.parsePostSet(response);
            log.info("Adding posts for category: " + postCategory + " for subreddit: " + subreddit + ". Number of posts: " + categoryPosts.size());
            allPosts.addAll(categoryPosts);
            Thread.sleep(1050);
        }
        return allPosts;
    }

    public void refreshPostAndCommentData() throws IOException, InterruptedException{
        Set<Post> posts = new HashSet<>();
        for (String subreddit : subreddits) {
            posts.addAll(getPostsForAllCategories(subreddit));
        }
        for (Post post : posts) {
            post.setComments(getCommentsForPost(post.getPermalink()));
            Thread.sleep(1050);
        }
        postRepository.saveAll(posts);
    }

    @PreDestroy
    private void destroy() {
        refreshThreadExecutor.shutdownNow();
    }

    @Component
    private static class Parser {
        public static Set<Post> parsePostSet(String json) throws JsonProcessingException {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            Set<Post> postSet = new HashSet<>();
            JsonNode posts = root.get("data").get("children");
            for (JsonNode current : posts) {
                current = current.get("data");
                Post post = new Post();
                post.setAuthor(stripDoubleQuotes(current.get("author").toString()
                        .equals("\"[deleted]\"") ? current.get("author").toString() : current.get("author_fullname").toString()));
                post.setCommentsCount(current.get("num_comments").asInt());
                post.setPermalink(stripDoubleQuotes(current.get("permalink").toString()));
                post.setAwardsCount(current.get("all_awardings").size());
                post.setSubreddit(stripDoubleQuotes(current.get("subreddit").toString()));
                post.setText(stripDoubleQuotes(current.get("selftext").toString()));
                post.setTitle(stripDoubleQuotes(current.get("title").toString()));
                post.setScore(current.get("score").asInt());
                post.setCreationTime(current.get("created").asLong());
                post.setFlair(stripDoubleQuotes(current.get("link_flair_text").toString()));

                log.info(post.toString());
                postSet.add(post);
            }
            return postSet;
        }

        private static Set<Comment> parseCommentTree(String json) throws JsonProcessingException{
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode root = (ArrayNode) mapper.readTree(json);
            String postPermalink = stripDoubleQuotes(root.get(0).get("data").get("children").get(0).get("data").get("permalink").toString());
            Set<Comment> comments = new HashSet<>();
            Iterator<JsonNode> topCommentsIterator = root.get(1).get("data").get("children").iterator();
            JsonNode current;
            while ((topCommentsIterator.hasNext())) {
                current = topCommentsIterator.next();
                if (!current.get("kind").toString().equals("\"more\"")) {
                    parseCommentHelper(current.get("data"), null, comments);
                }
            }
            comments.forEach(c -> c.setPostPermalink(postPermalink));
            log.info("Comments list size: " + comments.size() + " for: " + postPermalink);
            return comments;
        }

        private static void parseCommentHelper(JsonNode current, Comment parent, Set<Comment> comments) {
            Comment comment = new Comment();
            log.info("current: " + current.toString());
            boolean deletedFlag = (current.get("body").toString().equals("\"[deleted]\"") || (current.get("author").toString().equals("\"[deleted]\"")));
            if (!deletedFlag) {

                comment.setAuthor(stripDoubleQuotes(current.get("author_fullname").toString()));
                comment.setText(stripDoubleQuotes(current.get("body").toString()));
                comment.setPermalink(stripDoubleQuotes(current.get("permalink").toString()));
                comment.setSubreddit(stripDoubleQuotes(current.get("subreddit").toString()));
                comment.setAwardsCount(current.get("all_awardings").size());
                comment.setScore(current.get("score").asInt());
                comment.setCreationTime(current.get("created").asLong());
                if (parent != null) {
                    comment.setParentPermalink(parent.getPermalink());
                }
                comments.add(comment);

            }
            if (!current.get("replies").isEmpty()) {
                JsonNode replies = current.get("replies").get("data").get("children");

                for (JsonNode reply : replies) {
                    if (reply.get("kind").toString().equals("\"more\"")) {
                        break;
                    } else if (!deletedFlag) {
                        parseCommentHelper(reply.get("data"), comment, comments);
                    } else {
                        parseCommentHelper(reply.get("data"), null, comments);
                    }
                }
            }
        }
        private static String stripDoubleQuotes(String text) {
            return text.substring(1, text.length()-1);
        }
    }
}
