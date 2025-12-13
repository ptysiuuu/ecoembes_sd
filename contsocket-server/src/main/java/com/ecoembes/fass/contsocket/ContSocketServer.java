package com.ecoembes.fass.contsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ContSocketServer {

    private static final String PLANT_ID = "CONTSO-01";
    private static final Double BASE_CAPACITY = 80.5; // tons

    // Track assigned containers per date
    private static final Map<LocalDate, Integer> assignedContainersByDate = new HashMap<>();

    public static void main(String[] args) throws IOException {
        int portNumber = 9090;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("ContSocketServer (" + PLANT_ID + ") listening on port " + portNumber);
            System.out.println("Base capacity: " + BASE_CAPACITY + " tons");
            while (true) {
                new ContSocketThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    private static synchronized double getAvailableCapacity(LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        int assignedContainers = assignedContainersByDate.getOrDefault(effectiveDate, 0);
        // Assume 1000 containers = 1 ton (adjust ratio as needed)
        double usedCapacity = assignedContainers / 1000.0;
        double availableCapacity = BASE_CAPACITY - usedCapacity;
        return Math.max(0.0, availableCapacity); // Never return negative capacity
    }

    private static synchronized void addIncomingDumpsters(int totalContainers, LocalDate arrivalDate) {
        LocalDate date = arrivalDate != null ? arrivalDate : LocalDate.now();
        int currentAssigned = assignedContainersByDate.getOrDefault(date, 0);
        assignedContainersByDate.put(date, currentAssigned + totalContainers);
        System.out.println("Added " + totalContainers + " containers for date " + date);
        System.out.println("Total assigned for " + date + ": " + assignedContainersByDate.get(date));
        System.out.println("Available capacity for " + date + ": " + getAvailableCapacity(date) + " tons");
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
                        LocalDate date = null;
                        if (tokens.length > 1) {
                            try {
                                date = LocalDate.parse(tokens[1], DateTimeFormatter.ISO_DATE);
                            } catch (Exception e) {
                                System.err.println("Invalid date format: " + tokens[1]);
                            }
                        }
                        double capacity = getAvailableCapacity(date);
                        out.println(capacity);
                    } else if (tokens.length >= 3 && tokens[0].equals("NOTIFY")) {
                        // Format: NOTIFY <numDumpsters> <totalContainers> <date>
                        // Each server manages one plant, no plantId needed
                        try {
                            String numDumpsters = tokens[1];
                            int totalContainers = Integer.parseInt(tokens[2]);
                            LocalDate date = null;
                            if (tokens.length > 3) {
                                try {
                                    date = LocalDate.parse(tokens[3], DateTimeFormatter.ISO_DATE);
                                } catch (Exception e) {
                                    System.err.println("Invalid date format: " + tokens[3]);
                                    date = LocalDate.now();
                                }
                            }

                            System.out.println("Notification received");
                            System.out.println("Incoming dumpsters: " + numDumpsters);
                            System.out.println("Total containers: " + totalContainers);
                            System.out.println("Expected arrival: " + (date != null ? date : "today"));

                            addIncomingDumpsters(totalContainers, date);

                            out.println("OK");
                        } catch (NumberFormatException e) {
                            out.println("ERROR: Invalid number format");
                        }
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
