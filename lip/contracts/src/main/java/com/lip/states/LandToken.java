package com.lip.states;

import com.lip.contracts.LandTokenContract;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BelongsToContract(LandTokenContract.class)
@CordaSerializable

public class LandToken extends EvolvableTokenType {
    private final String description;
    private final UniqueIdentifier linearId;
    private final LandState landState;

    public LandToken(String description, UniqueIdentifier linearId, LandState landState) {
        this.description = description;
        this.linearId = linearId;
        this.landState = landState;
    }

    @Override
    public int getFractionDigits() {
        return 0;
    }

    @NotNull
    @Override
    public List<Party> getMaintainers() {
        List<Party> parties = new ArrayList<>();
        parties.add(landState.getBnoParty());
        parties.add(landState.getRegulatorParty());
        parties.add(landState.getBankParty());
        parties.add(landState.getLandAffairsParty());

        return parties;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getDescription() {
        return description;
    }

    public LandState getLandState() {
        return landState;
    }
}
