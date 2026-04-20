package ru.etc1337.api.mods.fastping;

import com.google.common.net.InetAddresses;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.UnknownHostException;

@UtilityClass
public class InetAddressPatcher {
    @SuppressWarnings("UnstableApiUsage")
    public InetAddress patch(String hostName, InetAddress addr) throws UnknownHostException {
        if (InetAddresses.isInetAddress(hostName)) {
            addr = InetAddress.getByAddress(addr.getHostAddress(), addr.getAddress());
        }
        return addr;
    }
}