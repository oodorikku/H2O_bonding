import java.io.*;
import java.net.*;
import java.util.*;

public class HydrogenClient {
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        String clientType, response, timestamp = "";
        File file = new File("oxygenSent.txt");
        File file2 = new File("oxygenReceived.txt");
        
        if(file.exists()){file.delete();}
        if(file2.exists()){file2.delete();}

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
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("hydrogenSent.txt", true))) {
                    writer.write(logMessage);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while ((response = in.readLine()) != null) {
                System.out.println("Received: " + response);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("hydrogenReceived.txt", true))) {
                    writer.write(response);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
