package com.lip.flows.tokens;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandTokenContract;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.FungibleTokenContract;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@InitiatingFlow
@StartableByRPC
public class BroadcastLandTokenFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(BroadcastLandTokenFlow.class);
    private final FungibleToken fungibleToken;

    public BroadcastLandTokenFlow(FungibleToken fungibleToken) {
        this.fungibleToken = fungibleToken;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A  BroadcastLandTokenFlow starting ...");
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        List<NodeInfo> nodes = getServiceHub().getNetworkMapCache().getAllNodes();
        List<PublicKey> publicKeys = new ArrayList<>();
        List<Party> parties = new ArrayList<>();
        List<FlowSession> flowSessions = new ArrayList<>();
        NodeInfo mInfo = getServiceHub().getMyInfo();
        for (NodeInfo info: nodes) {
            if (info.getLegalIdentities().get(0).getName().getOrganisation().contains("Notary")) {
                continue;
            }
            if (info.getLegalIdentities().get(0).getName().getOrganisation()
                    .equalsIgnoreCase(mInfo.getLegalIdentities().get(0).getName().getOrganisation())) {
                continue;
            }
            publicKeys.add(info.getLegalIdentities().get(0).getOwningKey());
            parties.add(info.getLegalIdentities().get(0));
        }
        LandTokenContract.Register command = new LandTokenContract.Register();
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(fungibleToken, FungibleTokenContract.Companion.getContractId())
                .addCommand(command,publicKeys);
        txBuilder.verify(getServiceHub());
        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A  Land Register TransactionBuilder verified"
                .concat(" " + publicKeys.size()).concat(" publicKeys"));
        // Signing the transaction.
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A  Broadcast token Transaction signInitialTransaction executed ...");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Broadcast signInitialTransaction: ".concat(signedTx.toString()));
        for (Party party: parties) {
            flowSessions.add(initiateFlow(party));
        }
        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A  Collecting Signatures for Token Broadcast....");
        SignedTransaction signedTransaction = subFlow(
                new CollectSignaturesFlow(signedTx,
                        flowSessions));

        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A     \uD83D\uDC99 Signatures collected OK!   \uD83D\uDC99  \uD83D\uDC99  will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ".concat(signedTransaction.toString()));
        SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(
                signedTransaction,
                flowSessions));

        logger.info("\uD83D\uDD0A \uD83D\uDD0A \uD83D\uDD0A   \uD83D\uDD34  \uD83D\uDD34 BroadcastLandTokenFlow done. Returning signedTx: "
        .concat(mSignedTransactionDone.getId().toString()).concat("  \uD83D\uDD34  \uD83D\uDD34 "));
        return mSignedTransactionDone;
    }


    @Suspendable
    private void reportToRegulator(ServiceHub serviceHub,
                                   SignedTransaction mSignedTransactionDone) throws FlowException {
        logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  Talking to the Regulator, " +
                " for compliance, Senor! .............");
        Set<Party> parties = serviceHub.getIdentityService().partiesFromName("Regulator",false);
        Party regulator = parties.iterator().next();
        try {
            subFlow(new ReportToRegulatorFlow(regulator,mSignedTransactionDone));
            logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  DONE talking to the Regulator, Phew!");

        } catch (Exception e) {
            logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Regulator fell down.  " +
                    "\uD83D\uDC7F IGNORED  \uD83D\uDC7F ", e);
            throw new FlowException("Regulator fell down!");
        }
    }
}
