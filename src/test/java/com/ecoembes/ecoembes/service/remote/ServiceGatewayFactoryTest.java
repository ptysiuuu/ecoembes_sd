package com.ecoembes.ecoembes.service.remote;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceGatewayFactoryTest {

    @Test
    void getServiceGateway() {
        ApplicationContext context = mock(ApplicationContext.class);
        ServiceGateway plasSBGateway = mock(PlasSBServiceGateway.class);
        ServiceGateway contSocketGateway = mock(ContSocketServiceGateway.class);

        when(context.getBean("PlasSB", ServiceGateway.class)).thenReturn(plasSBGateway);
        when(context.getBean("ContSocket", ServiceGateway.class)).thenReturn(contSocketGateway);

        ServiceGatewayFactory factory = new ServiceGatewayFactory(context);

        assertEquals(plasSBGateway, factory.getServiceGateway("PlasSB"));
        assertEquals(contSocketGateway, factory.getServiceGateway("ContSocket"));
    }
}
