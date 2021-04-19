CREATE TABLE public.reddit_comment
(
    author text COLLATE pg_catalog."default",
    text text COLLATE pg_catalog."default",
    permalink text COLLATE pg_catalog."default" NOT NULL,
    subreddit text COLLATE pg_catalog."default" NOT NULL,
    post_permalink text COLLATE pg_catalog."default" NOT NULL,
    awards_count numeric NOT NULL,
    score numeric NOT NULL,
    creation_time bigint NOT NULL,
    parent_permalink text COLLATE pg_catalog."default",
    CONSTRAINT reddit_comment_pkey PRIMARY KEY (permalink)
)