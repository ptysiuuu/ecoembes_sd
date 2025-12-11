package com.ecoembes.ecoembes.service.remote;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServiceGatewayFactory {

    private final Map<String, ServiceGateway> gateways;

    public ServiceGatewayFactory(Map<String, ServiceGateway> gateways) {
        this.gateways = gateways;
    }

    public ServiceGateway getServiceGateway(String gatewayType) {
        ServiceGateway serviceGateway = gateways.get(gatewayType);
        if (serviceGateway == null) {
            throw new IllegalArgumentException("No service gateway found for type: " + gatewayType);
        }
        return serviceGateway;
    }
}
