package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private BookableUnit bookableUnit;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private User guest;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingAddonItem> addonItems = new ArrayList<>();

    private Double totalPrice;
    private LocalDate createdAt;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public Booking() {
    }

    public Booking(
            BookableUnit bookableUnit,
            User guest,
            Double totalPrice,
            LocalDate createdAt,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            BookingStatus status
    ) {
        this.bookableUnit = bookableUnit;
        this.guest = guest;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
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

    public User getGuest() {
        return guest;
    }

    public void setGuest(User guest) {
        this.guest = guest;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void addAddonItem(BookingAddonItem item) {
        addonItems.add(item);
        item.setBooking(this);
    }

    public List<BookingAddonItem> getAddonItems() {
        return addonItems;
    }

    public void setAddonItems(List<BookingAddonItem> addonItems) {
        this.addonItems = addonItems;
    }
}
