package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class BookingAddonItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_id", nullable = false)
    private Addon addon;

    @Column(
            name = "addon_name_snapshot",
            nullable = false
    )
    private String addonNameSnapshot;

    private double pricePaid;
    private boolean wasPerNight;

    public BookingAddonItem() {
    }

    public BookingAddonItem(Booking booking, String addonNameSnapshot, double pricePaid, boolean wasPerNight, Addon addon) {
        this.booking = booking;
        this.addonNameSnapshot = addonNameSnapshot;
        this.pricePaid = pricePaid;
        this.wasPerNight = wasPerNight;
        this.addon = addon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Addon getAddon() {
        return addon;
    }

    public void setAddon(Addon addon) {
        this.addon = addon;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(double pricePaid) {
        this.pricePaid = pricePaid;
    }

    public boolean isWasPerNight() {
        return wasPerNight;
    }

    public void setWasPerNight(boolean wasPerNight) {
        this.wasPerNight = wasPerNight;
    }

    public String getAddonNameSnapshot() {
        return addonNameSnapshot;
    }

    public void setAddonNameSnapshot(String addonNameSnapshot) {
        this.addonNameSnapshot = addonNameSnapshot;
    }
}
