import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService implements IDataService {
    // --- MYSQL CONFIGURATION ---
    private static final String URL = "jdbc:mysql://localhost:3306/blog_db";
    private static final String USER = "root";
    private static final String PASS = "password"; // <--- MAKE SURE THIS IS YOUR MYSQL ROOT PASSWORD

    private Connection connection;
    private boolean isConnected = false;

    public DatabaseService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
            isConnected = true;
            createTablesIfNotExist();
            System.out.println("SUCCESS: Connected to MySQL Database.");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL JDBC Driver not found.");
            isConnected = false;
        } catch (SQLException e) {
            System.err.println("WARNING: Connection Failed. " + e.getMessage());
            isConnected = false;
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        if (!isConnected) return;
        try (Statement stmt = connection.createStatement()) {
            // 1. Create Posts Table
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255), " +
                    "content TEXT, " +
                    "author VARCHAR(50), " +
                    "likes INT)");

            // 2. Create Users Table (Auto-create if you forgot the SQL step)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(50))");

            // 3. Auto-insert Admin if not exists (Safety check)
            stmt.execute("INSERT IGNORE INTO users (username, password) VALUES ('admin', 'Ayushdada123')");
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        if (isConnected) {
            // SECURE LOGIC: Check Database for matching Name AND Password
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                // If rs.next() is true, it means we found a user with that password
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // Memory Mode: Allow generic login if DB is down
            return !username.isEmpty();
        }
    }

    @Override
    public void addPost(BlogPost post) {
        if (isConnected) {
            String sql = "INSERT INTO posts (title, content, author, likes) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, post.title);
                pstmt.setString(2, post.content);
                pstmt.setString(3, post.author.getUsername());
                pstmt.setInt(4, post.likes);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            MemoryStore.posts.add(post);
        }
    }

    @Override
    public List<BlogPost> getAllPosts() {
        List<BlogPost> posts = new ArrayList<>();
        if (isConnected) {
            String sql = "SELECT * FROM posts ORDER BY id DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String authorName = rs.getString("author");
                    User u = new RegularUser(authorName);

                    BlogPost p = new BlogPost(
                            rs.getString("title"),
                            rs.getString("content"),
                            u
                    );
                    p.likes = rs.getInt("likes");
                    posts.add(p);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            posts.addAll(MemoryStore.posts);
        }
        return posts;
    }
}