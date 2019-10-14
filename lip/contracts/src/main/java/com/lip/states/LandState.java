package com.lip.states;

import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;

import java.math.BigDecimal;
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
    private final BigDecimal value;
    private final UUID identifier;

    public LandState(String name, Party landAffairsParty, Party bnoParty,
                     Party regulatorParty, Party bankParty, List<Coordinates> polygon, List<String> imageURLs,
                     Date dateRegistered, String description, BigDecimal value, UUID identifier) {
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
        this.identifier = identifier;
    }

    @Override
    public List<AbstractParty> getParticipants() {
       return ImmutableList.of(bnoParty,landAffairsParty,regulatorParty, bankParty);
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

    public BigDecimal getValue() {
        return value;
    }

    public UUID getIdentifier() {
        return identifier;
    }
}
