package com.lip.contracts;

import com.lip.states.PropertyState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
//    private final MockServices ledgerServices = new MockServices();
//
//    private final TestIdentity alice = new TestIdentity(new CordaX500Name("OneConnectSuppliers", "Johannesburg","ZA"));
//    private final PropertyState propertyState = new PropertyState(alice.getParty(),
//            "Alice","aloce@oneconnect.co.za",
//            "099 877 5643","tbd", "Pharma, Retail");
//    @Test
//    public void contractIsSupplierContract() {
//
//        assert (new PropertyContract() instanceof Contract);
//    }
//    @Test
//    public void supplierContractsRequiresZeroInputsInTheTransaction() {
//        transaction(ledgerServices, tx -> {
//            tx.input(PropertyContract.ID, propertyState);
//            tx.output(PropertyContract.ID, propertyState);
//            tx.command(alice.getPublicKey(),new PropertyContract.Register());
//            tx.fails();
//            return  null;
//
//        });
//    }
}
