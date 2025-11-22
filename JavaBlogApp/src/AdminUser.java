public class AdminUser extends User {
    public AdminUser(String username) {
        super(username, "ADMIN");
    }

    @Override
    public String getWelcomeMessage() {
        return "Administrator Access: " + username;
    }
}