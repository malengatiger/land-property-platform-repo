package com.lip.states;

import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(LandContract.class)
@CordaSerializable
public class LandState implements ContractState {

    final String name;
    final Party landAffairsParty, bnoParty, regulatorParty, bankParty;
    final List<Coordinates> polygon;
    final List<String> imageURLs;
    final Date dateRegistered;
    final String description;
    final double originalValue;

    public LandState(String name, Party landAffairsParty, Party bnoParty,
                     Party regulatorParty, Party bankParty,
                     List<Coordinates> polygon, List<String> imageURLs,
                     Date dateRegistered, String description, double originalValue) {
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

    @Override
    public List<AbstractParty> getParticipants() {
       return ImmutableList.of(bnoParty,landAffairsParty,regulatorParty, bankParty);
    }

    public List<String> getImageURLs() {
        return imageURLs;
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

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public String getDescription() {
        return description;
    }

    public double getOriginalValue() {
        return originalValue;
    }
}
