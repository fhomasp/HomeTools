package com.peeterst.android.data;

import android.os.Handler;
import android.os.Message;
import com.peeterst.android.checklistview.ChecklistConnectActivity;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.network.AndroidClient;
import com.peeterst.android.server.Postable;
import com.peeterst.android.server.Server;
import com.peeterst.android.server.ServerConfigService;
import com.peeterst.android.util.NetworkAddressChooser;
import com.peeterst.android.xml.ChecklistWriteXML;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 3/07/13
 * Time: 20:02
 */
public class ChecklistNetworkIO {

    private static ChecklistNetworkIO instance;

    private AndroidClient.InputHandler inputHandler;
    private AndroidClient.OuputHandler ouputHandler;
    private boolean connected;
    private Server server;

    private Map<String,Server.UIOutputHandler> clientOutlets = new ConcurrentHashMap<String,Server.UIOutputHandler>();

    public Mode mode;

    public boolean hasNetwork() {
        String usableIPV4Address = NetworkAddressChooser.getUsableIPV4Address();
        return usableIPV4Address != null;
    }

    //TODO: use this mode in the connectactivity?
    public enum Mode {
        SERVER,CLIENT
    }

    private ChecklistNetworkIO(){
    }

    public static ChecklistNetworkIO getInstance(){
        if(instance == null){
            instance = new ChecklistNetworkIO();
        }
        return instance;
    }



    public void connectClient(String name,String server, String port,Handler handler) throws IOException {

        if(name != null && server != null && port != null){

            Socket master = new Socket();

//            master.setSoTimeout(2000);
            master.connect(new InetSocketAddress(server,Integer.parseInt(port)),2000);


            ouputHandler = new AndroidClient.OuputHandler(name,master);

            inputHandler = new AndroidClient.InputHandler(name,master,handler,ouputHandler);

            new Thread(ouputHandler).start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(inputHandler).start();
            connected = true;
            mode = Mode.CLIENT;
        }

    }

    public void send(String data){
        ouputHandler.send(data);
    }

    public boolean disconnect() throws IOException {
        boolean ok = false;
        ok = inputHandler.stop();
        connected = false;
        if(!ok){
            ouputHandler.stop();
            throw new IOException("Unable to stop the Input Handler");
        }else {
            return ouputHandler.stop();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void connectServer(final Postable postable,final Handler handler, final String name,String port) throws IOException {
        server = Server.getServer(postable);
        int serverPort = Integer.parseInt(port);
        ServerConfigService serverConfigService = ServerConfigService.getInstance();
        serverConfigService.setPostable(postable);
        Properties envProperties = serverConfigService.getEnvProperties();
        envProperties.put("port",port);
        serverConfigService.storeEnvProperties(envProperties);
        Runnable serverRunner = new Runnable() {


            @Override
            public void run() {

                try {
//                    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
//                    while(interfaces.hasMoreElements()){
//
//                        NetworkInterface ni =  (NetworkInterface) interfaces.nextElement();
//                        Enumeration addrEnum = ni.getInetAddresses();
//                        while(addrEnum.hasMoreElements()){
//
//                            Inet4Address addr = (Inet4Address) addrEnum.nextElement();
//                            System.err.println(addr.getHostAddress());
//                        }
//
//
//                    }

                    server.startServing(postable,name);
                    ChecklistNetworkIO.this.connected = true;
                }catch (IOException e){
                    e.printStackTrace();
                    Message message = Message.obtain(handler);
                    message.obj = ChecklistConnectActivity.ChecklistNetworkHandler.SERVING_STOPPED;
                    ChecklistNetworkIO.this.connected = false;
                    handler.dispatchMessage(message);
                }

            }
        };
        Thread serverThread = new Thread(serverRunner);
        serverThread.start();
        mode = Mode.SERVER;
        this.connected = true;
    }

    public void stopServer() throws IOException {
        boolean stopped = server.stop();
        if(stopped){
            System.out.println("server stopped");
        }else {
            System.out.println("server NOT stopped");
        }
        ChecklistNetworkIO.this.connected = false;
    }

    public boolean isClient(){
        return this.mode == Mode.CLIENT;
    }

    public Map<String, Server.UIOutputHandler> getClientOutlets() {
        return clientOutlets;
    }

    public void setClientOutlets(Map<String, Server.UIOutputHandler> clientOutlets) {
        this.clientOutlets = clientOutlets;
    }

    public void sendChecklist(String contact,Checklist checklist) {
        ChecklistWriteXML checklistWriteXML = new ChecklistWriteXML(checklist);
        if(isClient()){
            send(checklistWriteXML.createStringVersion());
        }
        if(contact == null){
            for(String outlet:clientOutlets.keySet()){
                Server.UIOutputHandler outputHandler = clientOutlets.get(outlet);
                outputHandler.send(checklistWriteXML.createStringVersion());
            }
        }else {
            for(String outlet:clientOutlets.keySet()){
                if(outlet.equals(contact)){
                    Server.UIOutputHandler outputHandler = clientOutlets.get(outlet);
                    outputHandler.send(checklistWriteXML.createStringVersion());
                }
            }
        }
    }

}
