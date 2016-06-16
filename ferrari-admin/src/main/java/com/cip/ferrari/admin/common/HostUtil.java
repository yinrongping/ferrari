/**
 * 
 */
package com.cip.ferrari.admin.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yuantengkai
 *
 */
public class HostUtil {
	
	private static final Logger logger = LoggerFactory
			.getLogger(HostUtil.class);
	
	private static String IP = null;

	/**
	 * 获取本机的机器名称
	 * 
	 * @return
	 */
	public static String getHostname() {
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			return localhost.getHostName();
		} catch (UnknownHostException e) {

		}
		return "";
	}
	
	/**
	 * 获取本机ip
	 * @return
	 */
	public static String getIP() {
        if (IP == null) {
            IP = getInetAddress();
        }
        return IP;
    }
	
	private static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        return address.getHostAddress();
                    }
                }
            }
            logger.warn("[InetAddressUtil] Can not get the server IP address.");
            return null;
        } catch (Throwable t) {
        	logger.error("[InetAddressUtil] Get the server IP address failed.", t);
            return null;
        }
    }
	
	public static void main(String[] args) throws UnknownHostException {
		System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
		System.out.println(InetAddress.getLocalHost().getHostName());
		System.out.println(getIP());
	}
}
