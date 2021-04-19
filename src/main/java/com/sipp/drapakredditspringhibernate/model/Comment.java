package com.sipp.drapakredditspringhibernate.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "reddit_comment")
public class Comment {

    private String author;
    private String text;
    @EqualsAndHashCode.Include
    @Id
    private String permalink;
    private String subreddit;
    private String postPermalink;
    private int awardsCount;
    private int score;
    private long creationTime;
    private String parentPermalink;
}
