package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
public class PeriodPriceAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "addon_mapping_id")
    private AddonMapping addonMapping;

    private double price;

    private LocalDate startDate;
    private LocalDate endDate;

    public PeriodPriceAddon() {
    }

    public PeriodPriceAddon(AddonMapping addonMapping, double price, LocalDate startDate, LocalDate endDate) {
        this.addonMapping = addonMapping;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AddonMapping getAddonMapping() {
        return addonMapping;
    }

    public void setAddonMapping(AddonMapping addonMapping) {
        this.addonMapping = addonMapping;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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
}
