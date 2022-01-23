package server;

public interface AuthServise {
    /**
     * Получение никнейма по логину и паролю
     * возвращает никнейм если учетка есть
     * null если пары логин пароль не нашлось
     * */
    String getNicknameByLoginAndPassword(String login, String password);
    /**
     * Регистрация нового пользователя
     * при успешной регистрации ( логин и никнейм не заняты ) вернет true
     * иначе вернет false
     * */
    boolean registration(String login, String password, String nickname);
}
