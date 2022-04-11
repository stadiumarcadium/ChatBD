package Chat.authentication;

import Chat.models.User;

import java.util.List;

public class BaseAuthenticationService implements AuthenticationService {

    private static final List<User> clients = List.of(
            new User("user1", "1111", "Тимофей"),
            new User("user2", "2222", "Дмитрий"),
            new User("user3", "3333", "Диана"),
            new User("user4", "4444", "Арман")
    );

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) && client.getPassword().equals(password)) {
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public void createUser(String login, String password, String username) {

    }

    @Override
    public void updateUsername(String login, String newUsername) {

    }

    @Override
    public Boolean checkLoginByFree(String login) {
        return null;
    }

    @Override
    public void startAuthentication() {
        System.out.println("Старт аутентификации");

    }

    @Override
    public void endAuthentication() {
        System.out.println("Конец аутентификации");

    }

    @Override
    public String getLoginByUsername(String username) {
        return null;
    }

}
