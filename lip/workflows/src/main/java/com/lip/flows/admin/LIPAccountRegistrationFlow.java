package com.lip.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.contracts.LIPAccountContract;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.states.LIPAccountState;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@InitiatingFlow
@StartableByRPC
public class LIPAccountRegistrationFlow extends FlowLogic<LIPAccountState> {
    private final static Logger logger = LoggerFactory.getLogger(LIPAccountRegistrationFlow.class);

    private final AccountInfo accountInfo;
    private final String email;
    private final String cellphone, name;

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

    public LIPAccountRegistrationFlow(AccountInfo accountInfo,
                                      String email, String cellphone, String name) {
        this.accountInfo = accountInfo;
        this.email = email;
        this.cellphone = cellphone;
        this.name = name;
    }

    @Override
    @Suspendable
    public LIPAccountState call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();
        logger.info((" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... " +
                "LIPAccountRegistrationFlow call started ...").concat(accountInfo.getName()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        //generate crypto materials for new account
        PartyAndCertificate partyAndCertificate = subFlow(new NewKeyForAccount(accountInfo.getIdentifier().getId()));
        List<Party> parties = new ArrayList<>();
        List<PublicKey> publicKeys = new ArrayList<>();
        List<NodeInfo> nodes = serviceHub.getNetworkMapCache().getAllNodes();
        Party me = getOurIdentity();
        for (NodeInfo info: nodes) {
            if (info.getLegalIdentities().get(0).getName().getOrganisation().contains("Notary")) {
                continue;
            }
            parties.add(info.getLegalIdentities().get(0));
            publicKeys.add(info.getLegalIdentities().get(0).getOwningKey());
        }
        LIPAccountState lipAccountState = new LIPAccountState(
                accountInfo,
                partyAndCertificate,
                parties,
                accountInfo.getName(),
                email,
                cellphone,
                accountInfo.getIdentifier().getId().toString());

        logger.info("\uD83C\uDFD3 Parties to share account with: ".concat(" \uD83C\uDFD3 " + parties.size()));
        logger.info("\uD83C\uDFD3 PartyAndCertificate created for: ".concat(partyAndCertificate.getName().toString()));
        logger.info("\uD83C\uDFD3 certificate publicKey: ".concat(partyAndCertificate.getCertificate().getPublicKey().getEncoded().toString()));

        LIPAccountContract.Register command = new LIPAccountContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A lipAccountState: " + lipAccountState.getAccountInfo().getName());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(lipAccountState, LIPAccountContract.ID)
                .addCommand(command, publicKeys);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 LIPAccountRegistrationFlow TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 LIPAccountRegistrationFlow " +
                "Transaction signInitialTransaction executed ...");

        progressTracker.setCurrentStep(GATHERING_SIGS);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
        List<FlowSession> flowSessionList = new ArrayList<>();
        Party myNode = serviceHub.getMyInfo().getLegalIdentities().get(0);
        for (Party party: parties) {
            if (Arrays.toString(party.getOwningKey().getEncoded())
                    .equalsIgnoreCase(Arrays.toString(myNode.getOwningKey().getEncoded()))) {
                continue;
            }
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 creating FlowSession for: "
            .concat(party.getName().toString()));
            flowSessionList.add(initiateFlow(party));
        }
        SignedTransaction signedTransaction = subFlow(
                new CollectSignaturesFlow(signedTx,
                        flowSessionList,
                        GATHERING_SIGS.childProgressTracker()));

        logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  " +
                "Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 will call FinalityFlow ..." +
                " \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ").concat(signedTransaction.toString()));

        SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(
                signedTransaction,
                flowSessionList,
                FINALISING_TRANSACTION.childProgressTracker()));
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ..." +
                " \uD83E\uDD66  are we good? \uD83E\uDD66 ❄️ ❄️ ❄️");
        logger.info((" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:  " +
                "❄️ ❄️ : ").concat(mSignedTransactionDone.toString()));

        reportToRegulator(serviceHub,mSignedTransactionDone);
        return lipAccountState;
    }

    @Suspendable
    private void reportToRegulator(ServiceHub serviceHub, SignedTransaction mSignedTransactionDone) throws FlowException {
        logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  Talking to the Regulator, " +
                "for compliance, Senor! .............");
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
