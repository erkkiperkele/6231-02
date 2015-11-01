package Services;

import Contracts.IFileLogger;
import Contracts.ILoggerService;
import Contracts.ISessionService;
import Data.Bank;
import Data.Customer;

/**
 * A singleton class to handle session information and logging services
 * It insures log are written for the current customer.
 */
public class SessionService implements ISessionService {
    private static SessionService ourInstance = new SessionService();
    private static Customer currentCustomer;
    private static ILoggerService loggerService;

    private SessionService() {
        this.loggerService = new LoggerService();
        this.currentCustomer = new Customer(
                0,
                0,
                "Default",
                "Customer",
                "none",
                Bank.None,
                "default.customer@default.com",
                "514.111.2222");
    }

    public static SessionService getInstance() {
        return ourInstance;
    }

    @Override
    public void setCurrentCustomer(Customer currentCustomer) {
        SessionService.currentCustomer = currentCustomer;
    }

    @Override
    public Customer getCurrentCustomer() {
        return this.currentCustomer;
    }

    @Override
    public boolean isLoggedIn() {
        return currentCustomer.getAccountNumber() != 0;
    }

    @Override
    public IFileLogger log() {
        return this.loggerService.getLogger();
    }
}
