package com.lip.webserver.util;

import com.google.cloud.firestore.Firestore;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.flows.admin.AccountRegistrationFlow;
import com.lip.flows.admin.LIPAccountRegistrationFlow;
import com.lip.flows.admin.ShareAccountInfoFlow;
import com.lip.flows.land.RegisterLandFlow;
import com.lip.flows.tokens.CreateLandTokenTypeFlow;
import com.lip.flows.tokens.DistributeLandTokenFlow;
import com.lip.states.Coordinates;
import com.lip.states.LIPAccountState;
import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.lip.webserver.data.*;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
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
import java.util.UUID;

import static com.lip.webserver.data.Constants.*;

public class WorkerBee {
    private final static Logger logger = LoggerFactory.getLogger(WorkerBee.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private static final Firestore db = FirestoreClient.getFirestore();
    private static CordaRPCOps mProxy;
    private static List<NodeInfo> nodes;

    private static Party getParty(String name) throws Exception {
        if (nodes == null || nodes.isEmpty())
            nodes = mProxy.networkMapSnapshot();
        Party party = null;
        for (NodeInfo info : nodes) {
            Party m = info.getLegalIdentities().get(0);
            if (m.getName().getOrganisation().contains(name)) {
                party = m;
            }
        }
        if (party == null) {
            throw new Exception("Party ".concat(name).concat(" not found"));
        }
        return party;
    }


    public static LandDTO addLand(CordaRPCOps proxy,
                                  LandDTO land, boolean addToFirestore) throws Exception {
        mProxy = proxy;
        logger.info("\uD83C\uDF4F .... startRegisterLandFlow ........... ");
        try {
            if (land.getValue() == 0) {
                throw new Exception("Amount cannot be zero");
            }
            if (land.getAreaInSquareMetres() == 0) {
                throw new Exception("AreaInSquareMetres cannot be zero");
            }
            Party landAffairsParty = getParty(LAND_AFFAIRS);
            Party regulatorParty = getParty(REGULATOR);
            Party bnoParty = getParty(BNO);
            Party bankParty = getParty(BANK);

            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: landAffairs: " + landAffairsParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: regulator: " + regulatorParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: networkOperator: " + bnoParty.getName().toString() + " \uD83E\uDD8B ");
            logger.info("\uD83C\uDF4F \uD83C\uDF4F ::: bank: " + bankParty.getName().toString() + " \uD83E\uDD8B ");

            List<Coordinates> coords = new ArrayList<>();
            for (CoordinatesDTO x : land.getPolygon()) {
                Coordinates coordinates = new Coordinates(x.getLatitude(), x.getLongitude(), x.getDateTime());
                coords.add(coordinates);
            }
            if (land.getDescription() == null) {
                land.setDescription("First Registration ".concat(new Date().toString()));
            }
            UUID uuid = UUID.randomUUID();
            if (land.getIdentifier() != null) {
                uuid = UUID.fromString(land.getIdentifier());
            }
            LandState state = new LandState(land.getName(), landAffairsParty, bnoParty,
                    regulatorParty, bankParty, coords, land.getImageURLs(), new Date(), land.getDescription(),
                    land.getValue(), land.getAreaInSquareMetres(), uuid);

            CordaFuture<FungibleToken> cordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterLandFlow.class, state).getReturnValue();

            FungibleToken fungibleToken = cordaFuture.get();
            logger.info("\uD83D\uDE21 \uD83D\uDE21 Land identifier :: \uD83D\uDE21 ".concat(uuid.toString())
                    .concat(" for: ").concat(state.getName()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C FungibleToken returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + fungibleToken.toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                    + " amount: " + land.getValue());

            LandDTO m = getDTO(state);
            logger.info("\uD83C\uDF4F \uD83C\uDF4F LandState and LandToken added to Corda: \uD83C\uDF4F ".concat(m.getName()));
            if (addToFirestore) {
                try {
                    logger.info("\uD83D\uDE21 \uD83D\uDE21 Adding land to Firestore: ".concat(state.getName()));
                    FirebaseUtil.addLand(m);
                } catch (Exception e) {
                    logger.error("\uD83D\uDC7F Failed to add LandState to Firestore: \uD83D\uDC7F "
                            + e.getMessage());
                }
            }
            return m;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }


    private static void createFirebaseUser(LIPAccountDTO account, String password) throws Exception {

        FirebaseUtil.createUser(account.getName(), account.getEmail(),
                password, account.getCellphone(), account.getIdentifier());
        logger.info("\uD83C\uDF1E \uD83C\uDF1E attempt to add user to Firebase");
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
            logger.info("\uD83D\uDD06 \uD83D\uDD06 start to share account with other nodes");
            List<NodeInfo> nodes = proxy.networkMapSnapshot();
            for (NodeInfo nodeInfo : nodes) {
                if (nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().contains("Notary")) {
                    continue;
                }
                startAccountSharingFlow(proxy, nodeInfo.getLegalIdentities().get(0),
                        accountInfo);
            }
            return dto;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }


    public static LandTokenDTO createLandTokenType(CordaRPCOps proxy,
                                                   String description,
                                                   String landStateIdentifier) throws Exception {
        //todo - fix queries OR bust!
        if (landStateIdentifier == null) {
            throw new Exception("landStateIdentifier not found");
        }
        //get landState
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<LandState> page = proxy.vaultQueryByCriteria(criteria, LandState.class);
        LandState landState = null;
        logger.info("\uD83C\uDF55 \uD83C\uDF55 landStateIdentifier: ".concat(landStateIdentifier));
        logger.info("\uD83C\uDF55 \uD83C\uDF55 LandStates found on Corda: " + page.getStates().size());
        for (StateAndRef<LandState> s : page.getStates()) {
            LandState x = s.getState().getData();
            logger.info("\uD83C\uDF55 \uD83C\uDF55 LandState: ".concat(x.getName()));
            String id = x.getIdentifier().toString();
            if (id.equalsIgnoreCase(landStateIdentifier)) {
                landState = s.getState().getData();
            }
        }
        if (landState == null) {
            throw new Exception("LandState not found");
        }
        if (description == null) {
            description = landState.getName();
        }
        logger.info("\uD83D\uDE21 \uD83D\uDE21 createLandTokenType: LandState found : "
                .concat(landState.getName()));
        Party me = proxy.nodeInfo().getLegalIdentities().get(0);
        LandToken token = new LandToken(description, new UniqueIdentifier(), landState);
        CordaFuture<FungibleToken> signedTransactionCordaFuture = proxy.startFlowDynamic(
                CreateLandTokenTypeFlow.class, landState, description).getReturnValue();

        FungibleToken fungibleToken = signedTransactionCordaFuture.get();
        logger.info((" \uD83D\uDCCC \uD83D\uDCCC FungibleToken::LandToken created on Corda; " +
                "\uD83C\uDF4E tx: ")
                .concat(fungibleToken.toString()));
        logger.warn(("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D" +
                " Fungible Token: ").concat(GSON.toJson(fungibleToken)));

        LandTokenDTO dto = getDTO(token);
        logger.info("\uD83D\uDE21 \uD83D\uDE21 LandToken Type: ".concat(GSON.toJson(dto))
                .concat(" ... \uD83D\uDE21 writing LandTokenDTO to Firestore"));
        FirebaseUtil.addToken(dto);
        return dto;
    }

    private static void startAccountSharingFlow(CordaRPCOps proxy,
                                                Party otherParty, AccountInfo account) throws Exception {
        try {
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
            if (ref.getState().getData() instanceof FungibleToken) {
                FungibleToken token = (FungibleToken) ref.getState().getData();
                list.add(token.toString());
                logger.info(("\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F " +
                        "FungibleToken: ").concat(token.toString()));
                logger.info("\uD83D\uDD06 TokenIdentifier: ".concat(token.getIssuedTokenType().getTokenIdentifier()));
                logger.info("\uD83D\uDD06 Token Amount: ".concat(String.valueOf(token.getAmount())));
            }
            if (ref.getState().getData() instanceof AccountInfo) {
                AccountInfo info = (AccountInfo) ref.getState().getData();
                String s1 = "\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F Account Info: " + info.getName().concat(" \uD83C\uDF4E ")
                        .concat(info.getIdentifier().getId().toString());
                list.add(s1);
                logger.info(s1);
            }
            if (ref.getState().getData() instanceof LIPAccountState) {
                LIPAccountState info = (LIPAccountState) ref.getState().getData();
                String s1 = "\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D LIPAccount: " + info.getName().concat(" \uD83D\uDCA0 ")
                        .concat(info.getCellphone())
                        .concat(" \uD83D\uDCA0 ").concat(info.getEmail());
                String keys = "\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 PartyAndCertificate name: ".concat(info.getPartyAndCertificate().getName().toString()
                        .concat("   \uD83D\uDD35 PartyAndCertificate Key: ").concat(info.getPartyAndCertificate().getParty().getOwningKey().getEncoded().toString()
                                .concat("  \uD83D\uDC7D accountInfo Key: ").concat(info.getAccountInfo().getParticipants().get(0).getOwningKey().getEncoded().toString())));
                list.add(s1);
                logger.info(s1);
                logger.info(keys);
            }
            if (ref.getState().getData() instanceof LandState) {
                LandState info = (LandState) ref.getState().getData();
                String s1 = "\uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A Land Parcel Name: "
                        + info.getName().concat(" ")
                        .concat(" polygon points: " + info.getPolygon().size())
                        .concat(" value:  \uD83C\uDF4E ").concat("" + info.getValue() + "  \uD83C\uDF4E "
                                .concat(" AreaInSquareMetres: " + info.getAreaInSquareMetres()));
                list.add(s1);
                logger.info(s1);
            }
            if (ref.getState().getData() instanceof LandToken) {
                LandToken landToken = (LandToken) ref.getState().getData();
                String s1 = "\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Land Token Type: "
                        + landToken.getDescription().concat(" ")
                        .concat(" :  \uD83C\uDF4E ").concat(" LandState: " + landToken.getLandState().getName()
                                + "  \uD83C\uDF4E ");
                list.add(s1);
                logger.info(s1);
                logger.info(landToken.toString());
            }

        }
        logger.info(" \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 States found: "
                + list.size());
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

    public static String distributeTokens(CordaRPCOps proxy,
                                          long amount,
                                          String tokenIdentifier,
                                          String holderIdentifier) throws Exception {
        //get token
        UUID uuid = UUID.fromString(tokenIdentifier);
        //construct the query criteria
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED);
        // grab the token type off the ledger which was created using CreateEvolvableTokens flow
        Vault.Page<LandToken> page = proxy.vaultQueryByCriteria(queryCriteria,LandToken.class);
        if (page.getStates().isEmpty()) {
            throw new IllegalStateException("\uD83D\uDC7F LandToken type not found on DLT");
        } else {
            logger.info(" \uD83D\uDC7D \uD83D\uDC7D  We cool with LandToken !  \uD83D\uDC7D \uD83D\uDC7D ");
        }
        LandToken landToken = page.getStates().get(0).getState().getData();
        logger.info("\n\n\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F LandToken type to be distributed: \n"
                .concat(landToken.toString())
                .concat("\uD83D\uDD06 LinearId: ").concat(landToken.getLinearId().getId().toString()));

        //get lipAccountState
        QueryCriteria criteriaB = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<LIPAccountState> page2 = proxy.vaultQueryByCriteria(criteriaB, LIPAccountState.class);
        LIPAccountState lipAccountState = null;
        for (StateAndRef<LIPAccountState> s : page2.getStates()) {
            String id = s.getState().getData().getAccountInfo().getIdentifier().getId().toString();
            if (id.equalsIgnoreCase(holderIdentifier)) {
                lipAccountState = s.getState().getData();
            }
        }
        if (lipAccountState == null) {
            throw new Exception("\uD83D\uDC7F LIPAccountState not found");
        }
        if (lipAccountState.getPartyAndCertificate() == null) {
            throw new Exception("\uD83D\uDC7F LIPAccountState is missing PartyAndCertificate");
        }
        logger.info("\n\n\uD83C\uDF4E  \uD83C\uDF4E lipAccountState: \n"
                .concat(lipAccountState.toString())
        .concat("  \uD83D\uDDF3 \uD83D\uDDF3 \uD83D\uDDF3 \n\uD83C\uDF4E  \uD83C\uDF4E " +
                "\n .... Start DistributeLandTokenFlow ...."));

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                DistributeLandTokenFlow.class,
                tokenIdentifier, amount, lipAccountState)
                .getReturnValue();

        SignedTransaction issueTx = signedTransactionCordaFuture.get();
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                "YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E "
                + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                + " tokens distributed: " + amount);
        String result =
                "\uD83C\uDF3F \uD83C\uDF3F  distributeTokens completed OK";
        logger.info(result);
        return result;
    }

