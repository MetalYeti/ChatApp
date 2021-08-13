package server;

public interface AuthService {
    /**
     * Метод проверки наличия учетки
     *
     * @param login    логин, не должен содержать пробелов
     * @param password пароль, не должен содержать пробелов
     * @return nickname если учетка существует, null если учетки нет
     */
    String getNicknameByLoginAndPassword(String login, String password);

    /**
     * Метод для попытки регистрации новой учетной записи
     *
     * @return true если регистрация прошла успешно
     * false если логин или никнейм уже заняты
     */
    boolean registration(String login, String password, String nickname);
}
