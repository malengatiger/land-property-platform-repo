package com.lip.flows.land;

import co.paralleluniverse.fibers.Suspendable;
import com.lip.contracts.LandContract;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.flows.tokens.CreateLandTokenTypeFlow;
import com.lip.states.LandState;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;

@InitiatingFlow
@StartableByRPC
public class RegisterLandFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(RegisterLandFlow.class);

    final LandState landState;

    private final ProgressTracker.Step SENDING_TRANSACTION = new ProgressTracker.Step("Sending transaction to counterParty");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A ProgressTracker childProgressTracker ...");
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
    // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
    // function.
    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION,
            SENDING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public RegisterLandFlow(LandState landState) {
        this.landState = landState;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterLandFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        Party me = serviceHub.getMyInfo().getLegalIdentities().get(0);
        if (!me.getName().getOrganisation().contains("LandAffairs")) {
            throw new FlowException("Only the Department of Land Affairs can register land parcels");
        }

        LandContract.Register command = new LandContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A landAffairsParty: " + landState.getLandAffairsParty().getName().toString()
                + "  \uD83C\uDF4A bankParty: " + landState.getBankParty().getName().toString()
                + " \uD83E\uDDE9 bnoParty: " + landState.getBnoParty().getName().toString()
                + "  \uD83C\uDF4A regulatorParty: "+ landState.getRegulatorParty().getName().toString() +" \uD83C\uDF4E  name: "
                + landState.getName().concat("  \uD83D\uDC9A originalValue") + landState.getValue());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(landState, LandContract.ID)
                .addCommand(command,
                        landState.getBankParty().getOwningKey(),
                        landState.getLandAffairsParty().getOwningKey(),
                        landState.getRegulatorParty().getOwningKey(),
                        landState.getBnoParty().getOwningKey());

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 TransactionBuilder built ...Org: " + txBuilder.getNotary().getName().getOrganisation());
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Land Register TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Land Register Transaction signInitialTransaction executed ...");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Transaction signInitialTransaction: ".concat(signedTx.toString()));

        FlowSession regulatorFlowSession = initiateFlow(landState.getRegulatorParty());
        FlowSession bnoFlowSession = initiateFlow(landState.getBnoParty());
        FlowSession bankFlowSession = initiateFlow(landState.getBankParty());

        progressTracker.setCurrentStep(GATHERING_SIGS);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
        SignedTransaction signedTransaction = subFlow(
                new CollectSignaturesFlow(signedTx,
                        ImmutableList.of(regulatorFlowSession, bnoFlowSession, bankFlowSession),
                        GATHERING_SIGS.childProgressTracker()));

        logger.info("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ".concat(signedTransaction.toString()));

        SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(
                signedTransaction,
                ImmutableList.of(
                        regulatorFlowSession,
                        bnoFlowSession,
                        bankFlowSession),
                FINALISING_TRANSACTION.childProgressTracker()));
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66 ❄️ ❄️ ❄️");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:  ❄️ ❄️ : ".concat(mSignedTransactionDone.toString()));

        SignedTransaction tx = subFlow(new CreateLandTokenTypeFlow(
                landState,"First creation: ".concat(new Date().toString())));
        reportToRegulator(serviceHub,tx);
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
