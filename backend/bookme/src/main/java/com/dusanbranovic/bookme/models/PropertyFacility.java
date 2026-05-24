package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class PropertyFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Fascillity facility;

    public PropertyFacility() {
    }

    public PropertyFacility(Property property, Fascillity facility) {
        this.property = property;
        this.facility = facility;
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

    public Fascillity getFacility() {
        return facility;
    }

    public void setFacility(Fascillity facility) {
        this.facility = facility;
    }
}
