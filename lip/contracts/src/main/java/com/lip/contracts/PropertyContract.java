package com.lip.contracts;

import com.lip.states.PropertyState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************
@SuppressWarnings("unchecked")
public class PropertyContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = PropertyContract.class.getName();

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{

        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input states must be zero");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output PropertyState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();

        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof PropertyState)) {
            throw new IllegalArgumentException("Output state must be PropertyState");
        }
        PropertyState propertyState = (PropertyState)contractState;
        if (propertyState.getName() == null) {
            throw new IllegalArgumentException("Property name is required");
        }
        Party party = propertyState.getBnoParty();
        PublicKey key = party.getOwningKey();
        if (!requiredSigners.contains(key)) {
            throw new IllegalArgumentException("BNO Party must sign");
        }

        Party party3 = propertyState.getRegulatorParty();
        PublicKey key3 = party3.getOwningKey();
        if (!requiredSigners.contains(key3)) {
            throw new IllegalArgumentException("Regulator Party must sign");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Action implements Commands {}
    }

    public static class Register implements CommandData {}
}
