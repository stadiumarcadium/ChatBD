package Chat.authentication;

import java.sql.*;

public class DBAuthenticationService implements AuthenticationService {

    public static final String SQLITE_SRC = "jdbc:sqlite:src/main/resources/db/mainDB.db";
    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        String passwordDB = null;
        String username = null;

        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM auth WHERE login = ?");
            pstmt.setString(1, login);
            rs = pstmt.executeQuery();
            if (rs.isClosed()) {
                return null;
            }

            username = rs.getString("username");
            passwordDB = rs.getString("password");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ((passwordDB != null) && (passwordDB.equals(password))) ? username : null;
    }

    @Override
    public void createUser(String login, String password, String username) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO auth (login, password, username) VALUES (?, ?, ?)");

            pstmt.setString(1, login);
            pstmt.setString(2, password);
            pstmt.setString(3, username);

            pstmt.addBatch();

            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUsername(String login, String newUsername) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("UPDATE auth SET username = ? WHERE login = ?");
            pstmt.setString(1, newUsername);
            pstmt.setString(2, login);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Boolean checkLoginByFree(String login) {
        String username = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM auth WHERE login = ?");
            pstmt.setString(1, login);
            rs = pstmt.executeQuery();
            if (rs.isClosed()) {
                return true;
            }

            username = rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return username == null;
    }

    @Override
    public void startAuthentication() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_SRC);
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endAuthentication() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLoginByUsername(String username) {
        String login = null;

        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM auth WHERE username = ?");
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.isClosed()) {
                return null;
            }
            login = rs.getString("login");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return login;

    }
}
