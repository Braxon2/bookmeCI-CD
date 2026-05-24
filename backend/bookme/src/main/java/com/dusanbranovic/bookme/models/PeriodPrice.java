package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
public class PeriodPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private BookableUnit bookableUnit;

    private double pricePerNight;

    private LocalDate startDate;
    private LocalDate endDate;
    private String season;

    public PeriodPrice() {
    }

    public PeriodPrice(
            BookableUnit bookableUnit,
            double pricePerNight,
            LocalDate startDate,
            LocalDate endDate,
            String season
    ) {
        this.bookableUnit = bookableUnit;
        this.pricePerNight = pricePerNight;
        this.startDate = startDate;
        this.endDate = endDate;
        this.season = season;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookableUnit getBookableUnit() {
        return bookableUnit;
    }

    public void setBookableUnit(BookableUnit bookableUnit) {
        this.bookableUnit = bookableUnit;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
