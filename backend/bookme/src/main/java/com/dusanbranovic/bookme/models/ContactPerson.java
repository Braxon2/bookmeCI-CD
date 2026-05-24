package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class ContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User contactPerson;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    public ContactPerson() {
    }

    public ContactPerson(User contactPerson, Property property) {
        this.contactPerson = contactPerson;
        this.property = property;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(User contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
