import java.io.*;
import java.net.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Server {
    private static final int PORT = 12345;
    private static List<String> hydrogenRequests = new ArrayList<>();
    private static List<String> oxygenRequests = new ArrayList<>();
    private static List<PrintWriter> hydrogenClients = new ArrayList<>();
    private static List<PrintWriter> oxygenClients = new ArrayList<>();
    private static int bondIndex = 0;

    public static void main(String[] args) {
        PrintWriter out;
        BufferedReader in;
        String clientType = "";
        Socket socket;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running!");
            while (true) {
                socket = serverSocket.accept();
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                clientType = in.readLine();
                if (clientType.equals("h")) {
                    hydrogenClients.add(out);
                } else if (clientType.equals("o")) {
                    oxygenClients.add(out);
                }
                new Thread(new ClientHandler(socket, clientType)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized boolean tryBond() {
        String hydrogen1, hydrogen2, oxygen, timeStamp, logMessage = "";

        if (hydrogenRequests.size() >= 2 && oxygenRequests.size() >= 1) {
            hydrogen1 = hydrogenRequests.remove(0).split(", ")[0].substring(1);
            hydrogen2 = hydrogenRequests.remove(0).split(", ")[0].substring(1);
            oxygen = oxygenRequests.remove(0).split(", ")[0].substring(1);
            System.out.println("Bonded " + (++bondIndex) + ": " + hydrogen1 + ", " + hydrogen2 + ", " + oxygen);

			Date currTime = new Date();
			long timeAsInt = currTime.getTime();
            timeStamp = currTime.toString();
            logMessage = "(" + hydrogen1 + ", bonded, " + timeStamp + ")";
            sendToClients(hydrogenClients, logMessage);
            System.out.println("Sent: " + logMessage);

            logMessage = "(" + hydrogen2 + ", bonded, " + timeStamp + ")";
            sendToClients(hydrogenClients, logMessage);
            System.out.println("Sent: " + logMessage);

            logMessage = "(" + oxygen + ", bonded, " + timeStamp + ")";
            sendToClients(oxygenClients, logMessage);
            System.out.println("Sent: " + logMessage);

            // Store the data to a file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("bonding_log.txt", true))) {
                writer.write(hydrogen1 + " " + hydrogen2 + " " + oxygen + " " + timeAsInt + " " + logMessage);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }
    
    private static synchronized void sendToClients(List<PrintWriter> clients, String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String clientType;

        public ClientHandler(Socket socket, String clientType) {
            this.socket = socket;
            this.clientType = clientType;
        }

        @Override
        public void run() {
            String request, id, action = "";

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                if (clientType.equals("h")) {
                    out.println("h");
                } else if (clientType.equals("o")) {
                    out.println("o");
                }

                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(", ");
                    id = parts[0].substring(1);
                    action = parts[1];
                    if (parts.length == 3) {
                        synchronized(Server.class) {
                            if (action.equals("request")) {
                                if (id.startsWith("H")) {
                                    hydrogenRequests.add(request);
                                } else if (id.startsWith("O")) {
                                    oxygenRequests.add(request);
                                }
                            }
                        }
                        System.out.println("Received: " + request);

                        tryBond();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
