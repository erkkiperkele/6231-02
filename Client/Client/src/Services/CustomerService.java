package Services;


import Contracts.ICustomerServer;
import Contracts.ICustomerService;
import Data.Bank;
import Data.Customer;
import Data.Loan;
import Exceptions.TransferException;
import Transport.BankClientCorba;

import javax.security.auth.login.FailedLoginException;

/**
 * This service provides the customer console's functionality
 * (see interface documentation)
 */
public class CustomerService implements ICustomerService {

    private ICustomerServer[] clients;

    public CustomerService() {
        initializeCorbaClients();
    }

    private void initializeCorbaClients() {
        this.clients = new BankClientCorba[3];
        this.clients[Bank.Royal.toInt() - 1] = new BankClientCorba(Bank.Royal);
        this.clients[Bank.National.toInt() - 1] = new BankClientCorba(Bank.National);
        this.clients[Bank.Dominion.toInt() - 1] = new BankClientCorba(Bank.Dominion);
    }

    @Override
    public int openAccount(
            Bank bank,
            String firstName,
            String lastName,
            String emailAddress,
            String phoneNumber,
            String password) {

        ICustomerServer client = this.clients[bank.toInt() - 1];
        int accountNumber = client.openAccount(bank, firstName, lastName, emailAddress, phoneNumber, password);
        return accountNumber;
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password)
            throws FailedLoginException {

        try {
            return this.clients[bank.toInt() - 1].getCustomer(bank, email, password);
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
        } catch (FailedLoginException e) {
            SessionService.getInstance().log().error(e.getMessage());
            e.printStackTrace();
        }
        return newLoan;
    }

    @Override
    public Loan transferLoan(int loanId, Bank currentBank, Bank otherBank) {
        Loan newLoan = null;
        try {
            newLoan = this.clients[currentBank.toInt() - 1].transferLoan(loanId, currentBank, otherBank);
        } catch (TransferException e) {
            SessionService.getInstance().log().error(e.getMessage());
            e.printStackTrace();
        }
        return newLoan;
    }
}
