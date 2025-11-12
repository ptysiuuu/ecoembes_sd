package com.ecoembes.ecoembes.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dumpsters")
public class Dumpster {

    @Id
    private String dumpsterId;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private Double capacity;

    @Column(nullable = false)
    private String fillLevel;

    @Column(nullable = false)
    private Integer containersNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dumpster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Usage> usageHistory = new ArrayList<>();

    @OneToMany(mappedBy = "dumpster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    protected Dumpster() {}

    public Dumpster(String dumpsterId, String location, String postalCode, Double capacity) {
        this.dumpsterId = dumpsterId;
        this.location = location;
        this.postalCode = postalCode;
        this.capacity = capacity;
        this.fillLevel = "green";
        this.containersNumber = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(String fillLevel, Integer containersNumber) {
        this.fillLevel = fillLevel;
        this.containersNumber = containersNumber;
    }

    public String getDumpsterId() {
        return dumpsterId;
    }

    public void setDumpsterId(String dumpsterId) {
        this.dumpsterId = dumpsterId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public String getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(String fillLevel) {
        this.fillLevel = fillLevel;
    }

    public Integer getContainersNumber() {
        return containersNumber;
    }

    public void setContainersNumber(Integer containersNumber) {
        this.containersNumber = containersNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Usage> getUsageHistory() {
        return usageHistory;
    }

    public void setUsageHistory(List<Usage> usageHistory) {
        this.usageHistory = usageHistory;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}

