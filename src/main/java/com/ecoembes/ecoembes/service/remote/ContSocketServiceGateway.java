package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    public Double getPlantCapacity(Plant plant) throws Exception {
        try (
            Socket socket = socketFactory.createSocket(plant.getHost(), plant.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            out.println("GET_CAPACITY " + plant.getPlantId());
            String response = in.readLine();
            if (response != null && !response.startsWith("ERROR")) {
                return Double.parseDouble(response);
            }
        }
        return null;
    }
}
