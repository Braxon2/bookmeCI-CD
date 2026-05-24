package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Fascillity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "facility")
    private List<PropertyFacility> propertyFacilities;

    public Fascillity() {
    }

    public Fascillity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
