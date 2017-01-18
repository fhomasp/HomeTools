package com.peeterst.android.server;

import com.peeterst.android.util.NetworkAddressChooser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 25/05/13
 * Time: 18:32
 */
public class Server {

//    private static final Logger log = LoggerFactory.getLogger(Server.class);
    static ServerSocket master;
    static Map<Integer,Runnable> inputHandlers;
    private static Server instance;

    private static int port;
    private static String hostname;
    private static ServerConfigService serverConfigService;


//    public static void main(Postable postable) {
//
//        try {
//            master = new ServerSocket(8900);
//            inputHandlers = new ConcurrentHashMap<Integer, Runnable>();
//            Socket socket = master.accept();
//            InputHandler inputHandler = new InputHandler(socket,postable);
//            OutputHandler outputHandler = new OutputHandler(socket);
//            new Thread(inputHandler).start();
//            new Thread(outputHandler).start();
//            inputHandlers.put(0, inputHandler);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    }

    private Server(){

    }

    public static Server getServer(Postable postable) throws IOException {
        if(master == null) {
            serverConfigService = ServerConfigService.getInstance();
            serverConfigService.setPostable(postable);
            determineServerDefaultsAndUseConfig();
//            master = new ServerSocket(8900);
            inputHandlers = new ConcurrentHashMap<Integer, Runnable>();
            if(instance == null){
                instance = new Server();
            }
        }
        return instance;
    }

    public void startServing(Postable postable,String name) throws SocketException {
        try {

            determineServerDefaultsAndUseConfig();

            for(int i = 0;;i++){
                postable.post("\nOpening connection on " + hostname +" at "+port);
                postable.post("Waiting for Connection...");
                Socket socket = master.accept();
                InputHandler inputHandler = new InputHandler(socket,postable);
                new Thread(inputHandler).start();

                String clientName = inputHandler.getClientName();
                if(clientName == null){
                    Thread.sleep(500);
                    clientName = inputHandler.getClientName();
                    if(clientName == null){
                        socket.close();
                        System.out.println("Timeout...");
                        postable.post("Timeout...  dropped");
                        continue;
                    }
                }

                inputHandlers.put(i,inputHandler);
                UIOutputHandler outputHandler = new UIOutputHandler(socket);
                outputHandler.setOwnerName(clientName);
                String attachedName = postable.attachClient(clientName,outputHandler);
                inputHandler.clientName = attachedName; //the registered name counts
                outputHandler.sendHeader(name); //todo: threading heartbeat issue?

            }


        }catch (Exception e){
            e.printStackTrace();
//            log.info("Server",e);
            if(e instanceof SocketException){
                throw (SocketException)e;
            }
        }
    }

    public void connectClient(String name,String server, String port, Postable caller) throws IOException {

        if(name != null && server != null && port != null){

            Socket master = new Socket();

//            master.setSoTimeout(2000);
            master.connect(new InetSocketAddress(server,Integer.parseInt(port)),2000);
            UIOutputHandler outputHandler = new UIOutputHandler(master);
            outputHandler.setOwnerName(name);

            ClientInputHandler inputHandler = new ClientInputHandler(master,caller);
            new Thread(inputHandler).start();

            outputHandler.sendHeader();

            inputHandlers.put(0,inputHandler);
//            String attachClient = caller.attachClient(clientName, outputHandler);
//            inputHandler.clientName = attachClient;

        }

    }

    public static void determineServerDefaultsAndUseConfig() throws IOException {
        Properties properties = serverConfigService.getEnvProperties();


        String readPort = properties.getProperty("port");
        if(readPort == null){
            readPort = "12345";
            properties.put("port",readPort);
        }
        String readHostname = getHostAddress();

        //re-write the config
        serverConfigService.storeEnvProperties(properties);

        port = Integer.parseInt(readPort);
        hostname = readHostname;

        if(master == null || master.getLocalPort() != port){
            master = new ServerSocket();
            master.setReuseAddress(true);
            master.bind(new InetSocketAddress(port));
        }

    }

    public static String getHostAddress() throws SocketException {

        String usableIPV4Address = NetworkAddressChooser.getUsableIPV4Address();
        return usableIPV4Address;
//        try {
//            String addressFirst = InetAddress.getLocalHost().getHostAddress();
//            if(!addressFirst.equals("127.0.0.1")){
//                return addressFirst;
//            }
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
//
//        while(interfaces.hasMoreElements()){
//
//            NetworkInterface ni =  (NetworkInterface) interfaces.nextElement();
//            Enumeration addrEnum = ni.getInetAddresses();
//            while(addrEnum.hasMoreElements()){
//
//                InetAddress addr = (InetAddress) addrEnum.nextElement();
//                if(!addr.getHostAddress().equals("127.0.0.1")){
//                    return addr.getHostAddress();
//                }
//            }
//
//
//        }


//        return null;
    }

//    public UIOutputHandler startFromUI(Postable postable) throws SocketException {
//        try {
//
//            Socket socket = master.accept();
//            new Thread(new InputHandler(socket,postable)).start();
//            return new UIOutputHandler(socket);
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//            if(e instanceof SocketException){
//                throw (SocketException)e;
//            }
//        }
//        return null;
//    }

