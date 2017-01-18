package com.peeterst.android.server;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import com.peeterst.android.xml.ChecklistWriteXML;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 25/05/13
 * Time: 18:38
 *
 */
public class Client {

    public static void main(String[] args) {

        try {
//            Socket master = new Socket("192.168.0.247", 8900);
            Socket master = new Socket("127.0.0.1",54321);

            if(args == null || args.length == 0){
                OutputHandler outputHandler = new OutputHandler("One",master);
                new Thread(outputHandler).start();
                new Thread(new InputHandler("One",master,outputHandler)).start();
            }else {
                OutputHandler outputHandler = new OutputHandler(args[0],master);
                new Thread(outputHandler).start();
                new Thread(new InputHandler(args[0],master,outputHandler)).start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static class InputHandler implements Runnable {

        private Socket socket;
        DataInputStream dataInputStream;
        private String name;
        private OutputHandler registeredOutputHandler;

        public InputHandler(String name,Socket socket,OutputHandler outputHandler) {
            this.socket = socket;
            this.name = name;
            this.registeredOutputHandler = outputHandler;
        }

        @Override
        public void run() {
            boolean commune = true;

//            BufferedReader reader = null;
            try {

                dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (commune) {
                    String text = dataInputStream.readUTF();
                    System.out.println("\n<server> " + text);
                    if (text.toLowerCase().equals("bye")) {
                        commune = false;
                    } else
                    if(text.equals(Header.createHeartbeatLine())){
                        registeredOutputHandler.sendHeartbeatResponse();
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                System.exit(1);
            } finally {
                try {
                    dataInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//        @Override
//        public void run() {
//            boolean commune = true;
//            BufferedReader reader = null;
//            try {
//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                while (commune) {
//                    String text = reader.readLine();
//                    System.out.println("\n<server> " + text);
//                    if (text.toLowerCase().equals("bye")) {
//                        commune = false;
//                    }
//                }
//            } catch (Exception exp) {
//                exp.printStackTrace();
//            } finally {
//                try {
//                    reader.close();
//                } catch (Exception e) {
//                }
//                try {
//                    socket.close();
//                } catch (Exception e) {
//                }
//            }
//        }
    }

    public static class OutputHandler implements Runnable {

        private Socket socket;
        private String name;
        private DataOutputStream socketOut;

        public OutputHandler(String name, Socket socket) {
            this.socket = socket;
            this.name = name;
        }

        public String createMockChecklist(){
            Checklist checklist = new Checklist("test checklist",new Date().getTime());
            checklist.addItem(new ChecklistItem("testbullet"));
            checklist.addItem(new ChecklistItem("testbullet 2"));
            checklist.addItem(new ChecklistItem("testbullet 3"));
            checklist.getItems().get(1).setTaken(true);
            ChecklistWriteXML checklistWriteXML = new ChecklistWriteXML(checklist);
            return checklistWriteXML.createStringVersion();
        }

        @Override
        public void run() {
            boolean commune = true;
//            BufferedWriter writer = null;

            socketOut = null;
            DataInputStream  socketIn = null;
            try {

                socketOut = new DataOutputStream(socket.getOutputStream());
                sendHeader(socketOut);
                socketIn  = new DataInputStream(socket.getInputStream());
                DataInputStream  console   = new DataInputStream(System.in);

                while(commune){



                    boolean done = false;
                    String line = null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(console));
                    StringBuilder stringBuilder = new StringBuilder();
                    while (!done)
                    {
                        line = reader.readLine();
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                        if (line.equalsIgnoreCase(".bye")) {
                            done = true;
                            commune = false;
                        }else if(line.equalsIgnoreCase("checklist")){
                            //todo: stub!
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(createMockChecklist());
                            done = true;
                        }else if(!reader.ready()){
                            break;
                        }
                    }
                    socketOut.writeUTF(stringBuilder.toString());
                    socketOut.flush();
                }


//                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                writer.write("Client");
//                writer.newLine();
//                writer.flush();
//
//
//
//                Scanner scanner = new Scanner(new BufferedInputStream(System.in));
//                while (commune) {
//                    System.out.print("> ");
//                    StringBuilder stringBuilder = new StringBuilder();
////                    while (scanner.hasNext()){
//                        stringBuilder.append(scanner.next());
////                    }
//
//
//                    String text = stringBuilder.toString();
////                    scanner.hasNext()
//                    writer.write(text);
//                    writer.newLine();
//                    writer.flush();
//                    if (text.equalsIgnoreCase("bye")) {
//                        commune = false;
//                    }
//                }
            } catch (Exception exp) {
                exp.printStackTrace();
            } finally {
                try {
                    socketOut.close();
                    socketIn.close();
                } catch (Exception e) {
                }
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }

        private void sendHeader(DataOutputStream dataOutputStream) throws IOException {
            String header = Header.createHeaderLine(this.name);
            dataOutputStream.writeUTF(header);
            dataOutputStream.flush();

        }

        public void sendHeartbeatResponse() throws IOException {
            String heartbeatResponseLine = Header.createHeartbeatResponseLine();
            socketOut.writeUTF(heartbeatResponseLine);
            socketOut.flush();
            System.out.println("Heartbeat response sent");
        }

    }
}
