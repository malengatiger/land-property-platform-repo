package com.lip.webserver.util;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.flows.admin.AccountRegistrationFlow;
import com.lip.flows.admin.LIPAccountRegistrationFlow;
import com.lip.flows.admin.ShareAccountInfoFlow;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.flows.tokens.CreateLandTokenTypeFlow;
import com.lip.flows.tokens.IssueLandTokenFlow;
import com.lip.states.Coordinates;
import com.lip.states.LIPAccountState;
import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.lip.webserver.LandDTO;
import com.lip.webserver.data.CoordinatesDTO;
import com.lip.webserver.data.LIPAccountDTO;
import com.lip.webserver.data.X500Name;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WorkerBee {
    private final static Logger logger = LoggerFactory.getLogger(WorkerBee.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private  static final Firestore db = FirestoreClient.getFirestore();
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
                    new BigDecimal(land.getValue()), UUID.randomUUID());

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterLandFlow.class, state).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                    + " amount: " + land.getValue());
            FlowResult result = new FlowResult(issueTx.getId().toString(), "\uD83C\uDF3F \uD83C\uDF3F  startRegisterLandFlow completed OK");
            logger.info(GSON.toJson(result));
            return getDTO(state);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }


    private static void createFirebaseUser(LIPAccountDTO account, String password) throws Exception {

        FirebaseUtil.createUser(account.getName(),account.getEmail(),
                password,account.getCellphone(), account.getIdentifier());
        FirebaseUtil.addUserToDatabase(account);
    }
    public static LIPAccountDTO addAccount(CordaRPCOps proxy,
                                           String name,
                                           String email,
                                           String password,
                                           String cellphone) throws Exception {

        logger.info("\uD83C\uDF4F .... addAccount: starting AccountRegistrationFlow ........... ");
        try {
            CordaFuture<AccountInfo> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    AccountRegistrationFlow.class, name).getReturnValue();

            AccountInfo accountInfo = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C accountInfo returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + accountInfo.getName() + " \uD83C\uDF4F \uD83C\uDF4E ... start LIPAccountRegistrationFlow ...");

            CordaFuture<LIPAccountState> future = proxy.startTrackedFlowDynamic(
                    LIPAccountRegistrationFlow.class, accountInfo, email, cellphone, name).getReturnValue();
            LIPAccountState lipAccountState = future.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C lipAccountState returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + lipAccountState.getAccountInfo().getName() + " \uD83C\uDF4F \uD83C\uDF4E");

            LIPAccountDTO dto = getDTO(lipAccountState);
            createFirebaseUser(dto, password);
            logger.info(GSON.toJson(dto));
            List<NodeInfo> nodes = proxy.networkMapSnapshot();
            for (NodeInfo nodeInfo: nodes) {
                if (nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().contains("Notary")) {
                    continue;
                }
                startAccountSharingFlow(proxy,nodeInfo.getLegalIdentities().get(0),
                        accountInfo);
            }
            return dto;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }

    public static LandToken createLandTokenType(CordaRPCOps proxy,
                                                String description,
                                                String landStateIdentifier) throws Exception {
        //todo - fix queries OR bust!
        //get landState
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<LandState> page = proxy.vaultQueryByCriteria(criteria,LandState.class);
        LandState landState = null;
        for (StateAndRef<LandState> s: page.getStates()) {
            String id = s.getState().getData().getIdentifier().toString();
            if (id.equalsIgnoreCase(landStateIdentifier)) {
                landState = s.getState().getData();
            }
        }
        if (landState == null) {
            throw new Exception("LandState not found");
        }
        Party me = proxy.nodeInfo().getLegalIdentities().get(0);
        LandToken token = new LandToken(description,me,new UniqueIdentifier(),landState);

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                CreateLandTokenTypeFlow.class, landState, description).getReturnValue();

        SignedTransaction issueTx = signedTransactionCordaFuture.get();
        logger.info(" \uD83D\uDCCC \uD83D\uDCCC LandToken created on Corda; \uD83C\uDF4E tx: "
        .concat(issueTx.getId().toString()));
        FirebaseUtil.addToken(token);

        return token;
    }
    private static void startAccountSharingFlow(CordaRPCOps proxy,
                                                Party otherParty, AccountInfo account) throws Exception {
        try {
            logger.info(" \uD83C\uDFBD  \uD83C\uDFBD Sharing account: ".concat(account.getName().concat(" with: ")
            .concat(otherParty.getName().toString())));
            CordaFuture<String> accountInfoCordaFuture = proxy.startFlowDynamic(
                    ShareAccountInfoFlow.class, otherParty, account).getReturnValue();
            String result = accountInfoCordaFuture.get();
            logger.info(result);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
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

    public static List<String> getAccountInfos(CordaRPCOps proxy) {
        logger.info("........................ getAccountInfos:  \uD83D\uDC9A ");
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Vault.Page<AccountInfo> page = proxy.vaultQueryByWithPagingSpec(
                AccountInfo.class, criteria,
                new PageSpecification(1, 200));
        List<String> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 " +
                "Total AccountInfos states found: " + page.getStates().size());

        for (StateAndRef<AccountInfo> ref : page.getStates()) {
            list.add(ref.getState().getData().getName());
            logger.info("☎️ AccountInfo: ".concat(ref.getState().getData().getName()));
        }
        logger.info(" \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 AccountInfos found: " + list.size());
        return list;
    }
    public static List<String> getAllStates(CordaRPCOps proxy) {
        logger.info("........................ getAllStates:  \uD83D\uDC9A ");
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Vault.Page<ContractState> page = proxy.vaultQueryByWithPagingSpec(
                ContractState.class, criteria,
                new PageSpecification(1, 200));
        List<String> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 " +
                "Total states found: " + page.getStates().size());

        for (StateAndRef<ContractState> ref : page.getStates()) {
            list.add(ref.getState().getData().getClass().getName());
            logger.info("☎️  ContractState class: ".concat(ref.getState().getData().getClass().getName()));
        }
        logger.info(" \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 States found: " + list.size());
        return list;
    }
    public static List<LIPAccountDTO> getLIPAccounts(CordaRPCOps proxy) {
        logger.info("........................ getLIPAccounts:  \uD83D\uDC9A ");
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Vault.Page<LIPAccountState> page = proxy.vaultQueryByWithPagingSpec(
                LIPAccountState.class, criteria,
                new PageSpecification(1, 200));
        List<LIPAccountDTO> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 " +
                "Total LIPAccount states found: " + page.getStates().size());

        for (StateAndRef<LIPAccountState> ref : page.getStates()) {
            LIPAccountDTO m = getDTO(ref.getState().getData());
            list.add(m);
            logger.info(GSON.toJson(m));
        }
        logger.info(" \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 LIPAccounts found: " + list.size());
        return list;
    }
    public static String issueTokens(CordaRPCOps proxy, long amount,
                                     String holderIdentifier,
                                     String tokenIdentifier, String landStateIdentifier) throws Exception {

        logger.info("\uD83C\uDF4F .... issueTokens ........... ");
        try {
            //todo - fix queries - find out how to get states by an landStateIdentifier: IMPORTANT
            //get landState
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<LandState> page = proxy.vaultQueryByCriteria(criteria,LandState.class);
            LandState landState = null;
            for (StateAndRef<LandState> s: page.getStates()) {
                String id = s.getState().getData().getIdentifier().toString();
                if (id.equalsIgnoreCase(landStateIdentifier)) {
                    landState = s.getState().getData();
                }
            }
            if (landState == null) {
                throw new Exception("LandState not found");
            }
            //get lipAccountState
            QueryCriteria criteriaB = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<LIPAccountState> page2 = proxy.vaultQueryByCriteria(criteriaB,LIPAccountState.class);
            LIPAccountState lipAccountState = null;
            for (StateAndRef<LIPAccountState> s: page2.getStates()) {
                String id = s.getState().getData().getAccountInfo().getIdentifier().getId().toString();
                if (id.equalsIgnoreCase(holderIdentifier)) {
                    lipAccountState = s.getState().getData();
                }
            }
            if (lipAccountState == null) {
                throw new Exception("LIPAccountState not found");
            }

            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: token holderIdentifier: "
                    + lipAccountState.getAccountInfo().getName()
                    + " \uD83E\uDD8B tokens: " + amount);

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                    IssueLandTokenFlow.class, tokenIdentifier, amount, holderIdentifier).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                    + " tokens issued: " + amount);
            FlowResult result = new FlowResult(issueTx.getId().toString(),
                    "\uD83C\uDF3F \uD83C\uDF3F  issueTokens completed OK");
            logger.info(GSON.toJson(result));
            return issueTx.getId().toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }

    private static LandDTO getDTO(LandState s) {
        LandDTO m = new LandDTO();
        m.setName(s.getName());
        m.setValue(s.getValue().doubleValue());
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

    private static LIPAccountDTO getDTO(LIPAccountState s) {
        LIPAccountDTO m = new LIPAccountDTO();
        m.setAccountInfoName(new X500Name(
                s.getAccountInfo().getHost().getName().getOrganisation(),
                s.getAccountInfo().getHost().getName().getLocality(),
                s.getAccountInfo().getHost().getName().getCountry()));
        m.setIdentifier(s.getAccountInfo().getIdentifier().getId().toString());
        m.setLipAccountName(new X500Name(
                s.getPartyAndCertificate().getName().getOrganisation(),
                s.getPartyAndCertificate().getName().getLocality(),
                s.getPartyAndCertificate().getName().getCountry()));
        m.setName(s.getAccountInfo().getName());
        m.setEmail(s.getEmail());
        m.setCellphone(s.getCellphone());
        logger.info(" \uD83C\uDFB2  \uD83C\uDFB2 getDTO: ".concat(GSON.toJson(m)));
        return m;
    }

}
