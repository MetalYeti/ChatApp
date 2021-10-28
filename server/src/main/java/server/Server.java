package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private final Logger logger;

    private List<ClientHandler> clients;
    private AuthService authService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public Server() {
        logger = Logger.getLogger(this.getClass().getName());
        clients = new CopyOnWriteArrayList<>();
        //authService = new SimpleAuthService();
        authService = new DBAuthService();
        try {
            server = new ServerSocket(PORT);
            //System.out.println("Server started!");
            logger.info("Server started!");

            while (true) {
                socket = server.accept();
                //System.out.println("Client connected");
                logger.info("Client connected");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            logger.warning(Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                server.close();
                executorService.shutdown();
            } catch (IOException e) {
                logger.warning(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public void systemMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public boolean isSignedIn(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist");

        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String message = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void sendWhisper(ClientHandler sender, String recipientNick, String msg) {
        ClientHandler recipient = getClientByNickname(recipientNick);

        if (recipient != null) {
            if (recipient == sender) {
                sender.sendMsg("Разговариваем сами с собой?");
            } else {

                String messageFrom = String.format("Личное сообщение для %s: %s", recipientNick, msg);
                String messageTo = String.format("Личное сообщение от %s: %s", sender.getNickname(), msg);

                sender.sendMsg(messageFrom);
                recipient.sendMsg(messageTo);
            }
        } else {
            sender.sendMsg(String.format("Пользователя %s нет в чате", recipientNick));
        }
    }

    private ClientHandler getClientByNickname(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nick)) {
                return c;
            }
        }
        return null;
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
