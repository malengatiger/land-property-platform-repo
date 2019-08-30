package com.lip.flows.bno;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.businessnetworks.membership.flows.bno.support.BusinessNetworkOperatorInitiatedFlow;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.ServiceHub;
import org.jetbrains.annotations.NotNull;

@InitiatingFlow
@StartableByRPC
public class BNOStarterFlow extends BusinessNetworkOperatorInitiatedFlow {
    public BNOStarterFlow(@NotNull FlowSession flowSession) {
        super(flowSession);
    }

    @Override
    @Suspendable
    public Object onCounterpartyMembershipVerified(@NotNull StateAndRef counterpartyMembership) {
       final ServiceHub serviceHub = getServiceHub();
        return null;
    }
}
