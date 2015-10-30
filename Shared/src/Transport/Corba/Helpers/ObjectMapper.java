package Transport.Corba.Helpers;

import Transport.Corba.BankServerPackage.Bank;

public class ObjectMapper {

    public static Bank MapToCorbaObject(Data.Bank source) throws ObjectMappingException {
        Bank destination = Bank.None;

        switch (source)
        {
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
}
