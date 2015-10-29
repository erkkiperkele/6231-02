package Services;

import Contracts.IFileLogger;
import Contracts.ILoggerService;
import Data.Customer;
import IO.FileLogger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * This service provides an implementation for the loggerService interface
 * (see interface documentation)
 */
public class LoggerService implements ILoggerService, Closeable {
    private Map<Integer, IFileLogger> loggers;
    private String rootPath = "./Logs/";

    public LoggerService() {

        this.loggers = new HashMap<>();
    }


    @Override
    public void close() throws IOException {
        for (IFileLogger logger : this.loggers.values()) {
            logger.close();
        }
    }

    @Override
    public IFileLogger getLogger() {
        Customer currentCustomer = SessionService.getInstance().getCurrentCustomer();
        return getLazyLogger(currentCustomer);
    }

    private IFileLogger getLazyLogger(Customer currentCustomer) {

        IFileLogger logger = this.loggers.get(currentCustomer.getId());

        if (logger == null) {
            logger = createCustomerLogger(currentCustomer);
        }
        return logger;
    }

    private IFileLogger createCustomerLogger(Customer currentCustomer) {

        String path = this.rootPath
                + currentCustomer.getFirstName()
                + "_"
                + currentCustomer.getLastName()
                + "_"
                + currentCustomer.getBank()
                + ".txt";

        createFile(path);

        IFileLogger logger = new FileLogger(path);
        this.loggers.put(currentCustomer.getId(), logger);

        return logger;
    }

    private void createFile(String path) {
        try {
            File f = new File(path);

            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
