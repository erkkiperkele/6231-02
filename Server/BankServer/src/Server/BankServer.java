package Server;

import Contracts.*;
import Data.*;
import Services.BankService;
import Services.SessionService;
import Transport.RMI.RecordNotFoundException;

import javax.security.auth.login.FailedLoginException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

/**
 * This class starts both RMI and UDP servers for a given bank.
 * It also contains basic tests for data access (concurrency, UDP protocols...)
 */
public class BankServer {

    private static int serverPort;
    private static IBankService bankService;

    private static UDPServer udp;

    public BankServer(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Instatiates and starts the RMI and UDP servers.
     * ATTENTION: needs a single integer argument which is the bank Id for the server
     * See the Bank enum to know what integer corresponds to what bank.
     *
     * @param args a single integer defining what bank this server belongs to.
     */
    public static void main(String[] args) {

        String serverArg = args[0];
        initialize(serverArg);

        //Starting bank server
        startRMIServer();
        startUDPServer();
    }

    private static void initialize(String arg) {
        Bank serverName = Bank.fromInt(Integer.parseInt(arg));
        SessionService.getInstance().setBank(serverName);
        bankService = new BankService();
        udp = new UDPServer(bankService);
    }

    private static void startRMIServer() {

        Bank bank = SessionService.getInstance().getBank();
        int serverPort = ServerPorts.getRMIPort(bank);

        try {
            (new BankServerRMI(bankService, serverPort)).exportServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SessionService.getInstance().log().info(
                String.format("%s Server is up and running on port %d!",
                        bank,
                        serverPort)
        );
    }

    private static void startUDPServer() {
        Thread startUdpServer = new Thread(() ->
        {
            udp.startServer();
        });
        startUdpServer.start();
    }


}
