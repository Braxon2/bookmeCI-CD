package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "addon", cascade = CascadeType.ALL)
    private List<AddonMapping> addonMappings = new ArrayList<>();

    public Addon() {
    }

    public Addon(String name) {
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

    public List<AddonMapping> getAddonMappings() {
        return addonMappings;
    }

    public void setAddonMappings(List<AddonMapping> addonMappings) {
        this.addonMappings = addonMappings;
    }
}
