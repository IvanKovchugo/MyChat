package server;


import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement preparedStatementGetNickname;
    private static PreparedStatement preparedStatementRegistration;


    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chatdatabase.db");
            AllStatements();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void AllStatements() throws SQLException {
        preparedStatementGetNickname = connection.prepareStatement("SELECT nickname FROM chatUsers WHERE login = ? AND password = ?;");
        preparedStatementRegistration = connection.prepareStatement("INSERT INTO chatUsers(login, password, nickname) VALUES (? ,? ,? );");

    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            preparedStatementGetNickname.setString(1, login);
            preparedStatementGetNickname.setString(2, password);
            ResultSet rs = preparedStatementGetNickname.executeQuery();
            if (rs.next()) {
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            preparedStatementRegistration.setString(1, login);
            preparedStatementRegistration.setString(2, password);
            preparedStatementRegistration.setString(3, nickname);
            preparedStatementRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            preparedStatementRegistration.close();
            preparedStatementGetNickname.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}