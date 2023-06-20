package info.kgeorgiy.ja.piche_kruz.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    final int maxTasks = 100000;
    DatagramSocket socket;
    ExecutorService workers;
    BlockingQueue<DatagramPacket> works;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(400);
            workers = Executors.newFixedThreadPool(threads);
            works = new ArrayBlockingQueue<>(maxTasks);
            for (int i = 0; i < threads; i++) {
                workers.submit(listen(socket));
            }
        } catch (SocketException ex) {
            printError("Cannot listen to port " + port, ex);
        }
    }

    private void sendPack() {
        String message = "";
        try {
            if (!works.isEmpty()) {
                DatagramPacket sendPack = works.take();
                message = "Hello, " + MyUtils.getString(sendPack);
                MyUtils.send(socket,
                        message,
                        sendPack.getSocketAddress());
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                printError("There was a problem sending package " + message, e);
            }
        } catch (InterruptedException e) {
            printError("Could not send package, Queue interrupted", e);
        }
    }

    private void receivePack() {
        try {
            DatagramPacket packet = MyUtils.createPacket(socket);
            synchronized (this) {
                if (!socket.isBound() || socket.isClosed()) {
                    return;
                }
                socket.receive(packet);
            }
            works.put(packet);
        } catch (IOException e) {
            if (!socket.isClosed()) {
                printError("Unable to get packages from the given socket " + socket, e);
            }
        } catch (InterruptedException e) {
            printError("Unable to save work ", e);
        }
    }

    private Runnable listen(DatagramSocket socket) {
        return () -> {
            synchronized (this) {
                while (!socket.isClosed() && !Thread.interrupted()) {
                    receivePack();
                    sendPack();
                }
            }
        };
    }

    @Override
    public void close() {
        works.clear();
        socket.close();
        workers.shutdown();
    }

    private static boolean parseInt(String s, Integer[] intArr, int index, String name) {
        try {
            intArr[index] = Integer.parseInt(s);
            return false;
        } catch (NumberFormatException e) {
            printError("Expected a number as " + name + ", given " + s, e);
            return true;
        }
    }

    private static void printError(String message, Exception e) {
        System.err.println(message + " " + e.getMessage());
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Expected 2 arguments, given " + (args == null ? "null" : args.length));
            return;
        }
        Integer[] intArr = new Integer[2];
        if (parseInt(args[0], intArr, 0, "port number")) {
            return;
        }
        if (parseInt(args[1], intArr, 1, "number of threads")) {
            return;
        }
        try (HelloUDPServer helloUDPServer = new HelloUDPServer()) {
            helloUDPServer.start(intArr[0], intArr[1]);

        }
    }
}
