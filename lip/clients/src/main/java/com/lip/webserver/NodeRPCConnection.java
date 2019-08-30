package com.lip.webserver;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 */
@Component
public class NodeRPCConnection implements AutoCloseable {
    public static final Logger LOGGER = Logger.getLogger(NodeRPCConnection.class.getSimpleName());
    // The host of the node we are connecting to.
    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.rpc.port}")
    private int rpcPort;

    private CordaRPCConnection rpcConnection;
    CordaRPCOps proxy;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        LOGGER.log(Level.INFO,"NodeRPCConnection: \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 initialiseNodeRPCConnection: \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A host: " + host + " rpcPort:  \uD83D\uDD06 " + rpcPort + "  \uD83D\uDD06");
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(username, password);
        proxy = rpcConnection.getProxy();
        LOGGER.log(Level.INFO,"NodeRPCConnection: \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 initialiseNodeRPCConnection: Corda ServerProtocolVersion: \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A " + rpcConnection.getServerProtocolVersion());
        LOGGER.log(Level.INFO,"NodeRPCConnection: \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 initialiseNodeRPCConnection: getLegalIdentities: \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A " + proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation() + "  \uD83D\uDC9A  \uD83D\uDC9A");
    }

    @PreDestroy
    public void close() {
        rpcConnection.notifyServerAndClose();
    }
}
