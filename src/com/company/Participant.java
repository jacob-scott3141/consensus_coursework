package com.company;

import com.sun.xml.internal.ws.wsdl.writer.document.Part;

import java.io.PrintWriter;
import java.net.Socket;

public class Participant{
    PrintWriter out;
    Socket socket;
    ParticipantLogger log;
    public static void main(String[] args) throws InterruptedException {
        Participant p = new Participant();
        p.sendMessage("why");
    }
    public Participant(){
        try{
            socket = new Socket("localhost",4322);
            out = new PrintWriter(socket.getOutputStream());
            ParticipantLogger.initLogger(4322,0,10);
            log = ParticipantLogger.getLogger();
        }catch(Exception e){System.out.println("error"+e);}
    }

    public void sendMessage(String msg) throws InterruptedException {
        out.println("TCP message "+ msg); out.flush();
        System.out.println("TCP message "+ msg +" sent");
        log.logMessage(msg);
        Thread.sleep(1000);
    }
}