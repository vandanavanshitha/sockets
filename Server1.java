import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server1 {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static ServerGUI serverGUI;

    public static void main(String[] args) {
        final int PORT = 12345;
        serverGUI = new ServerGUI();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverGUI.log("Server is running and listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                serverGUI.log("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        serverGUI.log("Broadcasting message: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String username = getUsername();
                serverGUI.log("User " + username + " connected.");

                out.println("Welcome to the chat, " + username + "!");
                out.println("Type Your Message");
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    serverGUI.log("[" + username + "]: " + inputLine);
                    broadcast("[" + username + "]: " + inputLine);
                }

                clients.remove(this);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getUsername() throws IOException {
            out.println("Enter your username:");
            return in.readLine();
        }

        public void sendMessage(String message) {
            out.println(message);
            out.println("Type Your Message");
        }
    }
}