    public boolean stop() throws IOException {
//        master.setSoTimeout(10);
//        for(Runnable runnable:inputHandlers.values()){
//            InputHandler inputHandler = (InputHandler) runnable;
//            try {
//                inputHandler.stop();
//            }catch (IOException io){
//                io.printStackTrace();
//                //we do need to continue and close the master
//            }
//        }
        System.out.println("closing master");
        master.close();
        master = null;
        return true;
    }

    public static class InputHandler implements Runnable {

        private Socket socket;
        private Postable postable;
        private String clientName;
        private DataChecker checker;
        private boolean commune;
        private Heartbeat heartbeat;


        public InputHandler(Socket socket,Postable postable) {
            this.socket = socket;
            this.postable = postable;
        }

        @Override
        public void run() {
            commune = true;
            try {
//                socket.setSoTimeout(1000);
                checker = new DataChecker(socket.getInputStream(),socket.getReceiveBufferSize());

                //TODO: timeout principle (more threads?)
                clientName = checker.processClientHeader();

                heartbeat = new Heartbeat(10,socket);
                heartbeat.startBeating();
                while (commune) {

                    checker.processUTF(); //blocking command

                    if(checker.getData() != null){
                        if(checker.getData().equals(Header.createHeartbeatResponseLine())){
                            heartbeat.reset();
                            continue;
                        }
                        System.out.println("\n<client> " + checker.getData());
                        heartbeat.reset();
                        if(postable != null){
                            postable.postWhole(clientName, checker);
                            postable.post(clientName, checker.getData());
                        }

                        if (checker.getData().toLowerCase().equals("bye")) {
                            commune = false;
                        }
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                if(postable != null){
                    if(clientName == null){
                        postable.post("no client name... dropped ...");
                    }else {
                        exp.printStackTrace();
                        postable.post("@" + clientName + " -- " + exp.getMessage());
                        postable.removeClient(clientName);
                        heartbeat.stop();
                    }
                }
            } finally {
//                try {
////                    reader.close();
//                } catch (Exception e) {
//                }
                try {
                    socket.close();
                    if(postable != null){
                        postable.post("socket closed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public String getClientName() {
            return clientName;
        }

        public void stop() throws IOException {
//            socket.close();
//            checker.getInputStream().close();
            commune = false;
        }
    }


    public static class ClientInputHandler implements Runnable {

        private Socket socket;
        private Postable postable;
        private String clientName;
        private DataChecker checker;
        private boolean commune;


        public ClientInputHandler(Socket socket,Postable postable) {
            this.socket = socket;
            this.postable = postable;
        }

        @Override
        public void run() {
            commune = true;
            try {
//                socket.setSoTimeout(1000);
                checker = new DataChecker(socket.getInputStream(),socket.getReceiveBufferSize());

                while (commune) {

                    checker.processUTF(); //blocking command

                    if(checker.getData() != null){
                        System.out.println("\n<client> " + checker.getData());
                        if(postable != null){
                            postable.postWhole(clientName, checker);
//                            postable.post(clientName, checker.getData());
                        }

                        if (checker.getData().toLowerCase().equals("bye")) {
                            commune = false;
                        }
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                if(postable != null){
                    if(clientName == null){
                        postable.post("no client name... dropped ...");
                    }else {
                        exp.printStackTrace();
                        postable.post("@" + clientName + " -- " + exp.getMessage());
                        postable.removeClient(clientName);
                    }
                }
            } finally {
//                try {
////                    reader.close();
//                } catch (Exception e) {
//                }
                try {
                    socket.close();
                    if(postable != null){
                        postable.post("socket closed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public String getClientName() {
            return clientName;
        }

        public void stop() throws IOException {
//            socket.close();
//            checker.getInputStream().close();
            commune = false;
        }
    }

    /**
     * Not Runnable: runs on the UI thread with a listener which has its own thread
     */
    public class UIOutputHandler {

        DataOutputStream dataOutputStream;
        private String ownerName;
        private Socket socket;

        public UIOutputHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }

        public void send(String data){
            try {
                this.dataOutputStream.writeUTF(data);
                this.dataOutputStream.flush();

            } catch (IOException e) {
                throw new RuntimeException("Error sending.");
            }

        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public void close() throws IOException {
            socket.close();
            dataOutputStream.close();
        }

        private void sendHeader() throws IOException {
            String header = Header.createHeaderLine(ownerName);
            dataOutputStream.writeUTF(header);
            dataOutputStream.flush();
            System.out.println("Header sent");
        }

        private void sendHeader(String headerBody) throws IOException {
            String header = Header.createHeaderLine(headerBody);
            dataOutputStream.writeUTF(header);
            dataOutputStream.flush();
            System.out.println("Header sent");
        }
    }

//    public class UIInputHandler{
//
//    }
}
