package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    Socket socket;
    Server server;
    DataInputStream in;
    DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            sendMsg("/end");
                            System.out.println("Client disconnected");
                            break;
                        }
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s+");
                            nickname = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (nickname != null) {
                                if (!server.isSignedIn(login)) {
                                    sendMsg("/authok " + nickname);
                                    server.subscribe(this);
                                    authenticated = true;
                                    break;
                                } else {
                                    sendMsg("Пользователь уже вошел");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split("\\s+");
                            if (token.length < 4) {
                                continue;
                            }

                            boolean regOk = server.getAuthService().
                                    registration(token[1], token[2], token[3]);
                            if (regOk) {
                                sendMsg("/regok");
                            } else {
                                sendMsg("/regno");
                            }
                        }
                    }
                    socket.setSoTimeout(0);
                    // цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                System.out.println("Client disconnected");
                                break;
                            }

                            if (str.startsWith("/w ")) {
                                String[] tokens = str.split("\\s+", 3);
                                if (tokens.length < 3) {
                                    continue;
                                }
                                server.sendWhisper(this, tokens[1], tokens[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMsg("/end");
                    System.out.println("Client disconnected due timeout");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }

    public String getNickname() {
        return nickname;
    }
}
