package com.company;

import com.sun.xml.internal.ws.wsdl.writer.document.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Participant{
    PrintWriter out;
    ServerSocket server;
    Socket socket;
    ParticipantLogger log;
    int port;
    int serverPort;
    int timeOut;

    public Participant(int port, int serverPort, int timeOut){
        this.serverPort = serverPort;
        this.port = port;
        this.timeOut = timeOut;
        try{
            socket = new Socket("localhost",serverPort);
            server = new ServerSocket(port);

            out = new PrintWriter(socket.getOutputStream());
            ParticipantLogger.initLogger(serverPort, port, timeOut);
            log = ParticipantLogger.getLogger();

            this.join();
        }catch(Exception e){System.out.println("error"+e);}
    }

    public static void main(String[] args){
        Participant p = new Participant(Integer.parseInt(args[2]), Integer.parseInt(args[1]), Integer.parseInt(args[3]));
        p.accept();
    }

    public void join() throws InterruptedException {
        sendMessage("JOIN "+port);
    }

    public void accept(){
        try{
            server.accept();
            ServerSocket ss = new ServerSocket(4322);
            for(;;) {
                try {
                    Socket client = ss.accept();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null)
                        System.out.println(line + " received");
                    client.close();
                }
                catch(Exception e){}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String msg) throws InterruptedException {
        out.println(msg); out.flush();
        System.out.println(msg);
        log.logMessage(msg);
        //Thread.sleep(1000);
    }
}