package ie.nuigalway.topology.api.resources;

public class IPv4Converter {

	public static Long ipv4ToLong(String addr) {
        String[] address = addr.split("\\.");

        long ip = 0;

        for (int i = 0; i < address.length; i++) {

            int power = 3-i;

            ip += ((Integer.parseInt(address[i]) % 256 * Math.pow(256, power)));

        }
        return ip;
    }
	
	public static String longToIpv4(Long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >>  8) & 0xFF) + "." + (ip & 0xFF);
    }
}
