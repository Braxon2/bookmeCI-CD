package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class UnitFascillity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "unitFascillity")
    private List<UnitFascilityMapping> unitFascilityMappings;

    public UnitFascillity() {
    }

    public UnitFascillity(String name) {
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

    public List<UnitFascilityMapping> getUnitFascilityMappings() {
        return unitFascilityMappings;
    }

    public void setUnitFascilityMappings(List<UnitFascilityMapping> unitFascilityMappings) {
        this.unitFascilityMappings = unitFascilityMappings;
    }
}
