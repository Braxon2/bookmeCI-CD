package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class UnitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, unique = true, length = 1024)
    private String s3Key;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private BookableUnit bookableUnit;

    public UnitImage() {
    }

    public UnitImage(
            String s3Key,
            Boolean primary,
            Integer sortOrder,
            BookableUnit bookableUnit
    ) {
        this.s3Key = s3Key;
        this.primary = primary;
        this.sortOrder = sortOrder;
        this.bookableUnit = bookableUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        primary = primary;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BookableUnit getBookableUnit() {
        return bookableUnit;
    }

    public void setBookableUnit(BookableUnit bookableUnit) {
        this.bookableUnit = bookableUnit;
    }
}
