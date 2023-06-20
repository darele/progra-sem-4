package info.kgeorgiy.ja.piche_kruz.bank.Person;

import info.kgeorgiy.ja.piche_kruz.bank.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to work with natural people with bank accounts.
 */
public interface Person extends Remote {
    /**
     * Returns the name of a Person instance.
     * @return The name of the registered natural person.
     */
    String getName() throws RemoteException;

    /**
     * Returns last name of a Person instance.
     * @return Last name of registered natural person.
     */
    String getLastName() throws RemoteException;

    /**
     * Returns passport number of a Person instance.
     * @return Saved passport number for Person instance.
     */
    String getPassportNumber() throws RemoteException;

    /**
     * Returns the account with the specified Id through person instance.
     * This method asks the bank for the Account with ID=passportNumber:id.
     * @param id Account's id.
     * @return The account with the given id associated with this Person instance
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Adds an account to this Person instance with zero balance.
     * @param id ID of the new account to be added to this Person instance.
     */
    void addAccount(String id) throws RemoteException;
}
