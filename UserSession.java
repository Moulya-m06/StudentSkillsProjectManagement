public class UserSession {
    public final String userId;
    public final String username;
    public final String role;

    public UserSession(String userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
