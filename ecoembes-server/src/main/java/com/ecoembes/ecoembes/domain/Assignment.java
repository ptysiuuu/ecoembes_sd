package com.ecoembes.ecoembes.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dumpster_id", nullable = false)
    private Dumpster dumpster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int assignedContainers;

    protected Assignment() {}

    public Assignment(Plant plant, Dumpster dumpster, Employee employee, LocalDate assignmentDate, int assignedContainers) {
        this.plant = plant;
        this.dumpster = dumpster;
        this.employee = employee;
        this.assignmentDate = assignmentDate;
        this.assignedContainers = assignedContainers;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Dumpster getDumpster() {
        return dumpster;
    }

    public void setDumpster(Dumpster dumpster) {
        this.dumpster = dumpster;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAssignedContainers() {
        return assignedContainers;
    }

    public void setAssignedContainers(int assignedContainers) {
        this.assignedContainers = assignedContainers;
    }
}
