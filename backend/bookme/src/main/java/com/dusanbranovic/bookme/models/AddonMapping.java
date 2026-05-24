package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AddonMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean perNight;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private BookableUnit bookableUnit;

    @ManyToOne
    @JoinColumn(name = "addon_id")
    private Addon addon;

    @OneToMany(mappedBy = "addonMapping", cascade = CascadeType.ALL)
    private List<PeriodPriceAddon> periodPriceAddons = new ArrayList<>();

    public AddonMapping() {
    }

    public AddonMapping(boolean perNight, BookableUnit bookableUnit, Addon addon) {
        this.perNight = perNight;
        this.bookableUnit = bookableUnit;
        this.addon = addon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPerNight() {
        return perNight;
    }

    public BookableUnit getBookableUnit() {
        return bookableUnit;
    }

    public void setPerNight(boolean perNight) {
        this.perNight = perNight;
    }


    public void setBookableUnit(BookableUnit bookableUnit) {
        this.bookableUnit = bookableUnit;
    }

    public Addon getAddon() {
        return addon;
    }

    public void setAddon(Addon addon) {
        this.addon = addon;
    }

    public List<PeriodPriceAddon> getPeriodPriceAddons() {
        return periodPriceAddons;
    }

    public void setPeriodPriceAddons(List<PeriodPriceAddon> periodPriceAddons) {
        this.periodPriceAddons = periodPriceAddons;
    }
}
