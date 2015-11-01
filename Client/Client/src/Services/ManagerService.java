package Services;

import Contracts.IManagerService;
import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;
import Transport.BankClientCorba;
import Exceptions.RecordNotFoundException;

import javax.security.auth.login.FailedLoginException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This service provides the manager console's functionality
 * (see interface documentation)
 */
public class ManagerService implements IManagerService {


    private BankClientCorba[] clients;

    public ManagerService() {
        initializeClients();
    }

    private void initializeClients() {
        this.clients = new BankClientCorba[3];
        this.clients[Bank.Royal.toInt() - 1] = new BankClientCorba(Bank.Royal);
        this.clients[Bank.National.toInt() - 1] = new BankClientCorba(Bank.National);
        this.clients[Bank.Dominion.toInt() - 1] = new BankClientCorba(Bank.Dominion);
    }

    @Override
    public Customer signIn(Bank bank, String username, String password) throws FailedLoginException {

        //Manager Sign in not implemented on server side in the context of this assignment
        if (username.equalsIgnoreCase("manager") && password.equals("manager")) {
            Customer currentManager = new Customer(0, 0, "Manager", "ByDefault", "manager", bank, username, "");
            SessionService.getInstance().setCurrentCustomer(currentManager);
            SessionService.getInstance().log().info(
                    String.format("Manager just signed in as : %1$s %2$s at bank %3$s",
                            currentManager.getFirstName(),
                            currentManager.getLastName(),
                            currentManager.getBank().toString()
                    ));
            return currentManager;
        } else {
            SessionService.getInstance().log().warn(
                    String.format("Manager failed to log in with userName: %1$s at bank: %2$s",
                            username,
                            bank.toString()
                    ));
            throw new FailedLoginException();
        }
    }

    @Override
    public void delayPayment(Bank bank, int loanId, Date currentDueDate, Date newDueDate) {

        try {
            this.clients[bank.toInt() - 1].delayPayment(bank, loanId, currentDueDate, newDueDate);

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SessionService.getInstance().log().info(
                    String.format("Manager delayed payment for loan: %1$s from previous date: %2$s to new date: %3$s",
                            bank,
                            dateFormat.format(currentDueDate),
                            dateFormat.format(newDueDate))
            );

        } catch (RecordNotFoundException e) {
            SessionService.getInstance().log().warn(
                    String.format("Loan #%d not found at bank: %s.", loanId, bank)
            );
        }
    }

    /**
     * Implemented in RMI only. With Corba use the getCustomersInfoMessage.
     * REFACTOR: Corba should implement this interface.
     * @param bank
     * @return
     */
    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) {

        CustomerInfo[] infos = null;
        try {
            infos = this.clients[bank.toInt() - 1].getCustomersInfo(bank);
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
        return infos;
    }
}
