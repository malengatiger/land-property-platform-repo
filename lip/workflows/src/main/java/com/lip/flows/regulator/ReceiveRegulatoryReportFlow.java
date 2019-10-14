package com.lip.flows.regulator;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.node.StatesToRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InitiatedBy(ReportToRegulatorFlow.class)
public class ReceiveRegulatoryReportFlow extends FlowLogic<Void> {
    private final static Logger logger = LoggerFactory.getLogger(ReceiveRegulatoryReportFlow.class);

    final FlowSession otherSession;

    public ReceiveRegulatoryReportFlow(FlowSession otherSession) {
        this.otherSession = otherSession;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        logger.info("\uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 " +
                "Regulator receiving, Senor!");
        subFlow(new ReceiveTransactionFlow(otherSession,
                true, StatesToRecord.ALL_VISIBLE));
        return null;
    }
}
