package com.lip.flows.tokens;

import com.google.common.collect.ImmutableList;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
@StartableByService
public class IssueLandTokenFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(RegisterLandFlow.class);
    final String tokenId;
    final Long amount;
    final Party holder;

    public IssueLandTokenFlow(String tokenId, Long amount, Party holder) {
        this.tokenId = tokenId;
        this.amount = amount;
        this.holder = holder;
    }

    @Override
    public SignedTransaction call() throws FlowException {

        //using id of evolvable token type to grab the evolvable Token type from db.
        // you can use any custom criteria depending on your requirements
        UUID uuid = UUID.fromString(tokenId);

        //construct the query criteria
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED);

        // grab the token type off the ledger which was created using CreateEvolvableTokens flow
        StateAndRef<LandToken> stateAndRef = getServiceHub().getVaultService().
                queryBy(LandToken.class, queryCriteria).getStates().get(0);
        LandToken evolvableTokenType = stateAndRef.getState().getData();

        //get the pointer pointer to the evolvable token type
        TokenPointer tokenPointer = evolvableTokenType.toPointer(evolvableTokenType.getClass());

        //assign the issuer to the token type who will be issuing the tokens
        IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), tokenPointer);

        //mention the current holder also
        FungibleToken nonFungibleToken = new FungibleToken(
                new Amount<>(amount,issuedTokenType),
                holder,TransactionUtilitiesKt.getAttachmentIdForGenericParam(tokenPointer));
        //call built in flow to issue non fungible tokens
        SignedTransaction tx = subFlow(new IssueTokens(ImmutableList.of(nonFungibleToken)));

        //todo - report tx to regulator
        return tx;
    }
}
