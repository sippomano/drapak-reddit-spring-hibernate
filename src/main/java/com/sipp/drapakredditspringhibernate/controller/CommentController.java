package com.sipp.drapakredditspringhibernate.controller;

import com.sipp.drapakredditspringhibernate.model.Comment;
import com.sipp.drapakredditspringhibernate.repo.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    CommentRepository commentRepository;

    @Autowired
    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<Comment>> getAllComments() {
        return new ResponseEntity<>(commentRepository.getAllComments(), HttpStatus.OK);
    }

    @GetMapping(value = "/single/{permalink}")
    public ResponseEntity<Comment> getComment(@PathVariable("permalink") String permalink) {
        return new ResponseEntity<>(commentRepository.getOne(permalink), HttpStatus.OK);
    }

    @GetMapping(value = "/subreddit/{subreddit}")
    public ResponseEntity<List<Comment>> getCommentsForSubreddit(@PathVariable("subreddit") String subreddit) {
        return new ResponseEntity<>(commentRepository.getAllCommentsFromSubreddit(subreddit), HttpStatus.OK);
    }

    @GetMapping(value = "/creationtime/{from}")
    public ResponseEntity<List<Comment>> getCommentsFromTime(@PathVariable("from") long from) {
        return new ResponseEntity<>(commentRepository.getAllCommentsFromTime(from), HttpStatus.OK);
    }

    @GetMapping(value = "/creationtime/{from}/{to}")
    public ResponseEntity<List<Comment>> getCommentsFromTimeUntil(@PathVariable("from") long from, @PathVariable("to") long until) {
        return new ResponseEntity<>(commentRepository.getAllCommentsFromTimeUntil(from, until), HttpStatus.OK);
    }
}
