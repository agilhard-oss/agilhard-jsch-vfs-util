package net.agilhard.jschutil;

import java.util.HashMap;

/**
 * Fixed Host Mapping workaround for hosts not in DNS.
 *
 * @author bei
 */
public final class FixedHostMap {

    /**
     * private Constructor for utility class.
     */
    private FixedHostMap() {
        //.
    }

    /**
     * Map of hostnames to IP Addresses.
     */
    private static HashMap<String, String> hostMap = new HashMap<>();

    /**
     * Get IP from fixed mapping.
     *
     * @param host
     *            the hostname.
     * @return th ip address of the host if it is in the fixed map.
     */
    public static String getFixedIpOf(final String host) {
        if (host == null || hostMap == null) {
            return null;
        }
        return hostMap.get(host);
    }

    public static void put(String host, String ip) {
	hostMap.put(host,ip);
    }
}
