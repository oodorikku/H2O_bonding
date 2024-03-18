import java.io.*;
import java.net.*;
import java.util.*;

public class HydrogenClient {
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        String clientType, response, timestamp = "";

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
                timestamp = new Date().toString();
                String logMessage = "(H" + i + ", request, " + timestamp + ")";
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
