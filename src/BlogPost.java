import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPost {
    String title;
    String content;
    User author;
    int likes;
    List<String> comments; // Collections
    Date timestamp;

    public BlogPost(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.likes = 0;
        this.comments = new ArrayList<>();
        this.timestamp = new Date();
    }
}