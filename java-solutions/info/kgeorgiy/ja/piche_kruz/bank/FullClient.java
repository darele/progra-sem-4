package info.kgeorgiy.ja.piche_kruz.bank;

import info.kgeorgiy.ja.piche_kruz.bank.Person.Person;
import info.kgeorgiy.ja.piche_kruz.bank.Person.PersonType;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class FullClient {
    public static void main(String[] args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        if (args == null || args.length != 5) {
            System.err.println("Expected 5 parameters, " + (args == null ? "null" : args.length) + " given");
            return;
        }
        Person person = bank.getPerson(args[2], PersonType.REMOTE);
        if (person == null) {
            person = bank.registerPerson(args[0], args[1], args[2]);
        }
        Account account = person.getAccount(args[3]);
        final int change;
        try {
             change = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Expected a number specifying the amount to " +
                    "deposit/withdraw, given " + args[4] + e.getMessage());
            return;
        }
        account.setAmount(account.getAmount() + change);
    }
}
