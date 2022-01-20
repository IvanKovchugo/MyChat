package server;

import javax.imageio.IIOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // authy
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            sendMassage("/end");
                            break;
                        }
                        if (str.startsWith("/auth")) {
                            String[] token = str.split(" ", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                authenticated = true;
                                nickname = newNick;
                                sendMassage("/authok " + nickname);
                                server.subscribe(this);
                                System.out.println("Client: " + nickname + " authenticated");
                                break;
                            } else {
                                sendMassage("Неверный логин/пароль");
                            }
                        }
                    }
                    // work
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMassage("/end");
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] privatToken = str.split(" ", 3);
                                if (privatToken.length < 3) {
                                    continue;
                                }
                                server.privateMessage(this, privatToken[1], privatToken[2]);
                            }
                        } else {
                            server.broadcastMessage(this, str);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    server.removeClient(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        } catch (IIOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMassage(String massage) {
        try {
            out.writeUTF(massage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
