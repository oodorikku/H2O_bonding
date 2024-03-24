import java.io.*;
import java.net.*;
import java.util.*;

public class HydrogenClient {
    private static Date firstBondTime;
    private static Date lastBondTime;
    private static boolean firstBondTimeRecorded = false;

    private static void appendToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("h_log.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        String clientType, response, timestamp = "";
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

        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("h");
            clientType = in.readLine();
            System.out.println("Received: " + clientType);

            for (int i = 1; i <= N; i++) {
                timestamp = new Date().toString();
                String request = "(H" + i + ", request, " + timestamp + ")";
                out.println(request);
                System.out.println("Sent: " + request );
                appendToLogFile("Sent: " + request );
            }

            while ((response = in.readLine()) != null) {
                if (!firstBondTimeRecorded) {
                    firstBondTime = new Date();
                    firstBondTimeRecorded = true;
                }
                lastBondTime = new Date();
                System.out.println("Received: " + response);
                appendToLogFile("Received: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
