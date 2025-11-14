package com.ecoembes.ecoembes.service.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceGatewayFactory {

    private final ApplicationContext context;

    @Autowired
    public ServiceGatewayFactory(ApplicationContext context) {
        this.context = context;
    }

    public ServiceGateway getServiceGateway(String type) {
        return context.getBean(type, ServiceGateway.class);
    }
}
