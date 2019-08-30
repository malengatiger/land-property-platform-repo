package com.lip.flows.bno;

import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CordaService
public class MyCordaService extends SingletonSerializeAsToken {
    private final static Logger logger = LoggerFactory.getLogger(MyCordaService.class);
    private AppServiceHub serviceHub;
    public MyCordaService(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
        logger.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 MyCordaService Constructor. \uD83D\uDCA6 Read up on service usage  \uD83D\uDE21");
    }
    // public api of service
    public String getInfo() {
        logger.info(" \uD83D\uDC2C  \uD83D\uDC2C This node legal identity:  \uD83D\uDC2C " + serviceHub.getMyInfo().getLegalIdentities().get(0).getName().toString());
        logger.info(" \uD83D\uDC2C  \uD83D\uDC2C NetworkParameters:  \uD83D\uDC2C " + serviceHub.getNetworkParameters().toString());
        return serviceHub.getNetworkParameters().toString();
    }
}
