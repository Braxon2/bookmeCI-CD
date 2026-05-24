package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;

@Entity
public class BookableUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @OneToMany(mappedBy = "bookableUnit", cascade = CascadeType.ALL)
    private List<PeriodPrice> periodPriceList = new ArrayList<>();

    @OneToMany(mappedBy = "bookableUnit", cascade = CascadeType.ALL)
    private List<AddonMapping> addonMappings = new ArrayList<>();

    @OneToMany(mappedBy = "bookableUnit", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "bookableUnit")
    private List<UnitFascilityMapping> unitFascilityMappings = new ArrayList<>();;

    @OneToMany(mappedBy = "bookableUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitImage> images = new ArrayList<>();;

    private int maxCapacity;
    private double squareMeters;
    private int totalUnits;
    private int singleBeds;
    private int doubleBeds;
    private int maxAdultCapacity;
    private int maxKidsCapacity;
    private String name;


    public BookableUnit() {
    }

    public BookableUnit(
            Property property,
            int maxCapacity,
            double squareMeters,
            int totalUnits,
            int singleBeds,
            int doubleBeds,
            int maxAdultCapacity,
            int maxKidsCapacity,
            String name
    ) {
        this.property = property;
        this.maxCapacity = maxCapacity;
        this.squareMeters = squareMeters;
        this.totalUnits = totalUnits;
        this.singleBeds = singleBeds;
        this.doubleBeds = doubleBeds;
        this.maxAdultCapacity = maxAdultCapacity;
        this.maxKidsCapacity = maxKidsCapacity;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public List<PeriodPrice> getPeriodPriceList() {
        return periodPriceList;
    }

    public void setPeriodPriceList(List<PeriodPrice> periodPriceList) {
        this.periodPriceList = periodPriceList;
    }

    public List<AddonMapping> getAddonMappings() {
        return addonMappings;
    }

    public void setAddonMappings(List<AddonMapping> addonMappings) {
        this.addonMappings = addonMappings;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public double getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(double squareMeters) {
        this.squareMeters = squareMeters;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public int getSingleBeds() {
        return singleBeds;
    }

    public void setSingleBeds(int singleBeds) {
        this.singleBeds = singleBeds;
    }

    public int getDoubleBeds() {
        return doubleBeds;
    }

    public void setDoubleBeds(int doubleBeds) {
        this.doubleBeds = doubleBeds;
    }

    public int getMaxAdultCapacity() {
        return maxAdultCapacity;
    }

    public void setMaxAdultCapacity(int maxAdultCapacity) {
        this.maxAdultCapacity = maxAdultCapacity;
    }

    public int getMaxKidsCapacity() {
        return maxKidsCapacity;
    }

    public void setMaxKidsCapacity(int maxKidsCapacity) {
        this.maxKidsCapacity = maxKidsCapacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<UnitFascilityMapping> getUnitFascilityMappings() {
        return unitFascilityMappings;
    }

    public void setUnitFascilityMappings(List<UnitFascilityMapping> unitFascilityMappings) {
        this.unitFascilityMappings = unitFascilityMappings;
    }

    public List<UnitImage> getImages() {
        return images;
    }

    public void setImages(List<UnitImage> images) {
        this.images = images;
    }

    public void addPeriodPrice(PeriodPrice periodPrice) {
        this.periodPriceList.add(periodPrice);
        periodPrice.setBookableUnit(this);
    }
}
