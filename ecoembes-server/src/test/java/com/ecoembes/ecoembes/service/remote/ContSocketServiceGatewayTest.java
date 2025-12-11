package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ContSocketServiceGatewayTest {

    @Test
    void getPlantCapacity() throws Exception {
        Plant plant = new Plant("CONTSO-01", "ContSocket Ltd.", 80.5, "GENERAL", "ContSocket");
        plant.setHost("localhost");
        plant.setPort(4444);

        SocketFactory socketFactory = Mockito.mock(SocketFactory.class);
        Socket socket = Mockito.mock(Socket.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("75.0".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socketFactory.createSocket("localhost", 4444)).thenReturn(socket);
        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        ContSocketServiceGateway gateway = new ContSocketServiceGateway(socketFactory);
        LocalDate date = LocalDate.of(2025, 11, 5);
        Double capacity = gateway.getPlantCapacity(plant, date);

        assertEquals(75.0, capacity);
        // PlantId is not sent to server - each server manages only one plant
        assertEquals("GET_CAPACITY 2025-11-05" + System.lineSeparator(), outputStream.toString());
    }
}
