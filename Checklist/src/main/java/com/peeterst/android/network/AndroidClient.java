package com.peeterst.android.network;

import android.os.Handler;
import android.os.Message;
import com.peeterst.android.checklistview.ChecklistConnectActivity;
import com.peeterst.android.server.DataChecker;
import com.peeterst.android.server.Header;
import com.peeterst.android.util.ChecklistTempUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 02/07/13
 * Time: 18:38
 * Derived from the Client in the common module to fit the android needs
 *
 */
public class AndroidClient {

    public static void main(String[] args) {

        try {
            Socket master = new Socket("192.168.0.247", 8900);

            if(args == null || args.length == 0){
                OuputHandler one = new OuputHandler("One", master);
                new Thread(new InputHandler("One",master,null,one)).start();
                new Thread(one).start();
            }else {
                OuputHandler ouputHandler = new OuputHandler(args[0], master);
                new Thread(new InputHandler(args[0],master,null,ouputHandler)).start();
                new Thread(ouputHandler).start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static class InputHandler implements Runnable {

        private Socket socket;
        DataInputStream dataInputStream;
        private String name;
        private Handler handler;
        private OuputHandler registeredOutputHandler;

        public InputHandler(String name,Socket socket, Handler handler, OuputHandler ouputHandler) {
            this.socket = socket;
            this.name = name;
            this.handler = handler;
            this.registeredOutputHandler = ouputHandler;
        }

        @Override
        public void run() {
            boolean commune = true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            BufferedReader reader = null;
            try {
                DataChecker dataChecker = new DataChecker(socket.getInputStream(),socket.getReceiveBufferSize());
                dataInputStream = (DataInputStream) dataChecker.getInputStream();

//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (commune) {
                    dataChecker.processUTF();
                    String text = dataChecker.getData();
                    if(text == null || text.equals("")){
                        boolean connected = socket.isConnected();
                        if(connected){
                            registeredOutputHandler.sendHeartbeatResponse();
                        }else{
                            commune = false;
                        }

                    }
                    System.out.println("\n<server> " + text);
                    if(text != null){
                        if (text.toLowerCase().equals("bye")) {
                            commune = false;
                        }else
                        if(text.equals(Header.createHeartbeatLine())){
                            registeredOutputHandler.sendHeartbeatResponse();
                        }
                        else {
                            Message message = Message.obtain(handler);
                            message.obj = dataChecker;
                            handler.dispatchMessage(message);
                        }
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                Message message = Message.obtain(handler);
                message.obj = ChecklistConnectActivity.ChecklistNetworkHandler.INPUT_CLOSED;
                handler.dispatchMessage(message);
            } finally {
                try {
                    dataInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean stop(){
            try {
                dataInputStream.close();
//                    socketIn.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            try {
                socket.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

    }

    public static class OuputHandler implements Runnable {

        private Socket socket;
        private String name;
        private DataOutputStream socketOut;

        public OuputHandler(String name,Socket socket) {
            this.socket = socket;
            this.name = name;
        }

        public void send(final String data){

            Runnable sender = new Runnable() {
                @Override
                public void run() {
                    if(socket.isBound()){
                        try {
                            if(data.equalsIgnoreCase("checklist")){

                                socketOut.writeUTF(ChecklistTempUtils.createMockChecklist());
                            }else {
                                socketOut.writeUTF(data);
                            }
                            socketOut.flush();
                        } catch (Exception exp) {
                            exp.printStackTrace();

                            try {
                                socketOut.close();
                            } catch (Exception e) {       //todo: real handling
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                            try {
                                socket.close();
                                throw new RuntimeException(exp);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
//                        finally {
//                            try {
//                                socketOut.close();
//                            } catch (Exception e) {
//                            }
//                            try {
//                                socket.close();
//                            } catch (Exception e) {
//                            }
//                        }
                    }
                }
            };

            Thread sendThread = new Thread(sender);
            sendThread.start();

        }



        @Override
        public void run() {

            socketOut = null;
            try {

                socketOut = new DataOutputStream(socket.getOutputStream());
                System.err.println("Sending Header");
                sendHeader(socketOut);

            } catch (Exception exp) {
                exp.printStackTrace();
                try {
                    socketOut.close();
//                    socketIn.close();
                } catch (Exception e) {
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        public boolean stop(){
            try {
                socketOut.writeUTF("bye");
                socketOut.flush();
                socketOut.close();
//                    socketIn.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            try {
                socket.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        private void sendHeader(DataOutputStream dataOutputStream) throws IOException {
            String header = Header.createHeaderLine(this.name);
            dataOutputStream.writeUTF(header);
            dataOutputStream.flush();
            System.out.println("Header sent");
        }

        public void sendHeartbeatResponse() throws IOException {
            String heartbeatResponseLine = Header.createHeartbeatResponseLine();
            socketOut.writeUTF(heartbeatResponseLine);
            socketOut.flush();
            System.out.println("Heartbeat response sent");
        }

    }
}
