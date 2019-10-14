package com.lip.flows.regulator;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@StartableByRPC
@InitiatingFlow
public class ReportToRegulatorFlow extends FlowLogic<Void> {
    private final static Logger logger = LoggerFactory.getLogger(ReportToRegulatorFlow.class);

    private final Party regulator;
    private final SignedTransaction signedTransaction;

    public ReportToRegulatorFlow(Party regulator, SignedTransaction signedTransaction) {
        this.regulator = regulator;
        this.signedTransaction = signedTransaction;
    }
    @Suspendable
    @Override
    public Void call() throws FlowException {
        logger.info("\uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 " +
                "reporting to Regulator, Senor!");
        FlowSession session = initiateFlow(regulator);
        subFlow(new SendTransactionFlow(session,signedTransaction));
        logger.info("\uD83E\uDD6C \uD83E\uDD6C \uD83E\uDD6C" +
                "Done reporting to Regulator, Senor!");
        return null;
    }
}
