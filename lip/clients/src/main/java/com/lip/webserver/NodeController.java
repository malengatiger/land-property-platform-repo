package com.lip.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/node")
public class NodeController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public NodeController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A NodeController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/flows", produces = "application/json")
    private String flows() {
        logger.info("/flows requested  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        List<String> flows = proxy.registeredFlows();
        for (String flow: flows) {
            logger.info("\uD83D\uDC9A  \uD83D\uDC9A " + flow + " \uD83C\uDF3A");
        }
        logger.info("\uD83C\uDF3A\uD83C\uDF3A " + flows.size() + " registered flows in BFN \uD83C\uDF3A");
        return GSON.toJson(flows);
    }
}
