public class RegularUser extends User {
    public RegularUser(String username) {
        super(username, "USER");
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome back, writer " + username;
    }
}