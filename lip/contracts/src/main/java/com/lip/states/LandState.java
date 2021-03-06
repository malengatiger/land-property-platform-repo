package com.lip.states;

import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// *********
// * State *
// *********
@BelongsToContract(LandContract.class)
@CordaSerializable
public class LandState implements ContractState {

    private final String name;
    private final Party landAffairsParty, bnoParty, regulatorParty, bankParty;
    private final List<Coordinates> polygon;
    private final List<String> imageURLs;
    private final Date dateRegistered;
    private final String description;
    private final long value;
    private final long areaInSquareMetres;
    private final UUID identifier;

    public LandState(String name, Party landAffairsParty, Party bnoParty, Party regulatorParty, Party bankParty, List<Coordinates> polygon, List<String> imageURLs, Date dateRegistered, String description, long value, long areaInSquareMetres, UUID identifier) {
        this.name = name;
        this.landAffairsParty = landAffairsParty;
        this.bnoParty = bnoParty;
        this.regulatorParty = regulatorParty;
        this.bankParty = bankParty;
        this.polygon = polygon;
        this.imageURLs = imageURLs;
        this.dateRegistered = dateRegistered;
        this.description = description;
        this.value = value;
        this.areaInSquareMetres = areaInSquareMetres;
        this.identifier = identifier;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
       return ImmutableList.of(bnoParty,landAffairsParty,regulatorParty, bankParty);
    }

    public long getAreaInSquareMetres() {
        return areaInSquareMetres;
    }

    public void addImageURL(String url) {
        imageURLs.add(url);
    }

    public String getName() {
        return name;
    }

    public Party getLandAffairsParty() {
        return landAffairsParty;
    }

    public Party getBnoParty() {
        return bnoParty;
    }

    public Party getRegulatorParty() {
        return regulatorParty;
    }

    public Party getBankParty() {
        return bankParty;
    }

    public List<Coordinates> getPolygon() {
        return polygon;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public String getDescription() {
        return description;
    }

    public long getValue() {
        return value;
    }

    public UUID getIdentifier() {
        return identifier;
    }
}
