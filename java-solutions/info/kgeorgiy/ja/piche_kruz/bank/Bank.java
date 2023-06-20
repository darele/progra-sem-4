package info.kgeorgiy.ja.piche_kruz.bank;

import info.kgeorgiy.ja.piche_kruz.bank.Person.Person;
import info.kgeorgiy.ja.piche_kruz.bank.Person.PersonType;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Gets a Person instance of the given type by passport number.
     * When LOCAL personType is specified, a current copy of the state of the person with the given
     * passport number will be returned.
     * When REMOTE is specified, the corresponding remote reference is returned
     * @param passportNumber Passport number of the person instance to be returned.
     * @param personType LOCAL or REMOTE according to need
     * @return Person instance of the specified type or null if such person is not registered
     */
    Person getPerson(String passportNumber, PersonType personType) throws RemoteException;

    /**
     * Registers a Person instance in the bank provided their data.
     *
     * @param name           Person's first name
     * @param lastName       Person's last name
     * @param passportNumber Passport number uniquely identifying a person.
     * @return
     */
    Person registerPerson(String name, String lastName, String passportNumber) throws RemoteException;
}
