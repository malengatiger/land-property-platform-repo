package com.lip.webserver.data;

import java.util.List;

public class LandDTO {
    private String name;
    private X500Name landAffairsParty, bnoParty, regulatorParty, bankParty;
    private List<CoordinatesDTO> polygon;
    private List<String> imageURLs;
    private String dateRegistered;
    private String description, identifier;
    private long value, areaInSquareMetres;

    public LandDTO(String name, X500Name landAffairsParty, X500Name bnoParty, X500Name regulatorParty, X500Name bankParty, List<CoordinatesDTO> polygon, List<String> imageURLs, String dateRegistered, String description, String identifier, long value, long areaInSquareMetres) {
        this.name = name;
        this.landAffairsParty = landAffairsParty;
        this.bnoParty = bnoParty;
        this.regulatorParty = regulatorParty;
        this.bankParty = bankParty;
        this.polygon = polygon;
        this.imageURLs = imageURLs;
        this.dateRegistered = dateRegistered;
        this.description = description;
        this.identifier = identifier;
        this.value = value;
        this.areaInSquareMetres = areaInSquareMetres;
    }

    public long getAreaInSquareMetres() {
        return areaInSquareMetres;
    }

    public void setAreaInSquareMetres(long areaInSquareMetres) {
        this.areaInSquareMetres = areaInSquareMetres;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public X500Name getBankParty() {
        return bankParty;
    }

    public void setBankParty(X500Name bankParty) {
        this.bankParty = bankParty;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public LandDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public X500Name getLandAffairsParty() {
        return landAffairsParty;
    }

    public void setLandAffairsParty(X500Name landAffairsParty) {
        this.landAffairsParty = landAffairsParty;
    }

    public X500Name getBnoParty() {
        return bnoParty;
    }

    public void setBnoParty(X500Name bnoParty) {
        this.bnoParty = bnoParty;
    }

    public X500Name getRegulatorParty() {
        return regulatorParty;
    }

    public void setRegulatorParty(X500Name regulatorParty) {
        this.regulatorParty = regulatorParty;
    }

    public List<CoordinatesDTO> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<CoordinatesDTO> polygon) {
        this.polygon = polygon;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(String dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
