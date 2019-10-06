package com.lip.webserver;

import com.lip.webserver.data.CoordinatesDTO;
import com.lip.webserver.data.X500Name;

import java.util.Date;
import java.util.List;

public class LandDTO {
    private String name;
    private X500Name landAffairsParty, bnoParty, regulatorParty, bankParty;
    private List<CoordinatesDTO> polygon;
    private List<String> imageURLs;
    private Date dateRegistered;
    private String description;
    private double originalValue;

    public LandDTO(String name, X500Name landAffairsParty,
                   X500Name bnoParty, X500Name regulatorParty,
                   X500Name bankParty, List<CoordinatesDTO> polygon,
                   List<String> imageURLs, Date dateRegistered,
                   String description, double originalValue) {

        this.name = name;
        this.landAffairsParty = landAffairsParty;
        this.bnoParty = bnoParty;
        this.regulatorParty = regulatorParty;
        this.bankParty = bankParty;
        this.polygon = polygon;
        this.imageURLs = imageURLs;
        this.dateRegistered = dateRegistered;
        this.description = description;
        this.originalValue = originalValue;
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

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(double originalValue) {
        this.originalValue = originalValue;
    }
}
