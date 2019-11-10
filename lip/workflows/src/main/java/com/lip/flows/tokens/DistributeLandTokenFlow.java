package com.lip.flows.tokens;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandTokenContract;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.states.LIPAccountState;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.commands.EvolvableTokenTypeCommand;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
@StartableByService
public class DistributeLandTokenFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(DistributeLandTokenFlow.class);
    private final String tokenId;
    private final long amount;
    private final LIPAccountState newTokenOwner;

    public DistributeLandTokenFlow(String tokenId, Long amount, LIPAccountState newTokenOwner) {
        this.tokenId = tokenId;
        this.amount = amount;
        this.newTokenOwner = newTokenOwner;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        logger.info(" \uD83D\uDECE  \uD83D\uDECE  \uD83D\uDECE Distribute LandToken tokens to: "
                .concat(newTokenOwner.getName())
                .concat(" for amount: ").concat("\uD83E\uDD6C " + amount)
        .concat(" \uD83D\uDE0E tokenId: \uD83D\uDE0E ".concat(tokenId)));
        ServiceHub serviceHub = getServiceHub();
        UUID uuid = UUID.fromString(tokenId);
        //construct the query criteria
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED);
        // grab the token type off the ledger which was created using CreateEvolvableTokens flow
        Vault.Page<LandToken> landTokenPage = serviceHub.getVaultService().
                queryBy(LandToken.class, queryCriteria);
        if (landTokenPage.getStates().isEmpty()) {
            throw new FlowException("\uD83D\uDC7F LandToken type not found");
        }
        LandToken landToken = landTokenPage.getStates().get(0).getState().getData();
        logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F LandToken type to be distributed: "
                .concat(landToken.toString())
                .concat("\uD83D\uDD06 LinearId: ").concat(landToken.getLinearId().getId().toString()));
        StateAndRef<FungibleToken> stateAndRef = getFungibleTokenStateAndRef(uuid);
        FungibleToken fungibleToken = stateAndRef.getState().getData();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 Existing FungibleToken: \uD83D\uDD06 \uD83D\uDD06 "
                .concat(fungibleToken.toString()));

        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        SignedTransaction signedTx = getSignedTransaction(fungibleToken, stateAndRef, notary);
        Party me = serviceHub.getMyInfo().getLegalIdentities().get(0);
        if (newTokenOwner.getAccountInfo().getHost().getName().getOrganisation()
                .equalsIgnoreCase(me.getName().getOrganisation())) {
            logger.info("\uD83E\uDDE1 \uD83E\uDDE1 ⚜️ ⚜️ ⚜️ New token owner on same host, ⚜️ no need to collect signatures");
            reportToRegulator(serviceHub,signedTx);
            return signedTx;
        }

        SignedTransaction mSignedTransactionDone = collectSignatures(signedTx);
        return  mSignedTransactionDone;

    }

    @NotNull
    @Suspendable
    private StateAndRef<FungibleToken> getFungibleTokenStateAndRef(UUID uuid) throws FlowException {
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<FungibleToken> fungibleTokens = getServiceHub().getVaultService()
                .queryBy(FungibleToken.class, criteria);

        StateAndRef<FungibleToken> stateAndRef = null;
        for (StateAndRef<FungibleToken> ref : fungibleTokens.getStates()) {
            FungibleToken token = ref.getState().getData();
            logger.info("\uD83D\uDD06 TokenIdentifier: ".concat(token.getIssuedTokenType().getTokenIdentifier()));
            logger.info("\uD83D\uDD06 Token Amount: ".concat(String.valueOf(token.getAmount())));
            logger.info("\uD83D\uDD06 Token class: ".concat(token.getIssuedTokenType().getTokenType().getTokenClass().getName()));
            if (token.getIssuedTokenType().getTokenIdentifier()
                    .equalsIgnoreCase(uuid.toString())) {
                stateAndRef = ref;
            }
        }
        if (stateAndRef == null) {
            throw new FlowException("FungibleToken not found");
        }
        return stateAndRef;
    }

    @Suspendable
    private SignedTransaction collectSignatures(SignedTransaction signedTx) throws FlowException {
        FlowSession ownerFlowSession = initiateFlow(newTokenOwner.getAccountInfo().getHost());
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
        SignedTransaction signedTransaction = subFlow(
                new CollectSignaturesFlow(signedTx,
                        ImmutableList.of(ownerFlowSession)));

        logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  " +
                "\uD83D\uDE21 \uD83D\uDE21 will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A : ")
                .concat(signedTransaction.toString()));

        SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(
                signedTransaction,
                ImmutableList.of(
                        ownerFlowSession)));
        reportToRegulator(getServiceHub(),mSignedTransactionDone);
        return mSignedTransactionDone;
    }

    @NotNull
    @Suspendable
    private SignedTransaction getSignedTransaction(FungibleToken fungibleToken,
                                                   StateAndRef<FungibleToken> stateAndRef,
                                                   Party notary) throws FlowException {

        SignedTransaction signedTx;
        try {
            Amount<IssuedTokenType> amt = new Amount(amount, fungibleToken.getIssuedTokenType());
            FungibleToken updatedToken = new FungibleToken(fungibleToken.getAmount().minus(amt),
                    fungibleToken.getHolder(), fungibleToken.getTokenTypeJarHash());
            FungibleToken tokenToBeDistributed = new FungibleToken(amt,
                    newTokenOwner.getAccountInfo().getHost(), fungibleToken.getTokenTypeJarHash());

            logger.info(" \uD83E\uDD66  \uD83E\uDD66 Updated Token:  \uD83E\uDD66 "
                    .concat(updatedToken.toString()));
            logger.info(" \uD83E\uDD66  \uD83E\uDD66 TokenToBeDistributed:  \uD83E\uDD66 "
                    .concat(tokenToBeDistributed.toString()));
            EvolvableTokenTypeCommand command = new LandTokenContract.Distribute();
            TransactionBuilder builder = new TransactionBuilder(notary)
                    .addInputState(stateAndRef)
                    .addOutputState(updatedToken)
                    .addOutputState(tokenToBeDistributed)
                    .addCommand(command, fungibleToken.getHolder().getOwningKey(), newTokenOwner.getAccountInfo().getHost().getOwningKey());

            builder.verify(getServiceHub());
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 TransactionBuilder verified");
            // Signing the transaction.
            signedTx = getServiceHub().signInitialTransaction(builder);
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 DistributeLandTokenFlow signInitialTransaction executed ..."
                    .concat(signedTx.toString()));
            return signedTx;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FlowException("\uD83D\uDC7F \uD83D\uDC7F Unable to get Transaction signed:" +
                    " \uD83D\uDC79" + e.getMessage());
        }
    }

    @Suspendable
    private void reportToRegulator(ServiceHub serviceHub, SignedTransaction mSignedTransactionDone) throws FlowException {
        logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  Talking to the Regulator, for compliance, Senor! .............");
        Set<Party> parties = serviceHub.getIdentityService().partiesFromName("Regulator", false);
        Party regulator = parties.iterator().next();
        try {
            subFlow(new ReportToRegulatorFlow(regulator, mSignedTransactionDone));
            logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  DONE talking to the Regulator, Phew!");

        } catch (Exception e) {
            logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Regulator fell down.  \uD83D\uDC7F IGNORED  \uD83D\uDC7F ", e);
            throw new FlowException("Regulator fell down!");
        }
    }
}
