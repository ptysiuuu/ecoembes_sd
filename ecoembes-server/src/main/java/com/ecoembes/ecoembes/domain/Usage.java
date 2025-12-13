package com.ecoembes.ecoembes.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_history")
public class Usage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dumpster_id", nullable = false)
    private Dumpster dumpster;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String fillLevel;

    @Column(nullable = false)
    private Integer containersCount;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    protected Usage() {}

    public Usage(Dumpster dumpster, LocalDate date, String fillLevel, Integer containersCount) {
        this.dumpster = dumpster;
        this.date = date;
        this.fillLevel = fillLevel;
        this.containersCount = containersCount;
        this.recordedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dumpster getDumpster() {
        return dumpster;
    }

    public void setDumpster(Dumpster dumpster) {
        this.dumpster = dumpster;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(String fillLevel) {
        this.fillLevel = fillLevel;
    }

    public Integer getContainersCount() {
        return containersCount;
    }

    public void setContainersCount(Integer containersCount) {
        this.containersCount = containersCount;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}

