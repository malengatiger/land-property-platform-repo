package com.lip.flows.tokens;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
@StartableByService
public class DistributeLandTokenFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(DistributeLandTokenFlow.class);
    final String tokenId;
    final Long amount;
    final Party owner;

    public DistributeLandTokenFlow(String tokenId, Long amount, Party owner) {
        this.tokenId = tokenId;
        this.amount = amount;
        this.owner = owner;
    }

    @Override
    public SignedTransaction call() throws FlowException {
        logger.info(" \uD83D\uDECE  \uD83D\uDECE  \uD83D\uDECE DistributeLandToken tokens to: ".concat(owner.getName().toString())
        .concat(" for amount: ").concat("\uD83E\uDD6C " + amount));
        UUID uuid = UUID.fromString(tokenId);
        //construct the query criteria
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED);

        // grab the token type off the ledger which was created using CreateEvolvableTokens flow
        Vault.Page<LandToken> page = getServiceHub().getVaultService().
                queryBy(LandToken.class, queryCriteria);
        if (page.getStates().isEmpty() ) {
            throw new FlowException("\uD83D\uDC7F LandToken type not found");
        }
        LandToken landToken = page.getStates().get(0).getState().getData();
        logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F LandToken type to be issued: ".concat(landToken.getDescription()));
        try {
            TokenPointer tokenPointer = landToken.toPointer(LandToken.class);
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F TokenPointer created to build Fungible token: ");
            IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), tokenPointer );
            FungibleToken fungibleToken = new FungibleToken(
                    new Amount<>(amount, issuedTokenType),
                    owner, TransactionUtilitiesKt.getAttachmentIdForGenericParam(tokenPointer));
            //call built in flow to issue fungible tokens
            SignedTransaction tx = subFlow(new IssueTokens(ImmutableList.of(fungibleToken)));
            logger.info("\uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 IssueTokens executed OK; txId: ".concat(tx.getId().toString()));
            reportToRegulator(getServiceHub(),tx);
            return tx;
        } catch (Exception e) {
            throw new FlowException("Unable to create FungibleToken, possible toPointer exception");
        }

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
