package com.lip.flows.tokens;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.lip.contracts.LandContract;
import com.lip.contracts.LandTokenContract;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.flows.regulator.ReportToRegulatorFlow;
import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.*;
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
public class CreateLandTokenTypeFlow extends FlowLogic<FungibleToken> {
    private final static Logger logger = LoggerFactory.getLogger(RegisterLandFlow.class);
    final LandState landState;
    final String description;
    final long numberOfTokens;

    public CreateLandTokenTypeFlow(LandState landState, String description, long numberOfTokens) {
        this.landState = landState;
        this.description = description;
        this.numberOfTokens = numberOfTokens;
    }

    @Suspendable
    @Override
    public FungibleToken call() throws FlowException {
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 creating LandToken Type ..."
                .concat("  \uD83C\uDF4E landState: ".concat(landState.getName())));
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        List<NodeInfo> nodes = getServiceHub().getNetworkMapCache().getAllNodes();
        List<PublicKey> publicKeys = new ArrayList<>();
        List<Party> parties = new ArrayList<>();
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
        LandToken landToken = new LandToken(description,
                new UniqueIdentifier(),
                landState);
        TransactionState<LandToken> transactionState = new TransactionState(landToken, notary);
        SignedTransaction tx = subFlow(new CreateEvolvableTokens(transactionState));
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E  \uD83C\uDF4E  CreateEvolvableTokens executed for LandTokenType; tx:"
                .concat(tx.getId().toString()));

        TokenPointer tokenPointer = landToken.toPointer(LandToken.class);
        logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F TokenPointer created to build Fungible token: ");
        IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), tokenPointer);
        FungibleToken fungibleToken = new FungibleToken(
                new Amount<>(numberOfTokens, issuedTokenType),
                getOurIdentity(), TransactionUtilitiesKt.getAttachmentIdForGenericParam(tokenPointer));

        logger.info((" \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35" +
                " fungibleToken.getIssuedTokenType().getTokenIdentifier(): ")
                .concat(fungibleToken.getIssuedTokenType().getTokenIdentifier()));
        logger.info("\uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 IssueTokens for FungibleToken starting with "
                + parties.size() + " observers");

        SignedTransaction tx2 = subFlow(new IssueTokens(ImmutableList.of(fungibleToken),parties));

        logger.info("\uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 CreateLandTokenTypeFlow executed OK; txId: "
                .concat(tx2.getId().toString()).concat("  \uD83C\uDF4E "));

        return fungibleToken;
    }

}
