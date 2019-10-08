package com.lip.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.webserver.data.X500Name;
import com.lip.webserver.util.PingResult;
import com.lip.webserver.util.WorkerBee;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/land") // The paths for HTTP requests are relative to this base path.
public class LandAffairsController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(LandAffairsController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public LandAffairsController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A CustomerController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return " \uD83E\uDD6C  \uD83E\uDD6C LipWebApi: CustomerController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }

    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A CustomerController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";

        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 " + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");
        PingResult pingResult = new PingResult(msg, proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A CustomerController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        return GSON.toJson(pingResult);
    }

    @GetMapping(value = "/nodes", produces = "application/json")
    private String listNodes() {

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        StringBuilder sb = new StringBuilder();
        for (NodeInfo info : nodes) {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A BFN Corda Node: \uD83C\uDF3A " + info.getLegalIdentities().get(0).getName().toString());
            sb.append("Node: " + info.getLegalIdentities().get(0).getName().toString()).append("\n");
        }
        return GSON.toJson(new PingResult("List of Nodes", sb.toString()));
    }

    @GetMapping(value = "/listLandStates", produces = "application/json")
    private List<LandDTO> listLandStates() {

        List<LandDTO> list = WorkerBee.getLandStates(proxy);
        for (LandDTO m: list) {
            logger.info("\uD83D\uDD38\uD83D\uDD38\uD83D\uDD38 LAND PARCEL: \uD83D\uDD38\uD83D\uDD38\uD83D\uDD38 " + GSON.toJson(m));
        }
        return list;
    }

    Random random = new Random(System.currentTimeMillis());

    @PostMapping(value = "/startLandRegistrationFlow", produces = "application/json")
    private LandDTO startRegisterLandFlow(@RequestBody LandDTO land) throws Exception {

        if (land.getLandAffairsParty() == null) {
            land.setLandAffairsParty(new X500Name("DeptLandAffairs","Pretoria","ZA"));
        }
        if (land.getBnoParty() == null) {
            land.setBnoParty(new X500Name("LipNetworkOperator","Johannesburg","ZA"));
        }
        if (land.getBankParty() == null) {
            land.setBankParty(new X500Name("Bank","Johannesburg","ZA"));
        }
        if (land.getRegulatorParty() == null) {
            land.setRegulatorParty(new X500Name("Regulator","Johannesburg","ZA"));
        }
        if (land.getName() == null) {
            land.setName("Land: TempErf: " + System.currentTimeMillis());
        }
        if (land.getPolygon() == null || land.getPolygon().isEmpty() || land.getPolygon().size() < 3) {
            throw new Exception("invalid polygon");
        }
        logger.info("\uD83C\uDF4F .... startRegisterLandFlow ........... \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F LAND: " + GSON.toJson(land));
        try {
            return WorkerBee.addLand(proxy,land);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }


}
