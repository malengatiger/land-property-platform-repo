package com.lip.webserver.util;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lip.states.LandToken;
import com.lip.webserver.LandDTO;
import com.lip.webserver.data.LIPAccountDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FirebaseUtil {
    private final static Logger logger = LoggerFactory.getLogger(FirebaseUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final FirebaseAuth auth = FirebaseAuth.getInstance();
    static final Firestore db = FirestoreClient.getFirestore();
    static final FirebaseMessaging messaging = FirebaseMessaging.getInstance();

//    public static void sendInvoiceOfferMessage(InvoiceOfferDTO offer) throws ExecutionException, InterruptedException {
//
//        String topic = "invoiceOffers";
//        // See documentation on defining a message payload.
//        Notification m = new Notification("New Invoice Offer", GSON.toJson(offer));
//        Message message = Message.builder()
//                .putData("invoiceOffer", GSON.toJson(offer))
//                .setNotification(m)
//                .setTopic(topic)
//                .build();
//
//        // Send a message to the devices subscribed to the provided topic.
//        String response = messaging.sendAsync(message).get();
//        // Response is a message ID string.
//        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
//                "Successfully sent FCM INVOICE OFFER message to topic: \uD83D\uDE21 ").concat(topic)
//                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
//                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
//    }
//    public static void sendInvoiceMessage(InvoiceDTO offer) throws ExecutionException, InterruptedException {
//
//        String topic = "invoices";
//        // See documentation on defining a message payload.
//        Notification m = new Notification("New Invoice", GSON.toJson(offer));
//        Message message = Message.builder()
//                .putData("invoice", GSON.toJson(offer))
//                .setNotification(m)
//                .setTopic(topic)
//                .build();
//
//        // Send a message to the devices subscribed to the provided topic.
//        String response = messaging.sendAsync(message).get();
//        // Response is a message ID string.
//        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
//                "Successfully sent FCM INVOICE message to topic: \uD83D\uDE21 ").concat(topic)
//                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
//                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
//    }
//    public static void sendAccountMessage(AccountInfoDTO account) throws ExecutionException, InterruptedException {
//
//        String topic = "accounts";
//        // See documentation on defining a message payload.
//        Notification m = new Notification("New BFN Account", GSON.toJson(account));
//        Message message = Message.builder()
//                .putData("account", GSON.toJson(account))
//                .setNotification(m)
//                .setTopic(topic)
//                .build();
//
//        // Send a message to the devices subscribed to the provided topic.
//        String response = messaging.sendAsync(message).get();
//        // Response is a message ID string.
//        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
//                "Successfully sent FCM ACCOUNT message to topic: \uD83D\uDE21 ").concat(topic)
//                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
//                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
//    }

    static void addUserToDatabase(LIPAccountDTO account) throws Exception {
        ApiFuture<DocumentReference> future = db.collection("accounts")
                .add(GSON.toJson(account));
        DocumentReference reference = future.get();
        logger.info(("\uD83D\uDD11 \uD83D\uDD11 \uD83D\uDD11 " +
                "Account added to Firestore: ").concat(reference.getPath()));

    }
    static void createUser(String name, String email, String password,
                           String cellphone,
                           String uid)
            throws FirebaseAuthException {

        UserRecord.CreateRequest request = new UserRecord.CreateRequest();
        request.setEmail(email);
        request.setDisplayName(name);
        request.setPassword(password);
        if (cellphone != null) {
            request.setPhoneNumber("+".concat(cellphone));
        }
        if (uid == null) {
            uid = UUID.randomUUID().toString();
        }
        request.setUid(uid);

        UserRecord userRecord = auth.createUser(request);
        logger.info(("\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83E\uDD66 \uD83E\uDD66 " +
                "User record created in Firebase:  \uD83E\uDD66 ")
                .concat(userRecord.getEmail()));
    }

    static List<LIPAccountDTO> getAccounts() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("accounts").get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> list = snapshot.getDocuments();
        List<LIPAccountDTO> mLipAccounts = new ArrayList<>();
        for (QueryDocumentSnapshot qds: list) {
            LIPAccountDTO x = GSON.fromJson(qds.getData().toString(),LIPAccountDTO.class);
            mLipAccounts.add(x);
        }
        logger.info("\uD83C\uDF4A \uD83C\uDF4A Firestore found LipAccounts: \uD83D\uDD06 " + mLipAccounts.size() + " \uD83D\uDD06 ");
        return mLipAccounts;
    }
    static List<LandDTO> getLandParcels() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("landParcels").get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> list = snapshot.getDocuments();
        List<LandDTO> landParcels = new ArrayList<>();
        for (QueryDocumentSnapshot qds: list) {
            LandDTO x = GSON.fromJson(qds.getData().toString(),LandDTO.class);
            landParcels.add(x);
        }
        logger.info("\uD83E\uDD6C \uD83E\uDD6C Firestore found landParcels: \uD83D\uDD06 " + landParcels.size() + " \uD83D\uDD06 ");
        return landParcels;
    }
    static LandToken getToken(String identifier) throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("tokenTypes")
                .whereEqualTo("identifier",identifier)
                .get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> list = snapshot.getDocuments();
        if (list.isEmpty()) {
            throw new Exception("TokenType not found");
        }
        LandToken token = GSON.fromJson(list.get(0).getData().toString(),LandToken.class);
        return token;
    }
    static void addToken(LandToken token) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> future = db.collection("tokenTypes").add(token);
        DocumentReference documentReference = future.get();
        logger.info("\uD83C\uDF81 \uD83C\uDF81 TokenType added to Firestore: "

        .concat(documentReference.getPath()));
    }
    public static void deleteUsers() throws FirebaseAuthException {
        // Start listing users from the beginning, 1000 at a time.
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                if (user.getEmail().contains("aubrey")) {
                    continue;
                }
                auth.deleteUser(user.getUid());
                logger.info("\uD83C\uDF4A \uD83C\uDF4A \uD83C\uDF4A User deleted: " + user.getEmail());
            }
            page = page.getNextPage();
        }
        page = auth.listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            if (user.getEmail().contains("aubrey")) {
                continue;
            }
            logger.info("\uD83C\uDF4A \uD83C\uDF4A \uD83C\uDF4A User deleted: " + user.getEmail());
            auth.deleteUser(user.getUid());

        }
    }

    public static UserRecord getUser(String email) throws FirebaseAuthException {
        UserRecord record = null;
        try {
             record = auth.getUserByEmail(email);
        } catch (Exception e) {

        }
        return record;
    }

    public static List<UserRecord> getUsers() throws FirebaseAuthException {

        List<UserRecord> records = new ArrayList<>();
        ListUsersPage page = auth.listUsers(null);
        Iterable<ExportedUserRecord> m = page.getValues();
        m.forEach(records::add);

        int cnt = 0;
        for (UserRecord record : records) {
            cnt++;
            logger.info("\uD83E\uDD66  \uD83E\uDD66 UserRecord #" +
                    cnt + " from Firebase: ".concat(GSON.toJson(record)));
        }

        return records;
    }

    static void deleteCollections() {
        Iterable<CollectionReference> m = db.listCollections();
        for (CollectionReference reference : m) {
            logger.info("\uD83C\uDF4A \uD83C\uDF4A Existing Firestore collection: ".concat(reference.getPath()));
            deleteCollection(reference, 200);
        }
    }

    /**
     * Delete a collection in batches to avoid out-of-memory errors.
     * Batch size may be tuned based on document size (atmost 1MB) and application requirements.
     */
    private static void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
                logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted document:  \uD83D\uDC9C ".concat(document.getReference().getPath()));
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
                logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted collection:  \uD83E\uDDE1 ".concat(collection.getPath()));
            }
        } catch (Exception e) {
            logger.error("Error deleting collection : " + e.getMessage());
        }
    }

    private static int BATCH_SIZE = 2000;

    public static void deleteCollection(String collectionName) throws ExecutionException, InterruptedException {
        // retrieve a small batch of documents to avoid out-of-memory errors
        CollectionReference collection = db.collection(collectionName);
        ApiFuture<QuerySnapshot> future = collection.limit(BATCH_SIZE).get();
        int deleted = 0;
        // future.get() blocks on document retrieval
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            document.getReference().delete();
            ++deleted;
            logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted document:  \uD83D\uDC9C ".concat(document.getReference().getPath()));
        }
        if (deleted >= BATCH_SIZE) {
            // retrieve and delete another batch
            deleteCollection(collectionName);
            logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted collection:  \uD83E\uDDE1 ".concat(collection.getPath()));
        }

    }
}
