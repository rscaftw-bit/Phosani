
package com.phosanihelper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class BotZender {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 5006;

    public static void stuurCommandoNaarPython(String commando) {
        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress address = InetAddress.getByName(IP_ADDRESS);
            byte[] buffer = commando.getBytes(StandardCharsets.UTF_8);

            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    address,
                    PORT
            );

            socket.send(packet);

            System.out.println("[Java] Signaal verzonden: " + commando);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
