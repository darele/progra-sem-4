package info.kgeorgiy.ja.piche_kruz.bank;

import info.kgeorgiy.ja.piche_kruz.bank.Person.LocalPerson;
import info.kgeorgiy.ja.piche_kruz.bank.Person.Person;
import info.kgeorgiy.ja.piche_kruz.bank.Person.PersonType;
import info.kgeorgiy.ja.piche_kruz.bank.Person.RemotePerson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person getPerson(String passportNumber, PersonType personType) throws RemoteException {
        RemotePerson person;
        person = persons.getOrDefault(passportNumber, null);
        if (personType == PersonType.REMOTE || person == null) {
            return person;
        }
        return new LocalPerson(person.getName(), person.getLastName(), person.getPassportNumber(), new ConcurrentHashMap<>(person.getAccounts()));
    }

    @Override
    public Person registerPerson(String name, String lastName, String passportNumber) throws RemoteException {
        System.out.println("Registering Mr/Mrs " + lastName + ", " + name);
        final RemotePerson person = new RemotePerson(name, lastName, passportNumber, null);
        if (persons.putIfAbsent(passportNumber, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        }
        System.out.println("Error: Passport number " + passportNumber + " is Already registered");
        return persons.get(passportNumber);
    }
}
