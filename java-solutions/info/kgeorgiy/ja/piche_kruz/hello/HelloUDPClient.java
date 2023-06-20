package info.kgeorgiy.ja.piche_kruz.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * Class implementing {@link HelloClient}.
 * It sends to the specified host, the specified number of requests per thread with the given prefix.
 * It also receives answer from the server and outputs answers to standard output
 * */
public class HelloUDPClient implements HelloClient {
    final int maximumTime = 400;
    private record RequestHandler(int requests, String prefix, SocketAddress address, Phaser phaser) {
        private void sendRequests(final int threadNum, final DatagramSocket socket) {
            for (int i = 0; i < requests; i++) {
                String message = prefix + threadNum + "_" + i;
                while (!socket.isClosed() && !Thread.interrupted()) {
                    try {
                        String ans = MyUtils.request(message, socket, address);
                        if (ans.contains(message)) {
                            System.out.println(ans);
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        System.err.println("Maximum timeout to get an answer reached, sending request " + message + " again");
                    } catch (IOException e) {
                        printError("An error occurred while sending the request or processing the response: ", e);
                        break;
                    }
                }
            }
        }

        public void register() {
            phaser.register();
        }

        public void arriveAndAwaitAdvance() {
            phaser.arriveAndAwaitAdvance();
        }

        public void arriveAndDeregister() {
            phaser.arriveAndDeregister();
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress whereTo;
        try {
            whereTo = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            printError("Could not get address of the specified host " + host + " at port " + port, e);
            return;
        }
        final Phaser phaser = new Phaser(1);
        final RequestHandler requestHandler = new RequestHandler(requests, prefix, whereTo, phaser);
        final ExecutorService workers = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            requestHandler.register();
            workers.submit(createTask(i, requestHandler));
        }
        requestHandler.arriveAndAwaitAdvance();
        workers.shutdown();
    }

    private Runnable createTask(final int threadNum, final RequestHandler requestHandler) {
        return () -> {
            try (DatagramSocket socket = new DatagramSocket()){
                socket.setSoTimeout(maximumTime);
                requestHandler.sendRequests(threadNum, socket);
            } catch (SocketException e) {
                printError("Socket could not be open, please check network access", e);
            }
            requestHandler.arriveAndDeregister();
        };
    }

    private static void printError(String message, Exception e) {
        System.err.println(message + " " + e.getMessage());
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

    /**
     * Entry point for this class.
     * It reads 5 mandatory arguments from the command line.
     * <ol>
     *     <li>Name or IP-address of computer where server has been set</li>
     *     <li>Number of port by which requests should be sent</li>
     *     <li>Prefix to write to every request</li>
     *     <li>Number of threads to be created by which requests should be sent</li>
     *     <li>Number of requests that should be sent in every thread</li>
     * </ol>
     *
     * @param args - The array of arguments read from command line.
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Expected 5 arguments, given " + (args == null ? "null" : args.length));
            return;
        }
        final InetAddress serverAddress;
        InetAddress serverAddress1;
        try {
            serverAddress1 = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            //Maybe we are given IP
            try {
                serverAddress1 = InetAddress.getByAddress(args[0].getBytes(StandardCharsets.UTF_8));
            } catch (UnknownHostException ex) {
                printError("Can't get Sever Address for " + args[0], ex);
                return;
            }
        }
        serverAddress = serverAddress1;
        final Integer[] intArgs = new Integer[3];
        if (parseInt(args[1], intArgs, 0, "port")) {
            return;
        }
        final String prefix = args[2];
        if (parseInt(args[3], intArgs, 1, "maximum number of Threads")) {
            return;
        }
        if (parseInt(args[4], intArgs, 2, "maximum requests per thread")) {
            return;
        }
        HelloUDPClient helloUDPClient = new HelloUDPClient();
        helloUDPClient.run(serverAddress.getHostName(), intArgs[0], prefix, intArgs[1], intArgs[2]);
    }
}
