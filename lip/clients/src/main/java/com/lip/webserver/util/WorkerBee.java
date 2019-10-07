package com.lip.webserver.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.states.Coordinates;
import com.lip.states.LandState;
import com.lip.webserver.LandDTO;
import com.lip.webserver.data.CoordinatesDTO;
import com.lip.webserver.data.X500Name;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkerBee {
    private final static Logger logger = LoggerFactory.getLogger(WorkerBee.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static LandDTO addLand(CordaRPCOps proxy, LandDTO land) throws Exception {

        logger.info("\uD83C\uDF4F .... startRegisterLandFlow ........... ");
        try {
            List<NodeInfo> nodes = proxy.networkMapSnapshot();
            for (NodeInfo info: nodes) {
                logger.info("\uD83C\uDF81 \uD83C\uDF81 Corda Node: ".concat(info.getLegalIdentities().get(0).getName().toString()));
            }
            CordaX500Name name = new CordaX500Name(land.getLandAffairsParty().getOrganisation(),
                    land.getLandAffairsParty().getLocality(), land.getLandAffairsParty().getCountry());
            Party landAffairsParty = proxy.wellKnownPartyFromX500Name(name);

            CordaX500Name name2 = new CordaX500Name(land.getRegulatorParty().getOrganisation(),
                    land.getRegulatorParty().getLocality(), land.getRegulatorParty().getCountry());
            Party regulatorParty = proxy.wellKnownPartyFromX500Name(name2);

            CordaX500Name name3 = new CordaX500Name(land.getBnoParty().getOrganisation(),
                    land.getBnoParty().getLocality(), land.getBnoParty().getCountry());
            Party bnoParty = proxy.wellKnownPartyFromX500Name(name3);

            CordaX500Name name4 = new CordaX500Name(land.getBankParty().getOrganisation(),
                    land.getBankParty().getLocality(), land.getBankParty().getCountry());
            logger.info("bank x500: ".concat(name4.toString()));
            Party bankParty = proxy.wellKnownPartyFromX500Name(name4);

            if (bnoParty == null) {
                throw new Exception("bnoParty party missing");
            }
            if (regulatorParty == null) {
                throw new Exception("regulatorParty party missing");
            }
            if (landAffairsParty == null) {
                throw new Exception("LandAffairs party missing");
            }
            if (bankParty == null) {
                throw new Exception("Bank party missing");
            }

            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: landAffairs: " + landAffairsParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: regulator: " + regulatorParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: networkOperator: " + bnoParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: bank: " + bankParty.getName().toString() + " \uD83E\uDD8B ");

            List<Coordinates> coords = new ArrayList<>();
            for (CoordinatesDTO x: land.getPolygon()) {
                Coordinates coordinates = new Coordinates(x.getLatitude(),x.getLongitude());
                coords.add(coordinates);
            }
            LandState state = new LandState(land.getName(),landAffairsParty,bnoParty,
                    regulatorParty,bankParty,coords, land.getImageURLs(),new Date(),land.getDescription(),
                    land.getOriginalValue());

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterLandFlow.class, state).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                    + " amount: " + land.getOriginalValue());
            FlowResult result = new FlowResult(issueTx.getId().toString(), "\uD83C\uDF3F \uD83C\uDF3F  startRegisterLandFlow completed OK");
            logger.info(GSON.toJson(result));
            return getDTO(state);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }
    private static LandDTO getDTO(LandState s) {
        LandDTO m = new LandDTO();
        m.setName(s.getName());
        m.setOriginalValue(s.getOriginalValue());
        m.setDescription(s.getDescription());
        m.setImageURLs(s.getImageURLs());
        m.setDateRegistered(s.getDateRegistered());
        m.setBankParty(new X500Name(
                s.getBankParty().getName().getOrganisation(),
                s.getBankParty().getName().getLocality(),
                s.getBankParty().getName().getCountry()));
        m.setBnoParty(new X500Name(
                s.getBnoParty().getName().getOrganisation(),
                s.getBnoParty().getName().getLocality(),
                s.getBnoParty().getName().getCountry()));
        m.setLandAffairsParty(new X500Name(
                s.getLandAffairsParty().getName().getOrganisation(),
                s.getLandAffairsParty().getName().getLocality(),
                s.getLandAffairsParty().getName().getCountry()));
        m.setRegulatorParty(new X500Name(
                s.getRegulatorParty().getName().getOrganisation(),
                s.getRegulatorParty().getName().getLocality(),
                s.getRegulatorParty().getName().getCountry()));
        m.setPolygon(new ArrayList<>());
        for (Coordinates x: s.getPolygon()) {
            m.getPolygon().add(new CoordinatesDTO(x.getLatitude(),x.getLongitude()));
        }
        return m;
    }

    public static List<LandDTO> getLandStates(CordaRPCOps proxy) {
        logger.info("........................ getLandStates:  \uD83D\uDC9A ");
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<LandState> page = proxy.vaultQueryByWithPagingSpec(
                LandState.class, criteria,
                new PageSpecification(1, 200));
        List<LandDTO> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 " +
                "Total land states found: " + page.getStates().size());

        for (StateAndRef<LandState> ref : page.getStates()) {
            LandDTO m = getDTO(ref.getState().getData());
            list.add(m);
            logger.info(GSON.toJson(m));
        }
        return list;
    }

}
