package chat.server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class ServerMain {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter =
                    communicator.createObjectAdapterWithEndpoints(
                            "ChatAdapter", "default -p 10000"
                    );

            ChatRoomI servant = new ChatRoomI();
            adapter.add(servant, Util.stringToIdentity("ChatService"));

            adapter.activate();

            System.out.println("SERVIDOR ZEROC ICE INICIADO");
            System.out.println("Puerto TCP: 10000");
            System.out.println("Identidad: ChatService");
            System.out.println("Esperando clientes...");

            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("[ERROR SERVIDOR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}