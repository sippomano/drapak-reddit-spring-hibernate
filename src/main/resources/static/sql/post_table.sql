CREATE TABLE public.reddit_post
(
    author text COLLATE pg_catalog."default" NOT NULL,
    text text COLLATE pg_catalog."default",
    subreddit text COLLATE pg_catalog."default" NOT NULL,
    permalink text COLLATE pg_catalog."default" NOT NULL,
    title text COLLATE pg_catalog."default" NOT NULL,
    flair text COLLATE pg_catalog."default" NOT NULL,
    comments_count numeric NOT NULL,
    awards_count numeric NOT NULL,
    score numeric NOT NULL,
    creation_time bigint NOT NULL,
    CONSTRAINT reddit_post_pkey PRIMARY KEY (permalink)
)
