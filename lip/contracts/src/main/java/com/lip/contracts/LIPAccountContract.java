package com.lip.contracts;

import com.lip.states.LIPAccountState;
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

public class LIPAccountContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = LIPAccountContract.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(LIPAccountContract.class);

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 LIPAccountContract: verify starting ..... \uD83E\uDD6C \uD83E\uDD6C ");
        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input states must be zero");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output LIPAccount is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 requiredSigners: "
                + requiredSigners.size());
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof LIPAccountState)) {
            throw new IllegalArgumentException("Output state must be LIPAccountState");
        }
        LIPAccountState lipAccountState = (LIPAccountState)contractState;
        if (lipAccountState.getAccountInfo() == null) {
            throw new IllegalArgumentException("AccountInfo is required");
        }
        if (lipAccountState.getPartyAndCertificate() == null) {
            throw new IllegalArgumentException("PartyAndCertificate is required");
        }
        if (lipAccountState.getOtherParties().isEmpty()) {
            throw new IllegalArgumentException("OtherParties are required");
        }

        Party party = lipAccountState.getAccountInfo().getHost();
        PublicKey key = party.getOwningKey();
        if (!requiredSigners.contains(key)) {
            throw new IllegalArgumentException("Owning node must sign");
        }
        logger.info(" \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 Checking that all nodes have signed: " +
                "Number of Nodes  \uD83D\uDD35 " + lipAccountState.getOtherParties().size());
        for (Party p: lipAccountState.getOtherParties()) {
            PublicKey k = party.getOwningKey();
            if (!requiredSigners.contains(k)) {
                throw new IllegalArgumentException(p.getName().toString() + " - node must sign");
            }
        }

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                "LIPAccountContract: verification done OK! .....\uD83E\uDD1F \uD83E\uDD1F ");

    }

    public static class Register implements CommandData {}
}
