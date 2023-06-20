package info.kgeorgiy.ja.piche_kruz.bank.Person;

import info.kgeorgiy.ja.piche_kruz.bank.Account;

import java.util.concurrent.ConcurrentHashMap;

public class RemotePerson extends AbstractPerson {
    public RemotePerson(String name, String lastName, String passportNumber, ConcurrentHashMap<String, Account> accounts) {
        super(name, lastName, passportNumber, accounts);
    }
}
