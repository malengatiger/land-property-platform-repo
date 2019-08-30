package com.lip.webserver;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.states.Coordinates;
import com.lip.states.LandState;
import com.lip.webserver.util.FlowResult;
import com.lip.webserver.util.PingResult;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

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
        PingResult pingResult = new PingResult(msg,proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A CustomerController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

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
    @GetMapping(value = "/landStates", produces = "application/json")
    private String listLandStates() {

        Vault.Page<LandState> states = proxy.vaultQuery(LandState.class);
        StringBuilder sb = new StringBuilder();
        List<StateAndRef<LandState>> list = states.getStates();
        logger.info("\uD83C\uDF3A \uD83C\uDF3A vaultQuery for LandState: found \uD83E\uDD18 " + list.size() + " \uD83E\uDD18");
        int cnt = 0;
        for (StateAndRef<LandState> info: list) {
            cnt++;
            logger.info("\uD83C\uDF3A \uD83C\uDF3A LandState #"+cnt+": name:  \uD83D\uDC9A " + info.getState().getData().getName() + "  \uD83D\uDC9A value: " + info.getState().getData().getOriginalValue());
        }
        return GSON.toJson(new PingResult("Number of LandStates: ", "" + list.size()));
    }
    Random random = new Random(System.currentTimeMillis());
    @GetMapping(value = "/startRegisterLandFlow", produces = "application/json")
    private String startRegisterLandFlow() throws ExecutionException, InterruptedException {

        logger.info("\uD83C\uDF4F .... startRegisterLandFlow ........... ");
        try {
            CordaX500Name name = new CordaX500Name("DeptLandAffairs","Pretoria","ZA");
            Party landAffairsParty = proxy.wellKnownPartyFromX500Name(name);
            CordaX500Name name2 = new CordaX500Name("Regulator","Johannesburg","ZA");
            Party regulatorParty = proxy.wellKnownPartyFromX500Name(name2);
            CordaX500Name name3 = new CordaX500Name("LipNetworkOperator","Johannesburg","ZA");
            Party bnoParty = proxy.wellKnownPartyFromX500Name(name3);

            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: landAffairs: " + landAffairsParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: regulator: " + regulatorParty.getName().toString()  + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: networkOperator: " + bnoParty.getName().toString()  + " \uD83E\uDD8B ");

            int num = random.nextInt(100);
            List<Coordinates> polygon = new ArrayList<>();
            double amount = num * 9000600.23;

            polygon.add(new Coordinates(-25.15525,27.2635));
            polygon.add(new Coordinates(-25.15525,27.2635));
            polygon.add(new Coordinates(-25.15525,27.2635));
            polygon.add(new Coordinates(-25.15525,27.2635));
            
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(RegisterLandFlow.class, "Erf " + System.currentTimeMillis(),
                    landAffairsParty,bnoParty,regulatorParty,polygon, new Date(),"Piece of Land: " + new Date().toString(), amount).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E " + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
            + " amount: " + amount);
            return GSON.toJson(new FlowResult(issueTx.getId().toString()," \uD83C\uDF3F  \uD83C\uDF3F  startRegisterLandFlow completed OK"));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }


}
