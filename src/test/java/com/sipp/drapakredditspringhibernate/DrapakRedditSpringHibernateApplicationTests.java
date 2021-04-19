package com.sipp.drapakredditspringhibernate;

import com.sipp.drapakredditspringhibernate.model.Comment;
import com.sipp.drapakredditspringhibernate.model.Post;
import com.sipp.drapakredditspringhibernate.repo.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
class DrapakRedditSpringHibernateApplicationTests {



    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    @Rollback(false)
    void saveAllPosts(@Autowired PostRepository postRepository) {
        Post post = new Post();
        post.setComments(new HashSet<>());
        post.setAuthor("Name");
        post.setAwardsCount(3);
        post.setCommentsCount(5);
        post.setFlair("discussion");
        post.setTitle("The title");
        post.setText("Important message");
        post.setScore(33);
        post.setSubreddit("stocks");
        post.setPermalink("/r/permalink");
        Set<Post> posts = new HashSet<>();
        posts.add(post);
        postRepository.saveAll(posts);
    }

    @Test
    @Transactional
    @Rollback(false)
    void saveAllPostsWithComments(@Autowired PostRepository postRepository) {
        Post post = new Post();
        post.setComments(new HashSet<>());
        post.setAuthor("Name");
        post.setAwardsCount(3);
        post.setCommentsCount(5);
        post.setFlair("discussion");
        post.setTitle("The titlle");
        post.setText("Important message");
        post.setScore(33);
        post.setCreationTime(4345456);
        post.setSubreddit("stocks");
        post.setPermalink("/r/permalinkk");

        Comment comment = new Comment();
        comment.setAuthor("le man");
        comment.setAwardsCount(2);
        comment.setCreationTime(4345454);
        comment.setPermalink("/r/permalinkkkk");
        comment.setPostPermalink("/r/permalinkk");
        comment.setScore(45);
        comment.setText("gagagag");

        post.getComments().add(comment);

        Set<Post> posts = new HashSet<>();
        posts.add(post);
        postRepository.saveAll(posts);
    }

}
