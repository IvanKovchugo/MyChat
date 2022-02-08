package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthyService implements AuthServise {
    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthyService() {
        this.users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(new UserData("user" + i, "pass" + 0, "nick" + i));
        }
        users.add(new UserData("user1", "123", "nick1"));
        users.add(new UserData("user2", "321", "nick2"));
        users.add(new UserData("user3", "213", "nick3"));
        users.add(new UserData("user4", "231", "nick4"));
        users.add(new UserData("user5", "312", "nick5"));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData user : users) {
            if (user.login.equals(login) || user.nickname.equals(nickname)) {
                return false;
            }
        }
        users.add(new UserData(login, password, nickname));
        return true;
        }
    }