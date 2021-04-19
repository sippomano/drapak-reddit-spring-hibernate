package com.sipp.drapakredditspringhibernate.repo;

import com.sipp.drapakredditspringhibernate.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    @Override
    List<Post> findAll();
    List<Post> findBySubreddit(String subreddit);
    List<Post> findByCreationTimeGreaterThan(long creationTimeFrom);
    List<Post> findByCreationTimeGreaterThanAndCreationTimeLessThanEqual(long creationTimeFrom, long creationTimeUntil);
}
