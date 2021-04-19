package com.sipp.drapakredditspringhibernate.controller;

import com.sipp.drapakredditspringhibernate.model.Post;
import com.sipp.drapakredditspringhibernate.repo.CommentRepository;
import com.sipp.drapakredditspringhibernate.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostRepository postRepository;

    @Autowired
    PostController (PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<Post>> getAllPosts() {
        return new ResponseEntity<>(postRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/single/{permalink}")
    public ResponseEntity<Post> getPost(@PathVariable("permalink") String permalink) {
        return new ResponseEntity<>(postRepository.getOne(permalink), HttpStatus.OK);
    }

    @GetMapping(value = "/subreddit/{subreddit}")
    public ResponseEntity<List<Post>> getPostsSubreddit(@PathVariable("subreddit") String subreddit) {
        return new ResponseEntity<>(postRepository.findBySubreddit(subreddit), HttpStatus.OK);
    }

    @GetMapping(value = "/creationtime/{from}")
    public ResponseEntity<List<Post>> getPostsFromTime(@PathVariable("from") long from) {
        return new ResponseEntity<>(postRepository.findByCreationTimeGreaterThan(from), HttpStatus.OK);
    }

    @GetMapping(value = "/creationtime/{from}/{to}")
    public ResponseEntity<List<Post>> getPostsFromTimeUntil(@PathVariable("from") long from, @PathVariable("to") long to) {
        return new ResponseEntity<>(postRepository.findByCreationTimeGreaterThanAndCreationTimeLessThanEqual(from, to), HttpStatus.OK);
    }
}
