package com.lip.contracts;

import com.lip.states.LandState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************

public class LandContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = LandContract.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(LandContract.class);

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 LandContract: verify starting ..... \uD83E\uDD6C \uD83E\uDD6C ");
        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input states must be zero");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output LandState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 requiredSigners: " + requiredSigners.size());
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof LandState)) {
            throw new IllegalArgumentException("Output state must be LandState");
        }
        LandState landState = (LandState)contractState;
        if (landState.getName() == null) {
            throw new IllegalArgumentException("Land name is required");
        }
        if (landState.getAreaInSquareMetres() == 0) {
            throw new IllegalArgumentException("areaInSquareMetres is required");
        }
        if (landState.getPolygon().size() < 3) {
            throw new IllegalArgumentException("Polygon requires at least 3 coordinates");
        }
        if (landState.getBankParty() == null) {
            throw new IllegalArgumentException("Bank party required");
        }
        if (landState.getBnoParty() == null) {
            throw new IllegalArgumentException("Network operator party required");
        }
        if (landState.getLandAffairsParty() == null) {
            throw new IllegalArgumentException("LandAffairs party required");
        }
        if (landState.getRegulatorParty() == null) {
            throw new IllegalArgumentException("Regulator party required");
        }
        Party party = landState.getBnoParty();
        PublicKey key = party.getOwningKey();
        if (!requiredSigners.contains(key)) {
            throw new IllegalArgumentException("BNO Party must sign");
        }
        Party party2 = landState.getLandAffairsParty();
        PublicKey key2 = party2.getOwningKey();
        if (!requiredSigners.contains(key2)) {
            throw new IllegalArgumentException("Land Affairs Party must sign");
        }
        Party party3 = landState.getRegulatorParty();
        PublicKey key3 = party3.getOwningKey();
        if (!requiredSigners.contains(key3)) {
            throw new IllegalArgumentException("Regulator Party must sign");
        }
        Party party4 = landState.getRegulatorParty();
        PublicKey key4 = party4.getOwningKey();
        if (!requiredSigners.contains(key4)) {
            throw new IllegalArgumentException("BANK Party must sign");
        }
        if (!landState.getLandAffairsParty().getName().getOrganisation().contains("LandAffairs")) {
            throw new IllegalArgumentException("LandAffairs Party must drive this bus");
        }
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 LandContract: verification done OK! .....\uD83E\uDD1F \uD83E\uDD1F ");

    }

    public static class Register implements CommandData {}
}
