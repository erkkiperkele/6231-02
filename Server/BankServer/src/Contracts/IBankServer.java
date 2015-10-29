package Contracts;

/**
 * Defines the contract a bank server must implement in order to
 * register itself to a bank repository.
 */
public interface IBankServer {

    int getId();

    String getName();

    String getAddress();


}
