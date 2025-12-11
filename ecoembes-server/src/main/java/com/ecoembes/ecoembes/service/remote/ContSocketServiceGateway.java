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
        // Plant object already resolved by factory, contains all connection details
        // Gateway selected based on plant's gatewayType, so plantId is used from the object
        try (
            Socket socket = socketFactory.createSocket(plant.getHost(), plant.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String formattedDate = date != null ? date.format(DateTimeFormatter.ISO_DATE) : "";
            out.println("GET_CAPACITY " + plant.getPlantId() + (formattedDate.isEmpty() ? "" : " " + formattedDate));
            String response = in.readLine();
            if (response != null && !response.startsWith("ERROR")) {
                return Double.parseDouble(response);
            }
        }
        return null;
    }

    @Override
    public void notifyIncomingDumpsters(Plant plant, java.util.List<String> dumpsterIds, Integer totalContainers, LocalDate arrivalDate) throws Exception {
        try (
            Socket socket = socketFactory.createSocket(plant.getHost(), plant.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String formattedDate = arrivalDate != null ? arrivalDate.format(DateTimeFormatter.ISO_DATE) : "";
            // Format: NOTIFY <plantId> <numDumpsters> <totalContainers> <date>
            out.println("NOTIFY " + plant.getPlantId() + " " + dumpsterIds.size() + " " + totalContainers + " " + formattedDate);
            String response = in.readLine();
            if (response != null && response.startsWith("ERROR")) {
                throw new Exception("Error notifying plant: " + response);
            }
        }
    }
}
