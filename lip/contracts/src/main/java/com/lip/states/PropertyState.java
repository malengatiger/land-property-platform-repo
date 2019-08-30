package com.lip.states;

import com.lip.contracts.PropertyContract;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(PropertyContract.class)
public class PropertyState implements ContractState {

    final String name;
    final Party bnoParty, regulatorParty;
    final Coordinates coordinates;
    final Date dateRegistered;
    final String description;
    final double originalValue;

    public PropertyState(String name, Party bnoParty, Party regulatorParty, Coordinates coordinates, Date dateRegistered, String description, double originalValue) {
        this.name = name;
        this.bnoParty = bnoParty;
        this.regulatorParty = regulatorParty;
        this.coordinates = coordinates;
        this.dateRegistered = dateRegistered;
        this.description = description;
        this.originalValue = originalValue;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(bnoParty,regulatorParty);
    }

    public String getName() {
        return name;
    }

    public Party getBnoParty() {
        return bnoParty;
    }

    public Party getRegulatorParty() {
        return regulatorParty;
    }

    public Coordinates getCoordinates() {
        return coordinates;
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
