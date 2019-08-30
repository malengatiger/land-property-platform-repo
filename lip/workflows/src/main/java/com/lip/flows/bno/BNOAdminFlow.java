package com.lip.flows.bno;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.businessnetworks.membership.flows.bno.support.BusinessNetworkOperatorFlowLogic;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InitiatingFlow
@StartableByRPC
public class BNOAdminFlow extends BusinessNetworkOperatorFlowLogic {
    private final static Logger logger = LoggerFactory.getLogger(BNOAdminFlow.class);
    public BNOAdminFlow() {
        logger.info("\uD83C\uDFC0  \uD83C\uDFC0 Constructor run ");
    }


    @Override
    @Suspendable
    public Object call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();
        Party me = serviceHub.getMyInfo().getLegalIdentities().get(0);
        logger.info("\uD83C\uDFC0  \uD83C\uDFC0 The node party: ".concat(me.getName().toString()));
        StateAndRef membershipStateStateAndRef = findMembershipStateForParty(me);
        logger.info("\uD83C\uDFC0  \uD83C\uDFC0 membershipStateStateAndRef: " + membershipStateStateAndRef.getRef().getTxhash().toString());
        return null;
    }
}
