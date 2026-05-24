package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class PropertyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "propertyType")
    private List<Property> properties;

    public PropertyType() {
    }

    public PropertyType(String name) {
        this.name = name;
    }

    public PropertyType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
