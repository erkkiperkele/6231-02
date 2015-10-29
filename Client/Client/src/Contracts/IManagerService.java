package Contracts;

import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;

import javax.security.auth.login.FailedLoginException;
import java.util.Date;


/**
 * Defines the contract for the client's manager services.
 */
public interface IManagerService {

    /**
     * allows a manager to sign into the manager's client
     * @param bank
     * @param email
     * @param password
     * @return
     * @throws FailedLoginException
     */
    Customer signIn(Bank bank, String email, String password)
            throws FailedLoginException;

    /**
     * allows a manager to delay a customer's loan due date
     * @param bank
     * @param loanID
     * @param currentDueDate
     * @param newDueDate
     */
    void delayPayment(Bank bank, int loanID, Date currentDueDate, Date newDueDate);

    /**
     * allows a manager to retrieve all of the bank's customer information.
     * That is accounts and loans.
     * @param bank
     * @return
     */
    CustomerInfo[] getCustomersInfo(Bank bank);
}
