package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started!");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
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
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
