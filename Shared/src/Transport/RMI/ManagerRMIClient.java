package Transport.RMI;

import Contracts.IManagerServer;
import Data.Bank;
import Data.CustomerInfo;
import Data.ServerPorts;
import Server.BankServer;

import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * An implementation for the Manager server interface.
 * (See interface documentation for details on functionality)
 */
public class ManagerRMIClient implements IManagerServer {

    private final String policyPath = "./LoanManagerSecurity.policy";
    private IManagerServer server;

    public ManagerRMIClient(Bank bank) {
        System.setProperty("java.security.policy", this.policyPath);

        int serverPort = ServerPorts.getRMIPort(bank);
        this.server = new BankServer(serverPort);

        try {
            String serverUrl = String.format("rmi://localhost:%d/manager", ServerPorts.CustomerRMI.getRMIPort());
            this.server = (IManagerServer) Naming.lookup(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void delayPayment(Bank bank, int loanId, Date currentDueDate, Date newDueDate) throws RemoteException, RecordNotFoundException {

        this.server.delayPayment(bank, loanId, currentDueDate, newDueDate);
    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws RemoteException, FailedLoginException {
        return this.server.getCustomersInfo(bank);
    }
}

