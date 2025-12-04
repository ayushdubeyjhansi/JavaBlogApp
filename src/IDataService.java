import java.util.List;

public interface IDataService {
    boolean authenticateUser(String username, String password);
    void addPost(BlogPost post);
    List<BlogPost> getAllPosts();
}