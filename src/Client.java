import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <type> <count>");
            return;
        }

        String type = args[0].toLowerCase();
        int count = Integer.parseInt(args[1]);

        String clientType, response, timestamp = "";

        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(type);
            clientType = in.readLine();
            System.out.println("Received: " + clientType);

            for (int i = 1; i <= count; i++) {
                timestamp = new Date().toString();
                String request = "(" + type.toUpperCase().charAt(0) + i + ", request, " + timestamp + ")";
                out.println(request);
                timestamp = new Date().toString();
                String logMessage = "(" + type.toUpperCase().charAt(0) + i + ", request, " + timestamp + ")";
                System.out.println("Sent: " + logMessage);
            }

            while ((response = in.readLine()) != null) {
                System.out.println("Received: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}