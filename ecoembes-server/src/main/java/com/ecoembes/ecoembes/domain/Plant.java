package com.ecoembes.ecoembes.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plants")
public class Plant {

    @Id
    private String plantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double availableCapacity;

    @Column(nullable = false)
    private String type;

    @Column(nullable = true)
    private String host;

    @Column(nullable = true)
    private int port;

    @Column(nullable = true)
    private String gatewayType;

    @Column(nullable = false)
    private int totalContainersReceived;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    protected Plant() {}

    public Plant(String plantId, String name, Double availableCapacity, String type, String gatewayType) {
        this.plantId = plantId;
        this.name = name;
        this.availableCapacity = availableCapacity;
        this.type = type;
        this.gatewayType = gatewayType;
        this.totalContainersReceived = 0;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Double availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public int getTotalContainersReceived() {
        return totalContainersReceived;
    }

    public void setTotalContainersReceived(int totalContainersReceived) {
        this.totalContainersReceived = totalContainersReceived;
    }

    public void addContainers(int containers) {
        this.totalContainersReceived += containers;
    }
}
