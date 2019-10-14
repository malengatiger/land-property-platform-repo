package com.lip.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.AccountInfoContract;
import com.r3.corda.lib.accounts.contracts.commands.AccountCommand;
import com.r3.corda.lib.accounts.contracts.commands.Create;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.contracts.types.AccountStatus;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InitiatingFlow
@StartableByRPC
public class AccountRegistrationFlow extends FlowLogic<AccountInfo> {
    private final static Logger logger = LoggerFactory.getLogger(AccountRegistrationFlow.class);

    final String accountName;
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

    public AccountRegistrationFlow(String accountName) {
        this.accountName = accountName;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A RegisterAccountFlow constructor with accountName:: \uD83C\uDF4F " + accountName);
    }

    @Override
    @Suspendable
    public AccountInfo call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();

        Party bnoParty = serviceHub.getMyInfo().getLegalIdentities().get(0);
        AccountInfo accountInfo = new AccountInfo(accountName, bnoParty,
                new UniqueIdentifier(), AccountStatus.ACTIVE);
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        AccountCommand command = new Create();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: "
                + notary.getName().toString()
                + "  \uD83C\uDF4A accountName: " + accountName);

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(accountInfo, AccountInfoContract.class.getName())
                .addCommand(command, bnoParty.getOwningKey());

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06  TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 signInitialTransaction executed ...");

        SignedTransaction mSignedTransactionDone = subFlow(
                new FinalityFlow(signedTx, ImmutableList.of(),
                        FINALISING_TRANSACTION.childProgressTracker()));
        logger.info((" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                "FinalityFlow has been executed").concat(" txId: ")
                .concat(mSignedTransactionDone.getId().toString()));

        return accountInfo;
    }

}
