package com.lip.flows.tokens;

import com.google.common.collect.ImmutableList;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import net.corda.core.contracts.TransactionState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@InitiatingFlow
@StartableByRPC
public class CreateLandTokenTypeFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(RegisterLandFlow.class);
    final LandState landState;
    final String description;

    public CreateLandTokenTypeFlow(LandState landState, String description) {
        this.landState = landState;
        this.description = description;
    }

    @Override
    public SignedTransaction call() throws FlowException {
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 creating LandTokenType ..."
        .concat("  \uD83C\uDF4E landState: ").concat("  \uD83C\uDF4E regulator: "));
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        LandToken landToken = new LandToken(description,
                getOurIdentity(),
                new UniqueIdentifier(),
                landState);
        TransactionState<LandToken> transactionState = new TransactionState(landToken,notary);
        SignedTransaction tx = subFlow(new CreateEvolvableTokens(transactionState));
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E  \uD83C\uDF4E  CreateEvolvableTokens executed for LandTokenType");
        return tx;
    }
}
