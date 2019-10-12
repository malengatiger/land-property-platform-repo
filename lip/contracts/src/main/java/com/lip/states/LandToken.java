package com.lip.states;

import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandTokenContract;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BelongsToContract(LandTokenContract.class)
public class LandToken extends EvolvableTokenType {
    final String description;
    final Party maintainer;
    final UniqueIdentifier linearId;
    final LandState landState;

    public LandToken(String description, Party maintainer, UniqueIdentifier linearId, LandState landState) {
        this.description = description;
        this.maintainer = maintainer;
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
        return ImmutableList.of(maintainer);
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
