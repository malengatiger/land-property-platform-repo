package com.lip.webserver.util;

import com.lip.webserver.data.LIPAccountDTO;
import com.lip.webserver.data.LandDTO;
import com.lip.webserver.data.LandTokenDTO;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DemoUtil {
    private final static Logger logger = LoggerFactory.getLogger(DemoUtil.class);
    private static final int NUMBER_OF_ACCOUNTS = 25, NUMBER_OF_TOKEN_HOLDERS = 5;

    public static String startDemoData(CordaRPCOps proxy, boolean recreateLandParcels) throws Exception {

        logger.info("\n\n\uD83D\uDC24 \uD83D\uDC24 \uD83D\uDC24 DemoUtil starting, cleaning up Firebase ...");
        FirebaseUtil.deleteUsers();
        FirebaseUtil.deleteCollection("accounts");
        FirebaseUtil.deleteCollection("tokenTypes");

        if (recreateLandParcels)
            recreateLandParcelsOnCorda(proxy);

        logger.info("\nDemoUtil generating accounts  \uD83D\uDCA7 \uD83D\uDCA7 \uD83D\uDCA7...");
        HashMap<String, String> names = new HashMap<>();
        for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
            String name = getRandomName();
            names.put(name, name);
        }
        logger.info("randomized \uD83D\uDD06 \uD83D\uDD06 " + names.size() + " names for accounts");
        int cnt = 0;
        for (String name : names.values()) {
            try {
                WorkerBee.addAccount(proxy,
                        name,
                        getEmail(),
                        "pass123",
                        getPhone());
                cnt++;
            } catch (Exception e) {
                logger.error(" \uD83D\uDC7F  \uD83D\uDC7F Add account failed ");
                logger.error(e.getMessage());
            }
        }

        logger.info("\uD83D\uDD06 \uD83D\uDD06 DemoUtil done generating accounts; generated: " +
                "\uD83C\uDF45 " + cnt + " accounts \uD83C\uDF45 will start token issue ...");

//        String m = issueTokens(proxy);
        String msg = "\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                "Demo Data generation COMPLETE!\n";
        return msg;
    }

    private static void recreateLandParcelsOnCorda(CordaRPCOps proxy) throws Exception {
        List<LandDTO> list = FirebaseUtil.getLandParcels();
        int cnt = 0;
        for (LandDTO m : list) {
            WorkerBee.addLand(proxy, m, false);
            cnt++;
        }
        logger.info("\n\uD83C\uDF38 \uD83C\uDF38 Land Parcels recreated: " +
                "\uD83E\uDD66 " + cnt + " \uD83E\uDD66 ");
    }

    public static String distributeTokens(CordaRPCOps proxy) throws Exception {
        logger.info("\n\n\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35" +
                "Start issuing tokens for land parcels ...");
        List<LIPAccountDTO> lipAccounts = FirebaseUtil.getAccounts();
        List<LandDTO> lands = FirebaseUtil.getLandParcels();

        for (LandDTO land : lands) {
            int numberOfHolders = random.nextInt(NUMBER_OF_TOKEN_HOLDERS);
            if (numberOfHolders == 0) numberOfHolders = 1;
            logger.info(" \uD83C\uDF1E \uD83C\uDF1E ".concat(land.getName())
                    .concat(" to be split among \uD83C\uDF1E " + numberOfHolders) + " \uD83C\uDF1E "
                    .concat(" tokens: " + land.getValue()));
            splitTokensAmongAccounts(proxy, land, numberOfHolders, lipAccounts);
        }
        String msg = "\uD83D\uDC4C\uD83C\uDFFD \uD83D\uDC4C\uD83C\uDFFD Tokens issued to all accounts selected";
        logger.info(msg);
        return msg;
    }

    private static void splitTokensAmongAccounts(CordaRPCOps proxy, LandDTO land,
                                                 int numberOfHolders,
                                                 List<LIPAccountDTO> lipAccounts) throws Exception {
        HashMap<String, LIPAccountDTO> map = new HashMap<>();

        while (map.size() < numberOfHolders) {
            int index = random.nextInt(lipAccounts.size() - 1);
            LIPAccountDTO dto = lipAccounts.get(index);
            if (!map.containsKey(dto.getIdentifier())) {
                map.put(dto.getIdentifier(), dto);
            }
        }
        logger.info("\uD83E\uDDE9 splitTokensAmongAccounts \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 " + map.size());
        LandTokenDTO token = FirebaseUtil.getTokenByName(land.getName());
        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 LandToken created. YEBO! \uD83E\uDDA0 "
                .concat(token.getDescription()));
        Collection<LIPAccountDTO> list = map.values();
        long eachSplit = land.getValue() / numberOfHolders;
        for (LIPAccountDTO accountDTO : list) {
            grantTokensToAccount(proxy, accountDTO, eachSplit, token, land);
        }

    }

    private static void grantTokensToAccount(CordaRPCOps proxy,
                                             LIPAccountDTO account,
                                             long amount,
                                             LandTokenDTO landToken,
                                             LandDTO land) throws Exception {

        logger.info("\uD83D\uDC4C\uD83C\uDFFD \uD83D\uDC4C\uD83C\uDFFD grantTokensToAccount: "
                .concat("\uD83C\uDF4E " + amount).concat(" \uD83C\uDF4E per holder"));

        WorkerBee.issueTokens(proxy, amount, account.getIdentifier(),
                landToken.getLinearId(), land.getIdentifier());

        logger.info("\uD83D\uDC4C\uD83C\uDFFD \uD83D\uDC4C\uD83C\uDFFD " + amount + " tokens issued to: ".concat(account.getName())
                .concat(" for land: \uD83C\uDF45 ".concat(land.getName())
                        .concat(" \uD83C\uDF45 ")));
    }

    private static String getEmail() {
        return "user." + System.currentTimeMillis() + "@land.com";
    }

    private static String getPhone() {
        StringBuilder sb = new StringBuilder();
        sb.append("27");
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));

        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));

        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        return sb.toString();
    }

    private static Random random = new Random(System.currentTimeMillis());
    static List<String> names = new ArrayList<>();
    static HashMap<String, String> map = new HashMap<>();


    static String getRandomName() throws Exception {
        names.add("Jones Pty Ltd");
        names.add("Nkosi Associates");
        names.add("Maddow Enterprises");
        names.add("Xavier Inc.");
        names.add("House Inc.");
        names.add("Washington Brookes LLC");
        names.add("Johnson Associates Pty Ltd");
        names.add("Khulula Ltd");
        names.add("Innovation Partners");
        names.add("Peach Enterprises");
        names.add("Petersen Ventures Inc");
        names.add("Nixon Associates LLC");
        names.add("NamibianCool Inc.");
        names.add("BrothersFX Inc");
        names.add("Jabula Associates Pty Ltd");
        names.add("Graystone Khambule Ltd");
        names.add("Craighall Investments Ltd");
        names.add("Robert Grayson Associates");
        names.add("KZN Wildlife Pty Ltd");
        names.add("Kumar Enterprises Ltd");
        names.add("KrugerX Steel");
        names.add("TrainServices Pros Ltd");
        names.add("Topper PanelBeaters Ltd");
        names.add("Pelosi PAC LLC");
        names.add("Blackridge Inc.");
        names.add("Soweto Engineering Works Pty Ltd");
        names.add("Soweto Bakeries Ltd");
        names.add("BlackStone Partners Ltd");
        names.add("Constitution Associates LLC");
        names.add("Gauteng Manufacturers Ltd");
        names.add("Bidenstock Pty Ltd");
        names.add("Innovation Solutions Pty Ltd");
        names.add("Schiff Ventures Ltd");
        names.add("Process Innovation Partners");
        names.add("TrendSpotter Inc.");
        names.add("KnightRider Inc.");
        names.add("Fantastica Technology Inc.");
        names.add("Flickenburg Associates Pty Ltd");
        names.add("Cyber Operations Ltd");
        names.add("WorkerBees Inc.");
        names.add("FrickerRoad LLC.");
        names.add("Mamelodi Hustlers Pty Ltd");
        names.add("Wallace Incorporated");
        names.add("Peachtree Solutions Ltd");
        names.add("InnovateSpecialists Inc");
        names.add("DealMakers Pty Ltd");
        names.add("Clarity Solutions Inc");
        names.add("UK Holdings Ltd");
        names.add("Lauraine Pty Ltd");
        names.add("Paradigm Partners Inc");
        names.add("Washington Partners LLC");
        names.add("Motion Specialists Inc");
        names.add("OpenFlights Pty Ltd");
        names.add("ProServices Pty Ltd");
        names.add("TechnoServices Inc.");
        names.add("BrokerBoy Inc.");
        names.add("GermanTree Services Ltd");
        names.add("ShiftyRules Inc");
        names.add("BrookesBrothers Inc");
        names.add("PresidentialServices Pty Ltd");
        names.add("LawBook LLC");
        names.add("CampaignTech LLC");
        names.add("Tutankhamen Ventures Ltd");
        names.add("CrookesAndTugs Inc.");
        names.add("Coolidge Enterprises Inc");
        names.add("ProGuards Pty Ltd");
        names.add("BullFinch Ventures Ltd");
        names.add("ProGears Pty Ltd");
        names.add("HoverClint Ltd");
        names.add("KrugerBuild Pty Ltd");
        names.add("Treasure Hunters Inc");
        names.add("Kilimanjaro Consultants Ltd");
        names.add("Communications Brokers Ltd");
        names.add("VisualArts Inc");
        names.add("TownshipBusiness Ltd");
        names.add("HealthServices Pty Ltd");
        names.add("Macoute Professionals Ltd");
        names.add("Melber Pro Brokers Inc");
        names.add("Bronkies Park Pty Ltd");
        names.add("WhistleBlowers Inc.");
        names.add("Charles Mignon Pty Ltd");
        names.add("IntelligenceMaker Inc.");
        names.add("CroMagnon Industries");
        names.add("Status Enterprises LLC");
        names.add("Things Inc.");
        names.add("Rainmakers Ltd");
        names.add("Forensic Labs Ltd");
        names.add("DLT TechStars Inc");
        names.add("CordaBrokers Pty Ltd");
        names.add("Cordarell Farms");
        names.add("Johan van Niekerk");
        names.add("Pieter van Niekerk");
        names.add("Hessie van Niekerk");
        names.add("Johan Terblanche");
        names.add("Johan van Rensburg");
        names.add("Dan Durant");
        names.add("Johan Boshoff");
        names.add("Poppie Boshoff");
        names.add("Ruan Boshoff");
        names.add("Pieter Boshoff");
        names.add("Johan Ferreira");
        names.add("Johan Pieter Ferreira");
        names.add("Salome Ferreira");
        names.add("Johan Smit");
        names.add("Daniel Smit");
        names.add("Susanna Smit");
        names.add("Ruan Smit");
        names.add("Johan Frank Smit");
        names.add("Ronald Smit");
        names.add("John Makhubela");
        names.add("Peter K. Makhubela");
        names.add("Ntombi Makhubela");
        names.add("John Maringa");
        names.add("Bafana Maringa");
        names.add("Thabo Maringa");
        names.add("Dithebe Maringa");
        names.add("John Dlamini");
        names.add("Papi Dlamini");
        names.add("Ronald Dlamini");
        names.add("Thabiso Dlamini");
        names.add("David Dlamini");


        String name = names.get(random.nextInt(names.size() - 1));
        if (map.containsKey(name)) {
            String msg = "Random name collision";
            logger.warn(msg.concat(" - ").concat(name));
            getRandomName();
        } else {
            map.put(name, name);
        }

        return name;
    }
}
