package com.lip.contracts;

import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract;
import com.r3.corda.lib.tokens.contracts.commands.EvolvableTokenTypeCommand;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LandTokenContract extends EvolvableTokenContract implements Contract {
    private final static Logger logger = LoggerFactory.getLogger(LandTokenContract.class);
    public final static String ID = LandTokenContract.class.getName();
    @Override
    public void additionalCreateChecks(@NotNull LedgerTransaction tx) {
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  additionalCreateChecks starting ...");
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof LandToken)) {
            throw new IllegalArgumentException("\uD83D\uDC7F Output state must be LandToken");
        }
        LandToken landToken = (LandToken) contractState;
        if (landToken.getDescription() == null) {
            throw new IllegalArgumentException("\uD83D\uDC7F LandToken description is required");
        }
        if (landToken.getFractionDigits() != 0) {
            throw new IllegalArgumentException("\uD83D\uDC7F FractionDigits should be zero");
        }
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D "
                .concat(" landToken.getMaintainers().size(): "
                        + landToken.getMaintainers().size()));

        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  additionalCreateChecks done!  ☘️  ☘️ ");

    }

    @Override
    public void additionalUpdateChecks(@NotNull LedgerTransaction tx) {
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E additionalUpdateChecks starting ...  \uD83C\uDF4E What happens here ???????");
        //todo - check input and output states
    }
    public static class Register implements CommandData {}
    public static class Distribute implements EvolvableTokenTypeCommand {}
}

