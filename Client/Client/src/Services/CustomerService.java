package Services;


import Contracts.ICustomerService;
import Data.Bank;
import Data.Customer;
import Data.Loan;
import Transport.RMI.CustomerRMIClient;

import javax.security.auth.login.FailedLoginException;
import java.rmi.RemoteException;

/**
 * This service provides the customer console's functionality
 * (see interface documentation)
 */
public class CustomerService implements ICustomerService {

    private CustomerRMIClient[] clients;

    public CustomerService() {
        initializeClients();
    }

    private void initializeClients() {
        this.clients = new CustomerRMIClient[3];
        this.clients[Bank.Royal.toInt() - 1] = new CustomerRMIClient(Bank.Royal);
        this.clients[Bank.National.toInt() - 1] = new CustomerRMIClient(Bank.National);
        this.clients[Bank.Dominion.toInt() - 1] = new CustomerRMIClient(Bank.Dominion);
    }

    @Override
    public int openAccount(
            Bank bank,
            String firstName,
            String lastName,
            String emailAddress,
            String phoneNumber,
            String password) {

        try {

            CustomerRMIClient client = this.clients[bank.toInt() - 1];
            int accountNumber = client.openAccount(bank, firstName, lastName, emailAddress, phoneNumber, password);
            return accountNumber;

        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password)
            throws FailedLoginException {

        try {

            return this.clients[bank.toInt() - 1].getCustomer(bank, email, password);

        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (FailedLoginException e) {
            SessionService.getInstance().log().error(e.getMessage());
            throw e;
        }
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws FailedLoginException {
        try {

            Customer foundCustomer = this.clients[bank.toInt() - 1].signIn(bank, email, password);
            SessionService.getInstance().log().info(
                    String.format("Customer just signed in as : %1$s %2$s at bank %3$s",
                            foundCustomer.getFirstName(),
                            foundCustomer.getLastName(),
                            foundCustomer.getBank().toString()
                    ));
            return foundCustomer;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (FailedLoginException e) {
            SessionService.getInstance().log().error(e.getMessage());
            throw e;
        }
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount) {

        Loan newLoan = null;
        try {

            newLoan = this.clients[bank.toInt() - 1].getLoan(bank, accountNumber, password, loanAmount);

        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (FailedLoginException e) {
            SessionService.getInstance().log().error(e.getMessage());
            e.printStackTrace();
        }
        return newLoan;
    }
}
