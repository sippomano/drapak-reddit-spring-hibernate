package com.sipp.drapakredditspringhibernate.model;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "reddit_post")
public class Post {

    private String author;
    private String text;
    private String subreddit;
    @Id
    @EqualsAndHashCode.Include
    private String permalink;
    private String title;
    private String flair;
    private int commentsCount;
    private int awardsCount;
    private int score;
    private long creationTime;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "postPermalink", referencedColumnName = "permalink", nullable = false, insertable = false, updatable = false)
    private Set<Comment> comments;
}
