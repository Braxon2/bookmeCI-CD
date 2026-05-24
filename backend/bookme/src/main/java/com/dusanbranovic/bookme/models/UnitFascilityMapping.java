package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class UnitFascilityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private BookableUnit bookableUnit;

    @ManyToOne
    @JoinColumn(name = "unitFacility_id")
    private UnitFascillity unitFascillity;

    public UnitFascilityMapping() {
    }

    public UnitFascilityMapping(BookableUnit bookableUnit, UnitFascillity unitFascillity) {
        this.bookableUnit = bookableUnit;
        this.unitFascillity = unitFascillity;
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

    public UnitFascillity getUnitFascillity() {
        return unitFascillity;
    }

    public void setUnitFascillity(UnitFascillity unitFascillity) {
        this.unitFascillity = unitFascillity;
    }
}
