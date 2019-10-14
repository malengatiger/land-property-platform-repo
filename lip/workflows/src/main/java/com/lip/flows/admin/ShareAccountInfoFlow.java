package com.lip.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import kotlin.Unit;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@StartableByRPC
public class ShareAccountInfoFlow extends FlowLogic<String> {
    private final static Logger logger = LoggerFactory.getLogger(ShareAccountInfoFlow.class);
    private final Party otherParty;
    private final AccountInfo account;

    public ShareAccountInfoFlow(Party otherParty, AccountInfo account) {
        this.otherParty = otherParty;
        this.account = account;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {

        ServiceHub hub = getServiceHub();
        AccountService accountService = hub.cordaService(KeyManagementBackedAccountService.class);
        try {
            CompletableFuture<Unit> future = accountService.shareAccountInfoWithParty(
                    account.getIdentifier().getId(), otherParty).toCompletableFuture();
            Unit result = future.get();
            if (result != null) {
                logger.info(" \uD83D\uDE0E \uD83D\uDE0E "+account.getName()
                        +" shared with \uD83D\uDC7F " +
                        otherParty.getName().getOrganisation());
            }

        } catch (InterruptedException e) {
            logger.error("\uD83D\uDC7F \uD83D\uDC7F InterruptedException: ".concat(e.getMessage()));
            throw new FlowException("InterruptedException: Unable to share account");
        } catch (ExecutionException e) {
            logger.error("\uD83D\uDC7F \uD83D\uDC7F ExecutionException: ".concat(e.getMessage()));
            throw new FlowException("ExecutionException: Unable to share account");
        }

        return "\uD83C\uDFC8 \uD83C\uDFC8 Account shared: "
                + account.getName() + " with " + otherParty.getName().getOrganisation();
    }
}
