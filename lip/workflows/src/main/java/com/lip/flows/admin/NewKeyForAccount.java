package com.lip.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.ServiceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@StartableByRPC
@StartableByService
public class NewKeyForAccount extends FlowLogic<PartyAndCertificate> {
    private final static Logger logger = LoggerFactory.getLogger(AccountRegistrationFlow.class);
    final UUID accountId;

    public NewKeyForAccount(UUID accountId) {
        this.accountId = accountId;
    }

    @Override
    @Suspendable
    public PartyAndCertificate call() throws FlowException {
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A call started, accountId: ".concat(accountId.toString()));
        final ServiceHub serviceHub = getServiceHub();
        PartyAndCertificate partyAndCertificate = serviceHub.getKeyManagementService()
                .freshKeyAndCert(getOurIdentityAndCert(),false,accountId);


        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A returning partyAndCertificate: ".concat(partyAndCertificate.toString()));
        return partyAndCertificate;
    }
}
