package Transport.Corba.Helpers;

import Data.CustomerInfo;
import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Loan;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Calendar;
import java.util.Date;

public class ObjectMapper {

    public static Transport.Corba.BankServerPackage.Bank toCorbaBank(Data.Bank bank) {

        Transport.Corba.BankServerPackage.Bank corbaBank = null;
        try {
            corbaBank = ObjectMapper.mapToCorbaObject(bank);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return corbaBank;
    }

    public static Transport.Corba.BankServerPackage.Date toCorbaDate(Date date) {

        return mapToCorbaObject(date);
    }

    public static Customer toCorbaCustomer(Data.Customer customer) {
        return mapToCorbaObject(customer);
    }

    public static Loan toCorbaLoan(Data.Loan serverLoan) {
        return mapToCorbaObject(serverLoan);
    }

    //REFACTOR: pass a customerInfo[] in Corba IDL instead of managing a string.
    public static String toCorbaCustomersInfo(CustomerInfo[] customersInfo) {

        String corbaCustomersInfo = "";
        for(CustomerInfo customerInfo : customersInfo)
        {
            corbaCustomersInfo += mapToCorbaObject(customerInfo);
        }
        return corbaCustomersInfo;
    }

    public static Data.Customer toCustomer(Transport.Corba.BankServerPackage.Customer corbaCustomer) {

        Data.Customer customer = null;
        try {
            customer = ObjectMapper.mapToClientObject(corbaCustomer);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return customer;
    }

    public static Data.Loan toLoan(Transport.Corba.BankServerPackage.Loan corbaLoan) {

        Data.Loan loan = null;
        try {
            loan = ObjectMapper.mapToClientObject(corbaLoan);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return loan;
    }

    public static Data.Bank toBank(Transport.Corba.BankServerPackage.Bank corbaBank) {

        Data.Bank bank = null;
        try {
            bank = ObjectMapper.mapToClientObject(corbaBank);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return bank;
    }

    public static Date toDate(Transport.Corba.BankServerPackage.Date currentDueDate) {

        return mapToClientObject(currentDueDate);
    }

    public static CustomerInfo[] toCustomersInfo(String customersInfo) {

        throw new NotImplementedException();
    }

    private static Bank mapToCorbaObject(Data.Bank source) throws ObjectMappingException {
        Bank destination = Bank.None;

        switch (source) {
            case Dominion:
                destination = Bank.Dominion;
                break;
            case National:
                destination = Bank.National;
                break;
            case Royal:
                destination = Bank.Royal;
                break;
            case None:
                destination = Bank.None;
                break;
            default:
                throw new ObjectMappingException(
                        "This Bank is not implemented in the Corba Interface (BankServer.idl)"
                );
        }
        return destination;
    }

    private static Customer mapToCorbaObject(Data.Customer customer) {
        return new Customer(
                (short)customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                toCorbaBank(customer.getBank()),
                (short)customer.getAccountNumber(),
                customer.getPhone(),
                customer.getPassword()
        );
    }

    private static Transport.Corba.BankServerPackage.Date mapToCorbaObject(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH)+1;
        int day = cal.get(cal.DAY_OF_MONTH);
        return new Transport.Corba.BankServerPackage.Date(year, month, day);
    }

    private static Transport.Corba.BankServerPackage.Loan mapToCorbaObject(Data.Loan serverLoan){

        Transport.Corba.BankServerPackage.Loan corbaLoan = new Loan(
                (short)serverLoan.getLoanNumber(),
                (short)serverLoan.getCustomerAccountNumber(),
                (int)serverLoan.getAmount(),
                mapToCorbaObject(serverLoan.getDueDate())
        );

        return corbaLoan;
    }

    private static String mapToCorbaObject(CustomerInfo customerInfo){

        return customerInfo.toString();
    }

    private static Data.Bank mapToClientObject(Bank source) throws ObjectMappingException {
        Data.Bank destination = Data.Bank.None;


        switch (source.toString()) {
            case "Dominion":
                destination = Data.Bank.Dominion;
                break;
            case "National":
                destination = Data.Bank.National;
                break;
            case "Royal":
                destination = Data.Bank.Royal;
                break;
            case "None":
                destination = Data.Bank.None;
                break;
            default:
                throw new ObjectMappingException(
                        "This Bank is not implemented in the Corba Interface (BankServer.idl)"
                );
        }
        return destination;
    }

    private static Data.Customer mapToClientObject(Customer corbaCustomer) throws ObjectMappingException {

        return new Data.Customer(
                corbaCustomer.id,
                corbaCustomer.accountNumber,
                corbaCustomer.firstName,
                corbaCustomer.lastName,
                corbaCustomer.password,
                mapToClientObject(corbaCustomer.bank),
                corbaCustomer.email,
                corbaCustomer.phone
        );
    }

    private static Data.Loan mapToClientObject(Loan corbaLoan) throws ObjectMappingException {

        return new Data.Loan(
                corbaLoan.loanNumber,
                corbaLoan.customerAccountNumber,
                corbaLoan.amount,
                mapToClientObject(corbaLoan.dueDate)
        );
    }

    private static Date mapToClientObject(Transport.Corba.BankServerPackage.Date corbaDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH, corbaDate.day);
        calendar.set(Calendar.MONTH, corbaDate.month -1);
        calendar.set(Calendar.YEAR, corbaDate.year);
        return calendar.getTime();
    }
}
