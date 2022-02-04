package server;

import java.sql.*;

public class DatabaseSql implements AuthServise {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement preparedStatement;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return SQLHandler.getNicknameByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return SQLHandler.registration(login, password, nickname);
    }
}
