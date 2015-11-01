package Contracts;

import Data.Bank;
import Data.Customer;
import Data.Loan;
import Exceptions.TransferException;

import javax.security.auth.login.FailedLoginException;

/**
 * Defines the contract for the customer services.
 */
public interface ICustomerService {

    /**
     * Allows a new customer to open an account for the selected bank.
     * @param bank
     * @param firstName
     * @param lastName
     * @param emailAddress
     * @param phoneNumber format: 514.000.1111
     * @param password No complexity requirement at the moment.
     * @return
     */
    int openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password);

    /**
     * Retrieves a customer's info
     * @param bank
     * @param email
     * @param password
     * @return
     * @throws FailedLoginException
     */
    Customer getCustomer(Bank bank, String email, String password)
            throws FailedLoginException;

    /**
     * Allows a customer to sign in on the customer client
     * @param bank
     * @param email
     * @param password
     * @return
     * @throws FailedLoginException
     */
    Customer signIn(Bank bank, String email, String password)
            throws FailedLoginException;

    /**
     * allows a customer to request a loan at the chosen bank.
     * Note, that credit line will be checked at other banks
     * @param bank
     * @param accountNumber
     * @param password
     * @param loanAmount
     * @return
     */
    Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount);

    /**
     * Transfers a loan from a bank to another.
     * If the current user does not have an account at the other bank, the account will be created.
     * The transfer is transactional. If any of the required operation fails, all prior operations will
     * be rolled back.
     * @param loanId of the loan at the current bank
     * @param currentBank the current customer's bank where the loan is from
     * @param otherBank the new bank the loan is transferred to
     * @return the new loan created at the other bank
     * @throws TransferException
     */
    Loan transferLoan(int loanId, Bank currentBank, Bank otherBank);
}
