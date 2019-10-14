package com.lip.webserver.util;

import com.lip.states.LandToken;
import com.lip.webserver.LandDTO;
import com.lip.webserver.data.LIPAccountDTO;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DemoUtil {
    private final static Logger logger = LoggerFactory.getLogger(DemoUtil.class);

    public static String startDemoData(CordaRPCOps proxy) throws Exception {

        logger.info("\n\nDemoUtil starting, cleaning up Firebase ...");
        FirebaseUtil.deleteUsers();
        FirebaseUtil.deleteCollection("accounts");

        logger.info("\nDemoUtil generating accounts  ...");
        int cnt = 0;
        for (int i = 0; i < 30; i++) {
            try {
                WorkerBee.addAccount(proxy,
                        getRandomName(),
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
                "\uD83C\uDF45 " + cnt + " \uD83C\uDF45 ");
        String m = issueTokens(proxy);
        String msg = "\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                "Demo Data generation COMPLETE!\n".concat(m);
        return msg;
    }

    private static String issueTokens(CordaRPCOps proxy) throws Exception {
        List<LIPAccountDTO> lipAccounts = FirebaseUtil.getAccounts();
        List<LandDTO> lands = FirebaseUtil.getLandParcels();

        for (LandDTO land : lands) {
            int numberOfHolders = random.nextInt(5);
            if (numberOfHolders == 0) numberOfHolders = 1;
            logger.info(" \uD83C\uDF1E \uD83C\uDF1E ".concat(land.getName())
                    .concat(" to be split among \uD83C\uDF1E " + numberOfHolders) + " \uD83C\uDF1E "
            .concat(" tokens: " + land.getValue()));
            splitTokens(proxy, land, numberOfHolders, lipAccounts);
        }
        String msg = "\uD83D\uDC4C\uD83C\uDFFD \uD83D\uDC4C\uD83C\uDFFD Tokens issued to all accounts selected";
        logger.info(msg);
        return msg;
    }

    private static void splitTokens(CordaRPCOps proxy, LandDTO land,
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
        LandToken token = WorkerBee.createLandTokenType(proxy, land.getName(), land.getIdentifier());
        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 LandToken created. YEBO! \uD83E\uDDA0 "
                .concat(token.getDescription()));
        FirebaseUtil.addToken(token);
        Collection<LIPAccountDTO> list = map.values();
        long eachSplit = (long) (land.getValue() / numberOfHolders);
        for (LIPAccountDTO accountDTO : list) {
            grantTokensToAccount(proxy, accountDTO, eachSplit, token, land);
        }

    }

    private static void grantTokensToAccount(CordaRPCOps proxy, LIPAccountDTO account,
                                             long amount, LandToken landToken, LandDTO land) throws Exception {
        WorkerBee.issueTokens(proxy, amount, account.getIdentifier(),
                landToken.getLinearId().getId().toString(), land.getIdentifier());
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

        String name = names.get(random.nextInt(names.size() - 1));
        if (map.containsKey(name)) {
            throw new Exception("Random name collision");
        } else {
            map.put(name, name);
        }

        return name;
    }
}
