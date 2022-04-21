package Chat.authentication;

public interface AuthenticationService {
    String getUsernameByLoginAndPassword(String login, String password);

    void createUser(String login, String password, String username);

    void updateUsername(String login, String newUsername);

    Boolean checkLoginByFree(String login);

    void startAuthentication();

    void endAuthentication();

    String getLoginByUsername(String username);
}

