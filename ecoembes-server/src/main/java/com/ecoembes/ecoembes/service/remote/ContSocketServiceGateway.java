package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service("ContSocket")
public class ContSocketServiceGateway implements ServiceGateway {

    private SocketFactory socketFactory;

    public ContSocketServiceGateway() {
        this.socketFactory = new DefaultSocketFactory();
    }

    public ContSocketServiceGateway(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    public Double getPlantCapacity(Plant plant, LocalDate date) throws Exception {
        // Plant ID is used only to select correct gateway via factory
        // Each plant server manages only one plant, so no ID in socket command
        try (
            Socket socket = socketFactory.createSocket(plant.getHost(), plant.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String formattedDate = date != null ? date.format(DateTimeFormatter.ISO_DATE) : "";
            out.println("GET_CAPACITY" + (formattedDate.isEmpty() ? "" : " " + formattedDate));
            String response = in.readLine();
            if (response != null && !response.startsWith("ERROR")) {
                return Double.parseDouble(response);
            }
        }
        return null;
    }

    @Override
    public void notifyIncomingDumpsters(Plant plant, java.util.List<String> dumpsterIds, Integer totalContainers, LocalDate arrivalDate) throws Exception {
        // Plant ID is used only to select correct gateway via factory
        // Each plant server manages only one plant, so no ID in socket command
        try (
            Socket socket = socketFactory.createSocket(plant.getHost(), plant.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String formattedDate = arrivalDate != null ? arrivalDate.format(DateTimeFormatter.ISO_DATE) : "";
            // Format: NOTIFY <numDumpsters> <totalContainers> <date>
            out.println("NOTIFY " + dumpsterIds.size() + " " + totalContainers + " " + formattedDate);
            String response = in.readLine();
            if (response != null && response.startsWith("ERROR")) {
                throw new Exception("Error notifying plant: " + response);
            }
        }
    }
}
