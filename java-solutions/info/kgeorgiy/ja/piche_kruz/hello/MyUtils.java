package info.kgeorgiy.ja.piche_kruz.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Class with static functions for use in HelloUDPClient and HelloUDPServer
 */
class MyUtils {

    protected static DatagramPacket createPacket(DatagramSocket var0) throws SocketException {
        return new DatagramPacket(new byte[var0.getReceiveBufferSize()], var0.getReceiveBufferSize());
    }

    protected static String request(String message, DatagramSocket socket, SocketAddress address) throws IOException {
        send(socket, message, address);
        return receive(socket);
    }

    private static String receive(DatagramSocket socket) throws IOException {
        DatagramPacket var1 = createPacket(socket);
        socket.receive(var1);
        return getString(var1);
    }

    protected static void send(DatagramSocket socket, String s, SocketAddress address) throws IOException {
        DatagramPacket var3 = new DatagramPacket(new byte[0], 0);
        setString(var3, s);
        var3.setSocketAddress(address);
        socket.send(var3);
    }

    public static void setString(DatagramPacket var0, String var1) {
        byte[] var2 = getBytes(var1);
        var0.setData(var2);
        var0.setLength(var0.getData().length);
    }

    public static byte[] getBytes(String var0) {
        return var0.getBytes(StandardCharsets.UTF_8);
    }

    protected static String getString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}
