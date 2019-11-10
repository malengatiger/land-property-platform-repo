package com.lip.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.webserver.data.LIPAccountDTO;
import com.lip.webserver.data.LandDTO;
import com.lip.webserver.util.DemoUtil;
import com.lip.webserver.util.FirebaseUtil;
import com.lip.webserver.util.WorkerBee;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/admin") // The paths for HTTP requests are relative to this base path.
public class AdminController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AdminController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A AdminController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return "\uD83D\uDC9A  BFNWebApi: AdminController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }
    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A AdminController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";

        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 " + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");
        PingResult pingResult = new PingResult(msg,proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A AdminController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        return GSON.toJson(pingResult);
    }
    @GetMapping(value = "/nodes", produces = "application/json")
    private String listNodes() {

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        StringBuilder sb = new StringBuilder();
        for (NodeInfo info: nodes) {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A BFN Corda Node: \uD83C\uDF3A " + info.getLegalIdentities().get(0).getName().toString());
            sb.append("Node: " + info.getLegalIdentities().get(0).getName().toString()).append("\n");
        }
        return GSON.toJson(new PingResult("List of Nodes",sb.toString()));
    }
    @GetMapping(value = "/notaries", produces = "application/json")
    private List<String> listNotaries() {

        List<Party> notaryIdentities = proxy.notaryIdentities();
        List<String> list = new ArrayList<>();
        for (Party info: notaryIdentities) {
            logger.info(" \uD83D\uDD35  \uD83D\uDD35 BFN Corda Notary: \uD83C\uDF3A " + info.getName().toString());
            list.add(info.getName().toString());
        }
        return list;
    }
    @GetMapping(value = "/flows", produces = "application/json")
    private List<String> listFlows() {

        logger.info("🥬 🥬 🥬 🥬 Registered Flows on Corda BFN ...  \uD83E\uDD6C ");
        List<String> flows = proxy.registeredFlows();
        int cnt = 0;
        for (String info: flows) {
            cnt++;
            logger.info("\uD83E\uDD4F \uD83E\uDD4F #$"+cnt+" \uD83E\uDD6C BFN Corda RegisteredFlow:  \uD83E\uDD4F" + info + "   \uD83C\uDF4E ");
        }

        logger.info("🥬 🥬 🥬 🥬 Total Registered Flows  \uD83C\uDF4E  " + cnt + "  \uD83C\uDF4E \uD83E\uDD6C ");
        return flows;
    }

    @GetMapping(value = "/addAccount", produces = "application/json")
    private LIPAccountDTO addAccount(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String cellphone,
            @RequestParam String password) throws Exception {

        logger.info("🥬 🥬 🥬 🥬 Register new LIP account ...  \uD83E\uDD6C ");
        LIPAccountDTO m = WorkerBee.addAccount(proxy,name, email,password,cellphone);
        //todo - remove this
        getAccounts();
        return m;
    }
    @GetMapping(value = "/getAccounts", produces = "application/json")
    private List<LIPAccountDTO> getAccounts() throws Exception {

        logger.info("🥬 🥬 🥬 🥬 get LIP accounts ...  \uD83E\uDD6C ");
        return WorkerBee.getLIPAccounts(proxy);
    }
    @GetMapping(value = "/getAccountInfos", produces = "application/json")
    private List<String> getAccountInfos() throws Exception {

        logger.info("🥬 🥬 🥬 🥬 get accountInfos ...  \uD83E\uDD6C ");
        return WorkerBee.getAccountInfos(proxy);
    }

    @GetMapping(value = "/getAllStates", produces = "application/json")
    private List<String> getAllStates() throws Exception {

        logger.info("🥬 🥬 🥬 🥬  getAllStates ...  \uD83E\uDD6C ");
        return WorkerBee.getAllStates(proxy);
    }
    @GetMapping(value = "/getLandStates", produces = "application/json")
    private List<LandDTO> getLandStates() throws Exception {

        logger.info("🥬 🥬 🥬 🥬  getLandStates ...  \uD83E\uDD6C ");
        List<LandDTO> list = WorkerBee.getLandStates(proxy);
        for (LandDTO m: list) {
            logger.info("🥬 🥬 🥬 🥬 ".concat(GSON.toJson(m)));
        }
        return list;
    }
    @GetMapping(value = "/demo", produces = "application/json")
    private String startDemoData(@RequestParam boolean recreate) throws Exception {

        logger.info("🥬 🥬 🥬 🥬  startDemoData ...  \uD83E\uDD6C ");
        List<LandDTO> list = FirebaseUtil.getLandParcels();
        if (list.isEmpty()) {
            throw new Exception("Generate LandStates first before running demo data");
        }
        logger.info("🥬 🥬 🥬 🥬  existing LandStates from Firestore" +
                " ...  \uD83E\uDD6C " + list.size() + " \uD83D\uDE21 \uD83D\uDE21 ");
        return DemoUtil.startDemoData(proxy, recreate);
    }

    @GetMapping(value = "/issueDemoTokens", produces = "application/json")
    private String issueDemoTokens() throws Exception {

        logger.info("🥬 🥬 🥬 🥬  issueTokens ...  \uD83E\uDD6C ");
        return DemoUtil.distributeTokens(proxy);
    }
    @GetMapping(value = "/issueTokens", produces = "application/json")
    private String issueTokens(
            @RequestParam long amount,
            @RequestParam String holderIdentifier,
            @RequestParam String tokenIdentifier,
            @RequestParam String landStateIdentifier
    ) throws Exception {

        logger.info("🥬 🥬 🥬 🥬  issueTokens ...  \uD83E\uDD6C ");
        return WorkerBee.issueTokens(proxy, amount,
                holderIdentifier, tokenIdentifier, landStateIdentifier);
    }

    private class PingResult {
        String message;
        String nodeInfo;

        PingResult(String message, String nodeInfo) {
            this.message = message;
            this.nodeInfo = nodeInfo;
        }
    }
}
