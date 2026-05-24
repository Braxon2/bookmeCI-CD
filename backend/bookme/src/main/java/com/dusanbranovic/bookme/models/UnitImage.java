package com.dusanbranovic.bookme.models;

import jakarta.persistence.*;

@Entity
public class UnitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String s3Key;
    private String url;
    private Boolean isPrimary;
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private BookableUnit bookableUnit;

    public UnitImage() {
    }

    public UnitImage(
            String s3Key,
            String url,
            Boolean isPrimary,
            Integer sortOrder,
            BookableUnit bookableUnit
    ) {
        this.s3Key = s3Key;
        this.url = url;
        this.isPrimary = isPrimary;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
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
