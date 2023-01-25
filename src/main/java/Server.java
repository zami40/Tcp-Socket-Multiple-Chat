import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        System.out.println("Server started");
//        System.out.println(socket);
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {

        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
//                throw new RuntimeException(e);
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter a nick name: ");
                nickname = in.readLine();
                System.out.println(nickname + "connected to chat!");
                broadcast(nickname + " Joined the chat!");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/N")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves");
                            System.out.println(nickname + " renamed themselves");
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname " + nickname);
                        } else {
                            System.out.println("Nick names is not set yet");
                        }
                        // TODO: handle nickname
                    } else if (message.startsWith("/quit")) {
//                        TODO: quit
                        broadcast(nickname + " Left the chat");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }
        public static void main(String[] args) {
            Server server = new Server();
            server.run();
        }
    }

