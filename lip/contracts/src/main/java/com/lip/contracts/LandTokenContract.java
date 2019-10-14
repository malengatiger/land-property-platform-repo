package com.lip.contracts;

import com.lip.states.LandState;
import com.lip.states.LandToken;
import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LandTokenContract extends EvolvableTokenContract implements Contract {
    private final static Logger logger = LoggerFactory.getLogger(LandTokenContract.class);
    @Override
    public void additionalCreateChecks(@NotNull LedgerTransaction tx) {
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  additionalCreateChecks starting ...");
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof LandToken)) {
            throw new IllegalArgumentException("Output state must be LandToken");
        }
        LandToken landToken = (LandToken) contractState;
        if (landToken == null) {
            throw new IllegalArgumentException("\uD83D\uDC7FLandToken is required");
        }
        if (landToken.getDescription() == null) {
            throw new IllegalArgumentException("\uD83D\uDC7FLandToken description is required");
        }
        if (landToken.getFractionDigits() != 0) {
            throw new IllegalArgumentException("\uD83D\uDC7FFractionDigits should be zero");
        }
        if (landToken.getMaintainers().size() != 1) {
            throw new IllegalArgumentException("\uD83D\uDC7F There should be exactly 1 maintainer");
        }
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  additionalCreateChecks done!  ☘️  ☘️ ");

    }

    @Override
    public void additionalUpdateChecks(@NotNull LedgerTransaction tx) {
        logger.info("additionalUpdateChecks starting ...");
    }
}
