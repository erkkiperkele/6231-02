package Contracts;

import Data.Account;
import Data.Bank;
import Data.Customer;
import Data.Loan;

import javax.security.auth.login.FailedLoginException;
import java.util.List;

/**
 * Defines the contract for the server customer services in order to provide the
 * required information to satisfy the API functionality.
 */
public interface ICustomerService {

    /**
     * Allows to create a new account.
     * @param bank Account won't be created if the bank is not the one of the server the API runs at
     * @param firstName
     * @param lastName
     * @param emailAddress
     * @param phoneNumber Format: 514.000.1111
     * @param password No complexity validation at the moment
     * @return
     */
    int openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password);

    /**
     * Retrieves a customer information if he has an account.
     * Else returns null.
     * @param email
     * @return
     */
    Customer getCustomer(String email);

    /**
     * Retrieves a list of loans currently registered at the bank for the current user.
     * Does not retrieve loans from other banks.
     * Will return null if account doesn't exist.
     * @param accountNumber
     * @return
     */
    List<Loan> getLoans(int accountNumber);

    /**
     * Will create a new loan for the given account number
     * granted the customer's credit line is low enough.
     * Note: the customer credit line is verified against all banks.
     * By default, when a new account is created, total amount for the loans
     * should not exceed 1.500$.
     * @param bank
     * @param accountNumber
     * @param password
     * @param loanAmount requested loan amount
     * @return
     * @throws FailedLoginException this exception is thrown in case the password is incorrect
     */
    Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount) throws FailedLoginException;

    Account getAccount(String firstName, String LastName);

}
