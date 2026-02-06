package main.java.com.example.db_test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// --- REPOSITORY ---
interface BlogRepository extends JpaRepository<BlogPost, Long> {
    // Spring generates the SQL: SELECT * FROM blog_posts WHERE author_name = ?
    List<BlogPost> findByAuthorName(String authorName);
}

// --- CONTROLLER ---
@RestController
public class BlogController {

    private final BlogRepository repository;

    public BlogController(BlogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/search")
    public List<BlogPost> search(@RequestParam String author) {
        long start = System.currentTimeMillis();
        
        // This is the query we are stress testing
        List<BlogPost> results = repository.findByAuthorName(author);
        
        long end = System.currentTimeMillis();
        System.out.println("Found " + results.size() + " posts in " + (end - start) + "ms");
        
        return results;
    }
}