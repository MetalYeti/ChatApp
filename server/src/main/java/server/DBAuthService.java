package server;

import java.sql.*;
import java.util.logging.Logger;

public class DBAuthService implements AuthService {
    private Connection connection;
    private PreparedStatement getNicknamePS;
    private PreparedStatement registerPS;
    private PreparedStatement updateNickPS;
    private final Logger logger;

    public DBAuthService() {
        logger = Logger.getLogger(this.getClass().getName());
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server.db");
            //System.out.println("Соединились с БД!");
            logger.info("Соединились с БД!");
            getNicknamePS = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ? LIMIT 1");
            registerPS = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES (?, ?, ?)");
            updateNickPS = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ?");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            getNicknamePS.setString(1, login);
            getNicknamePS.setString(2, password);
            ResultSet rs = getNicknamePS.executeQuery();

            String nickname = rs.getString("nickname");
            rs.close();

            return nickname;

        } catch (SQLException e) {
            if(!e.getMessage().equals("ResultSet closed")) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            registerPS.setString(1, login);
            registerPS.setString(2, password);
            registerPS.setString(3, nickname);
            int inserted = registerPS.executeUpdate();

            if (inserted > 0) return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean changeNickname(String login, String nickname) {
        try {
            updateNickPS.setString(1, nickname);
            updateNickPS.setString(2, login);

            int updated = updateNickPS.executeUpdate();

            if (updated > 0) return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
