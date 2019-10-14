package com.lip.flows.admin;

import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CordaService
public class LIPCordaService extends SingletonSerializeAsToken {
    private final static Logger logger = LoggerFactory.getLogger(LIPCordaService.class);
    private AppServiceHub serviceHub;
    public LIPCordaService(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
        logger.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 BFNCordaService Constructor. \uD83D\uDCA6 Read up on service usage  \uD83D\uDE21");
        getInfo();
    }
    // public api of service
    public String getInfo() {
        logger.info(" \uD83D\uDC2C  \uD83D\uDC2C This node legal identity:  \uD83D\uDC2C " + serviceHub.getMyInfo().getLegalIdentities().get(0).getName().toString());
        logger.info(" \uD83D\uDC2C  \uD83D\uDC2C NetworkParameters:  \uD83D\uDC2C " + serviceHub.getNetworkParameters().toString());

        return serviceHub.getNetworkParameters().toString();
    }
}
/*
BFNCordaService
 */
