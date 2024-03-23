import java.io.*;
import java.net.*;
import java.util.*;

public class OxygenClient {
    private static void appendToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("o_log.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int M = Integer.parseInt(args[0]);
        String clientType, response, timestamp = "";

        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("o");
            clientType = in.readLine();
            System.out.println("Received: " + clientType);

            for (int i = 1; i <= M; i++) {
                timestamp = new Date().toString();
                String request = "(O" + i + ", request, " + timestamp + ")";
                out.println(request);
                timestamp = new Date().toString();
                String logMessage = "(O" + i + ", request, " + timestamp + ")";
                System.out.println("Sent: " + logMessage);
                appendToLogFile("Sent: " + logMessage);
            }

            while ((response = in.readLine()) != null) {
                System.out.println("Received: " + response);
                appendToLogFile("Received: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
