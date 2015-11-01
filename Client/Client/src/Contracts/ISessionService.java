package Contracts;

import Data.Customer;

/**
 * A simple interface for a Session Service
 * holding a current session info and giving access
 * to a logger.
 * Designed to be used as a Singleton.
 */
public interface ISessionService {

    /**
     * allows the client to store the current customer's identity
     * this is useful for logging info to this particular user's file,
     * or accessing its information anywhere
     * @param currentCustomer
     */
    void setCurrentCustomer(Customer currentCustomer);

    /**
     * retrieves the current customer information.
     * @return
     */
    Customer getCurrentCustomer();

    /**
     * indicates if a user is logged in.
     * @return
     */
    boolean isLoggedIn();

    /**
     * accesses the current customer's logger
     * @return
     */
    IFileLogger log();
}
