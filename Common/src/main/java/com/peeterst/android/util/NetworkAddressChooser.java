package com.peeterst.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 24/08/2014
 * Time: 16:41
 */
public class NetworkAddressChooser {

    public static void getNetworkType(Context context){
        ConnectivityManager connectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager =  (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        int netType = info.getType();
        int netSubtype = info.getSubtype();

        if (netType == ConnectivityManager.TYPE_WIFI || netType == ConnectivityManager.TYPE_WIMAX)
        {

        }
        else if (netType == ConnectivityManager.TYPE_MOBILE &&
                netSubtype == TelephonyManager.NETWORK_TYPE_UMTS)
        {
            //3G connection
            if(!telephonyManager.isNetworkRoaming())
            {
                //do some networking
            }
        }
    }

    public static String getUsableIPV4Address(){
        Enumeration<NetworkInterface> ifs = null;
        try {
            ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface iface = ifs.nextElement();
                System.out.println(iface.getName());
                Enumeration<InetAddress> en = iface.getInetAddresses();
                while (en.hasMoreElements()) {
                    InetAddress addr = en.nextElement();
                    if(addr.isLoopbackAddress()) continue;
                    String address = addr.getHostAddress();
                    int end = address.lastIndexOf("%");
                    if (end > 0) {
                        String substring = address.substring(0, end);
                        System.out.println("\t" + substring);
                        if(checkNumberedAddress(substring)){
                            return substring;
                        }
                    }
                    else {
                        System.out.println("\t" + address);
                        if(checkNumberedAddress(address)){
                            return address;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            // error handling appropriate for your application
        }
        if (ifs == null) return null;
        return null;
    }

    private static boolean checkNumberedAddress(String address) {
        try {
            if (address == null || address.equals("") || address.equals(" ")) {
                return false;
            }

            String[] parts = address.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            return !address.endsWith(".");

        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
