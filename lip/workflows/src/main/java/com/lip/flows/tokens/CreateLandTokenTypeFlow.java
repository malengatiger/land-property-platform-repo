package com.lip.flows.tokens;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.flows.regulator.ReportToRegulatorFlow;
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
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

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

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 creating LandToken Type ..."
        .concat("  \uD83C\uDF4E landState: ".concat(landState.getName())));
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        LandToken landToken = new LandToken(description,
                getOurIdentity(),
                new UniqueIdentifier(),
                landState);
        TransactionState<LandToken> transactionState = new TransactionState(landToken,notary);
        SignedTransaction tx = subFlow(new CreateEvolvableTokens(transactionState));
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E  \uD83C\uDF4E  CreateEvolvableTokens executed for LandTokenType");

        reportToRegulator(getServiceHub(),tx);
        return tx;
    }
    @Suspendable
    private void reportToRegulator(ServiceHub serviceHub, SignedTransaction mSignedTransactionDone) throws FlowException {
        logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  Talking to the Regulator, for compliance, Senor! .............");
        Set<Party> parties = serviceHub.getIdentityService().partiesFromName("Regulator",false);
        Party regulator = parties.iterator().next();
        try {
            subFlow(new ReportToRegulatorFlow(regulator,mSignedTransactionDone));
            logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  DONE talking to the Regulator, Phew!");

        } catch (Exception e) {
            logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Regulator fell down.  \uD83D\uDC7F IGNORED  \uD83D\uDC7F ", e);
            throw new FlowException("Regulator fell down!");
        }
    }
}
