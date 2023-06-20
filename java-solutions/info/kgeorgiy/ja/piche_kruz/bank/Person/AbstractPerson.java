package info.kgeorgiy.ja.piche_kruz.bank.Person;

import info.kgeorgiy.ja.piche_kruz.bank.Account;
import info.kgeorgiy.ja.piche_kruz.bank.Person.Person;
import info.kgeorgiy.ja.piche_kruz.bank.RemoteAccount;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractPerson implements Person {
    private final String name;
    private final String lastName;
    private final String passportNumber;
    private final ConcurrentHashMap<String, Account> accounts;

    protected AbstractPerson(String name, String lastName, String passportNumber, ConcurrentHashMap<String, Account> accounts) {
        this.name = name;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
        this.accounts = Objects.requireNonNullElseGet(accounts, ConcurrentHashMap::new);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportNumber() {
        return passportNumber;
    }

    private String getFullId(String id) {
        return passportNumber + ":" + id;
    }

    @Override
    public Account getAccount(String id) {
        String fullAccountId = getFullId(id);
        if (!accounts.containsKey(fullAccountId)) {
            addAccount(id);
        }
        return accounts.get(fullAccountId);
    }

    @Override
    public void addAccount(String id) {
        String fullAccountId = getFullId(id);
        accounts.putIfAbsent(id, new RemoteAccount(fullAccountId));
    }


    public ConcurrentHashMap<String, Account> getAccounts() {
        return accounts;
    }
}
