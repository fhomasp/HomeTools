package com.peeterst.android.server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 16/07/13
 * Time: 20:24
 */
public class Heartbeat {

    private int interval;
    private Socket socket;
    private boolean alive = false;
    public boolean responseReceived = true;

    private DataOutputStream outputStream;
    private HeartbeatSender sender;

    public Heartbeat(int interval, Socket socket) {
        this.interval = interval;
        this.socket = socket;
    }

    public void startBeating() throws IOException {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        sender = new HeartbeatSender();
        Thread senderThread = new Thread(sender);
        senderThread.start();
    }

    public void stop(){
        sender.commune = false;
        sender.cancel();
    }

//    public void throwIOException(){
//        throw new IOException("Heartbeat time exceeded!");
//    }

    public void reset() {
        responseReceived = true;
        sender.reset();
    }

    private class HeartbeatSender implements Runnable{


        private boolean commune;
        private HeartbeatSenderClient heartbeatSenderClient;
        private HeartbeatChecker heartbeatChecker;
        boolean wait = false;
        private Timer timer;

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {

            commune = true;
            while(commune){
                try {

                    if(responseReceived) {
                        if(wait){
                            Thread.sleep(interval*1000);
                            wait = false;
                            if(!commune){
                                break;
                            }
                        }
                        if(timer == null) {
                            timer = new Timer();
                        }
                        heartbeatSenderClient = new HeartbeatSenderClient();
                        heartbeatChecker = new HeartbeatChecker();
                        timer.schedule(heartbeatSenderClient, 0);
                        timer.schedule(heartbeatChecker, interval * 1000);
                        responseReceived = false;
                    }
                    System.out.println("heartbeatsender pulse");
                    Thread.sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                    commune = false;
                    if(timer != null) {
                        timer.cancel();
                    }
//                    throw new RuntimeException(e);
                }
            }

        }

        public void cancel(){
            if(heartbeatChecker != null){
                heartbeatChecker.cancel();
            }
            if(heartbeatSenderClient != null){
                heartbeatSenderClient.cancel();
                heartbeatSenderClient.stop();
            }
            if(timer != null){
                timer.cancel();
            }
        }

        public void reset(){
            wait = true;
            if(heartbeatChecker != null){
                heartbeatChecker.cancel();
            }
            if(heartbeatSenderClient != null ){
                heartbeatSenderClient.cancel();
            }
            wait = true;
        }

        private class HeartbeatSenderClient extends TimerTask {

            @Override
            public void run() {
                String msg = Header.createHeartbeatLine();
                try {
                    outputStream.writeUTF(msg);
                    outputStream.flush();

                } catch (Exception e) {
                    try {
                        outputStream.close();
                        System.out.println("senderclient exception closed outputstream");
                        this.cancel();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }

            public void stop() {
                try {
                    outputStream.close();
                    System.out.println("senderclient closed outputstream");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private class HeartbeatChecker extends TimerTask {

            @Override
            public void run() {
                if(responseReceived){
                    System.out.println("response received within timeout");
                }else {
                    try {
                        System.out.println("heartbeat timeout exceeded");
                        commune = false;
                        outputStream.close();
                        System.out.println("checker closed outputstream");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

            }
        }
    }



}
