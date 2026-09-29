package chat.client;

import ChatApp.ChatException;
import ChatApp.ChatMessage;
import ChatApp.ChatRoomPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.Scanner;

public class ClientMain {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args);
             Scanner scanner = new Scanner(System.in)) {

            // La identidad y el puerto deben coincidir con ServerMain.
            String proxyString = "ChatService:default -h 127.0.0.1 -p 10000";
            ObjectPrx base = communicator.stringToProxy(proxyString);
            ChatRoomPrx chat = ChatRoomPrx.checkedCast(base);

            if (chat == null) {
                System.err.println("No se encontró el servicio ChatService.");
                return;
            }

            String nickname;

            System.out.println("=== CHAT DISTRIBUIDO CON ZEROC ICE ===");
            while (true) {
                System.out.print("Ingresa tu nickname: ");
                if (!scanner.hasNextLine()) {
                    return;
                }

                nickname = scanner.nextLine().trim();
                if (nickname.isEmpty()) {
                    System.out.println("El nickname no puede estar vacío.");
                    continue;
                }

                try {
                    chat.login(nickname);
                    break;
                } catch (ChatException e) {
                    System.out.println("Rechazado: " + e.reason);
                }
            }

            final String activeUser = nickname;
            System.out.println("Conectado como " + activeUser);
            System.out.println("Comandos: /users para ver usuarios, /exit para salir.");

            Thread listener = new Thread(() -> {
                long lastReceivedId = 0;

                while (running) {
                    try {
                        ChatMessage[] messages =
                                chat.getPendingMessages(activeUser, lastReceivedId);

                        for (ChatMessage msg : messages) {
                            if (msg.id > lastReceivedId) {
                                lastReceivedId = msg.id;
                            }

                            if (!activeUser.equals(msg.sender)) {
                                System.out.println(
                                        "\n[" + msg.timestamp + "] <"
                                                + msg.sender + "> " + msg.text
                                );
                                System.out.print("> ");
                            }
                        }

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        if (running) {
                            System.err.println(
                                    "\nSe perdió la comunicación con el servidor: "
                                            + e.getClass().getSimpleName()
                            );
                            running = false;
                        }
                        break;
                    }
                }
            }, "chat-listener");

            listener.setDaemon(true);
            listener.start();

            try {
                while (running && scanner.hasNextLine()) {
                    System.out.print("> ");
                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("/exit")) {
                        break;
                    }

                    if (input.equalsIgnoreCase("/users")) {
                        String[] users = chat.getOnlineUsers();
                        System.out.println(
                                "Usuarios activos (" + users.length + "): "
                                        + String.join(", ", users)
                        );
                    } else if (!input.isEmpty()) {
                        chat.postMessage(activeUser, input);
                    }
                }
            } catch (ChatException e) {
                System.err.println("El servidor rechazó la operación: " + e.reason);
            } catch (Exception e) {
                System.err.println(
                        "Error de comunicación: " + e.getClass().getSimpleName()
                                + " - " + e.getMessage()
                );
            } finally {
                running = false;
                try {
                    chat.logout(activeUser);
                } catch (Exception ignored) {
                    // El servidor puede haberse desconectado.
                }
                System.out.println("Sesión terminada.");
            }

        } catch (Exception e) {
            System.err.println(
                    "No se pudo iniciar el cliente: "
                            + e.getClass().getSimpleName() + " - " + e.getMessage()
            );
        }
    }
}