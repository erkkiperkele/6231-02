package Transport;

import Contracts.ICustomerRMIServer;
import Contracts.ICustomerServer;
import Data.Bank;
import Data.Customer;
import Data.Loan;
import Data.ServerPorts;

import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * An implementation for the Customer server interface.
 * (See interface documentation for details on functionality)
 */
public class CustomerRMIClient implements ICustomerRMIServer {

    private final String policyPath = "./LoanManagerSecurity.policy";
    private ICustomerServer server;

    public CustomerRMIClient(Bank bank) {
        System.setProperty("java.security.policy", this.policyPath);

        this.server = null;

        try {
            String serverUrl = String.format("rmi://localhost:%d/customer", ServerPorts.getRMIPort(bank));
            this.server = (ICustomerServer) Naming.lookup(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public int openAccount(Bank bankId, String firstName, String lastName, String emailAddress, String phoneNumber, String password)
            throws RemoteException {
        int accountNumber = this.server.openAccount(bankId, firstName, lastName, emailAddress, phoneNumber, password);
        return accountNumber;
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password) throws RemoteException, FailedLoginException {
        return this.server.getCustomer(bank, email, password);
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws RemoteException, FailedLoginException {
        return this.server.signIn(bank, email, password);
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount)
            throws RemoteException, FailedLoginException {
        return this.server.getLoan(bank, accountNumber, password, loanAmount);
    }
}