    public static String issueTokens(CordaRPCOps proxy,
                                     long amount,
                                     String holderIdentifier,
                                     String tokenIdentifier,
                                     String landStateIdentifier) throws Exception {

        logger.info("\n\n\uD83C\uDF4F \uD83C\uDF4F .... issueTokens with the following parameters: "
                .concat(" \n\uD83C\uDF4F holderIdentifier: ").concat(holderIdentifier)
                .concat(" \n\uD83C\uDF4F tokenIdentifier: ").concat(tokenIdentifier)
                .concat(" \n\uD83C\uDF4F landStateIdentifier: ").concat(landStateIdentifier)
                .concat(" \n\uD83C\uDF4F amount: " + amount));
        try {
            //todo - fix queries - find out how to get states by an landStateIdentifier: IMPORTANT
            //get landState
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<LandState> page = proxy.vaultQueryByCriteria(criteria, LandState.class);
            LandState landState = null;
            for (StateAndRef<LandState> s : page.getStates()) {
                String id = s.getState().getData().getIdentifier().toString();
                if (id.equalsIgnoreCase(landStateIdentifier)) {
                    landState = s.getState().getData();
                }
            }
            if (landState == null) {
                throw new Exception("\uD83D\uDC7F LandState not found");
            }
            //get lipAccountState
            QueryCriteria criteriaB = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<LIPAccountState> page2 = proxy.vaultQueryByCriteria(criteriaB, LIPAccountState.class);
            LIPAccountState lipAccountState = null;
            for (StateAndRef<LIPAccountState> s : page2.getStates()) {
                String id = s.getState().getData().getAccountInfo().getIdentifier().getId().toString();
                if (id.equalsIgnoreCase(holderIdentifier)) {
                    lipAccountState = s.getState().getData();
                }
            }
            if (lipAccountState == null) {
                throw new Exception("\uD83D\uDC7F LIPAccountState not found");
            }
            if (lipAccountState.getPartyAndCertificate() == null) {
                throw new Exception("\uD83D\uDC7F LIPAccountState is missing PartyAndCertificate");
            }

            logger.info("\uD83C\uDF4F \uD83C\uDF4F Ready to issue LandTokens::: token holder name: "
                    + lipAccountState.getAccountInfo().getName()
                    + " \uD83E\uDD8B tokens: \uD83D\uDE21 " + amount + " \uD83D\uDE21 ");

            Long longAmt = amount;
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                    DistributeLandTokenFlow.class,
                    tokenIdentifier, longAmt, lipAccountState.getPartyAndCertificate().getParty())
                    .getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC4C " +
                    "YEBO!! \uD83D\uDC4C signedTransaction returned: \uD83C\uDF4F \uD83C\uDF4E "
                    + issueTx.getId().toString() + " \uD83C\uDF4F \uD83C\uDF4E"
                    + " tokens issued: " + amount);
            String result =
                    "\uD83C\uDF3F \uD83C\uDF3F  issueTokens completed OK";
            logger.info(result);
            return issueTx.getId().toString();
        } catch (Exception e) {
            logger.error("\uD83D\uDC7F \uD83D\uDC7F \uD83D\uDC7F " +
                    "IssueTokens flow fell down. BOOO!!!");
            logger.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    private static LandDTO getDTO(LandState s) {
        LandDTO m = new LandDTO();
        m.setName(s.getName());
        m.setValue(s.getValue());
        m.setDescription(s.getDescription());
        m.setImageURLs(s.getImageURLs());
        m.setAreaInSquareMetres(s.getAreaInSquareMetres());
        m.setDateRegistered(s.getDateRegistered().toString());
        m.setIdentifier(s.getIdentifier().toString());
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
        for (Coordinates x : s.getPolygon()) {
            m.getPolygon().add(new CoordinatesDTO(x.getLatitude(), x.getLongitude()));
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
        logger.info(" \uD83C\uDFB2  \uD83C\uDFB2 #### getDTO: ".concat(GSON.toJson(m)));
        return m;
    }

    private static LandTokenDTO getDTO(LandToken t) {
        LandTokenDTO m = new LandTokenDTO();
        m.setDescription(t.getDescription());
        m.setLandStateIdentifier(t.getLandState().getIdentifier().toString());
        m.setLinearId(t.getLinearId().getId().toString());

        return m;
    }

}
