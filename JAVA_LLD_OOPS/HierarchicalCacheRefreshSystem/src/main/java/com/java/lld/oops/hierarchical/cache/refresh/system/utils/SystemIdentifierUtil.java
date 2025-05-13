package com.java.lld.oops.hierarchical.cache.refresh.system.utils;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;

public class SystemIdentifierUtil {

    public static String getSystemUniqueKey() {
        try {
            StringBuilder sb = new StringBuilder();

            // Append MAC address from first non-virtual, non-loopback interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface : Collections.list(interfaces)) {
                if (!iface.isLoopback() && !iface.isVirtual() && iface.isUp()) {
                    byte[] mac = iface.getHardwareAddress();
                    if (mac != null) {
                        for (byte b : mac) {
                            sb.append(String.format("%02X", b));
                        }
                        break;
                    }
                }
            }

            // Append OS name and architecture
            sb.append(System.getProperty("os.name"));
            sb.append(System.getProperty("os.arch"));

            // Hash the combined info for a consistent fixed-length key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString(); // Unique and stable across days
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate system key", e);
        }
    }
}