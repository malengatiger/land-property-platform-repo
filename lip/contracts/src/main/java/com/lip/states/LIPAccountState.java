package com.lip.states;

import com.lip.contracts.LIPAccountContract;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(LIPAccountContract.class)
@CordaSerializable
public class LIPAccountState implements ContractState {
    private final static Logger logger = LoggerFactory.getLogger(LIPAccountState.class);

    private final AccountInfo accountInfo;
    private final PartyAndCertificate partyAndCertificate;
    private final List<Party> otherParties;
    private final String name, email,
            cellphone,
            identifier;

    public LIPAccountState(AccountInfo accountInfo,
                           PartyAndCertificate partyAndCertificate,
                           List<Party> otherParties,
                           String name, String email,
                           String cellphone, String identifier) {
        this.accountInfo = accountInfo;
        this.partyAndCertificate = partyAndCertificate;
        this.otherParties = otherParties;
        this.name = name;
        this.email = email;
        this.cellphone = cellphone;
        this.identifier = identifier;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        for (Party party : otherParties) {
            if (party.getName().toString()
                    .equalsIgnoreCase(accountInfo.getHost().getName().toString())) {
                continue;
            }
            list.add(party);
        }
        list.add(0, accountInfo.getHost());
        logger.info("Number of Participants: " + list.size());
        return list;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public PartyAndCertificate getPartyAndCertificate() {
        return partyAndCertificate;
    }

    public List<Party> getOtherParties() {
        return otherParties;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getIdentifier() {
        return identifier;
    }
}
