import java.io.*;
import java.net.*;
import java.util.*;

public class HydrogenClient {
    private static synchronized void appendToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("h_log.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        String clientType, timestamp = "";

        

        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("h");

            // Creating a new thread to listen to the socket
        Thread listenerThread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                 
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Received: " + response);
                    appendToLogFile("Received: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        listenerThread.start();
        

            for (int i = 1; i <= N; i++) {
                timestamp = new Date().toString();
                String request = "(H" + i + ", request, " + timestamp + ")";
                out.println(request);
                timestamp = new Date().toString();
                String logMessage = "(H" + i + ", request, " + timestamp + ")";
                System.out.println("Sent: " + logMessage);
                appendToLogFile("Sent: " + logMessage);
            }

            listenerThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 

        
        
    }
}
