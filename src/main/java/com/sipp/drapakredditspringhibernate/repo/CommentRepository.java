package com.sipp.drapakredditspringhibernate.repo;

import com.sipp.drapakredditspringhibernate.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    @Override
    List<Comment> findAll();
    @Query("from Comment")
    List<Comment> getAllComments();
    @Query("from Comment where parentPermalink=:parentId")
    List<Comment> getAllCommentsForPost(@Param("parentId") String parentPermalink);
    @Query("from Comment where subreddit=:subreddit")
    List<Comment> getAllCommentsFromSubreddit(@Param("subreddit") String subreddit);
    @Query("from Comment where creationTime>=:from")
    List<Comment> getAllCommentsFromTime(@Param("from") long from);
    @Query("from Comment where creationTime>=:from and creationTime<:until")
    List<Comment> getAllCommentsFromTimeUntil(@Param("from") long from, @Param("until") long until);
}
