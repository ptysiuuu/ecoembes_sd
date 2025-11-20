package com.ecoembes.ecoembes.service.remote;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ServiceGatewayFactoryTest {

    @Test
    void getServiceGatewayReturnsCorrectImplementation() {
        ServiceGateway plasSBGateway = mock(PlasSBServiceGateway.class);
        ServiceGateway contSocketGateway = mock(ContSocketServiceGateway.class);

        ServiceGatewayFactory factory = new ServiceGatewayFactory(Map.of(
                "PlasSB", plasSBGateway,
                "ContSocket", contSocketGateway
        ));

        assertEquals(plasSBGateway, factory.getServiceGateway("PlasSB"));
        assertEquals(contSocketGateway, factory.getServiceGateway("ContSocket"));
    }

    @Test
    void getServiceGatewayThrowsWhenTypeUnknown() {
        ServiceGatewayFactory factory = new ServiceGatewayFactory(Map.of());

        assertThrows(IllegalArgumentException.class, () -> factory.getServiceGateway("Unknown"));
    }
}
