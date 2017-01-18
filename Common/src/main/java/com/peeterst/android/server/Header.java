package com.peeterst.android.server;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 2/06/13
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public enum Header {

    NAME,CHANGEID,STOP,HEARTBEAT,HEARTBEATRESPONSE;

    Header(){

    }

    public static String createHeaderLine(String name){
        return NAME.name()+"$"+name;
    }

    public static String getNameFromHeaderLine(String header){
        if(header.contains(NAME.name()+"$")) {
            String res = header.replace(NAME.name() + "$", "");
            return res;
        }else {
            return null;
        }
    }

    public static String createHeartbeatLine(){
        return HEARTBEAT.name()+"$";
    }

    public static String createHeartbeatResponseLine(){
        return HEARTBEATRESPONSE.name()+"$";
    }
}
