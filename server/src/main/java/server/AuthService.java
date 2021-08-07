package server;

public interface AuthService {
    /**
     * Метод проверки наличия учетки
     * @param login логин, не должен содержать пробелов
     * @param password пароль, не должен содержать пробелов
     * @return nickname если учетка существует, null если учетки нет
     * */
    String getNicknameByLoginAndPassword(String login, String password);
}
