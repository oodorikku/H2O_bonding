import java.io.*;
import java.net.*;
import java.util.*;

public class OxygenClient {
    private static Date firstBondTime;
    private static Date lastBondTime;
    private static boolean firstBondTimeRecorded = false;

    private static synchronized void appendToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("o_log.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        String clientType, timestamp = "";

        //SIGINT hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //Check if a bond has occurred yet (failsafe)
                if (!firstBondTimeRecorded) {
                    System.out.println("No bonds have been received yet.");
                    return;
                }
                //Print first and last bond time to console
                System.out.println("First bond time: " + firstBondTime.toString());
                System.out.println("Last bond time: " + lastBondTime.toString());
            }
        });
        
        try (Socket socket = new Socket(args[1], 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("o");
            clientType = in.readLine();
            System.out.println("Received: " + clientType);

            // Creating a new thread to listen to the socket
            Thread listenerThread = new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    
                    String response;
                    while ((response = in.readLine()) != null) {
                        lastBondTime = new Date();
                        if (!firstBondTimeRecorded) {
                            firstBondTime = lastBondTime;
                            firstBondTimeRecorded = true;
                        }

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
                String request = "(O" + i + ", request, " + timestamp + ")";
                out.println(request);
                timestamp = new Date().toString();
                String logMessage = "(O" + i + ", request, " + timestamp + ")";
                System.out.println("Sent: " + logMessage);
                appendToLogFile("Sent: " + logMessage);
            }

            listenerThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        

        
    }
}
