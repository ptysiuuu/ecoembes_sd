package com.ecoembes.fass.contsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ContSocketServer {

    private static final Map<String, Double> plantCapacities = new HashMap<>();

    public static void main(String[] args) throws IOException {
        plantCapacities.put("CONTSO-01", 75.0);
        plantCapacities.put("CONTSO-02", 90.0);

        int portNumber = 4444;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("ContSocketServer listening on port " + portNumber);
            while (true) {
                new ContSocketThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    private static class ContSocketThread extends Thread {
        private Socket socket = null;

        public ContSocketThread(Socket socket) {
            super("ContSocketThread");
            this.socket = socket;
        }

        public void run() {
            try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] tokens = inputLine.split(" ");
                    if (tokens.length >= 1 && tokens[0].equals("GET_CAPACITY")) {
                        // Each server manages one plant, no plantId needed
                        // Optional date parameter: GET_CAPACITY [date]
                        // For now, return default capacity (could be extended to handle date)
                        out.println(plantCapacities.values().stream().findFirst().orElse(0.0));
                    } else if (tokens.length >= 3 && tokens[0].equals("NOTIFY")) {
                        // Format: NOTIFY <numDumpsters> <totalContainers> <date>
                        // Each server manages one plant, no plantId needed
                        String numDumpsters = tokens[1];
                        String totalContainers = tokens[2];
                        String date = tokens.length > 3 ? tokens[3] : "unknown";

                        System.out.println("Notification received");
                        System.out.println("Incoming dumpsters: " + numDumpsters);
                        System.out.println("Total containers: " + totalContainers);
                        System.out.println("Expected arrival: " + date);
                        out.println("OK");
                    } else {
                        out.println("ERROR: Invalid command");
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
