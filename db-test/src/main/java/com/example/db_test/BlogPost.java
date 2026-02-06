package main.java.com.example.db_test;

import jakarta.persistence.*;

@Entity
@Table(name = "blog_posts")
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // This is the field we will search (and fail) on later
    private String authorName;

    @Lob // Large Object (Store a lot of text)
    private String content;

    // Standard Constructors
    public BlogPost() {}

    public BlogPost(String title, String authorName, String content) {
        this.title = title;
        this.authorName = authorName;
        this.content = content;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
}