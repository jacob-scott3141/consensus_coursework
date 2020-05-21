package com.company;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.wsdl.writer.document.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Participant{
    PrintWriter out;
    static ServerSocket server;
    Socket socket;
    ParticipantLogger log;
    static int order = 0;

    static AtomicInteger participantsFinished = new AtomicInteger(0);
    static AtomicInteger roundNumber = new AtomicInteger(0);

    static Object arrayLock = new Object();

    static int port;
    int serverPort;
    static int timeOut;

    static Random rand = new Random();

    static Vote vote;
    static ArrayList<String> details = new ArrayList<>();
    static ArrayList<String> voteOptions = new ArrayList<>();

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
        }catch(Exception e){e.printStackTrace();}
    }

    public static void main(String[] args){
        try {
            Participant p = new Participant(Integer.parseInt(args[2]), Integer.parseInt(args[0]), Integer.parseInt(args[3]));
            p.initialise();
            synchronized (arrayLock) {
                arrayLock.wait();
            }

            for(String s : details){
                if(Integer.parseInt(s) < port){
                    order += 1;
                }
            }
            System.out.println("order number " + order);
            vote = new Vote(port,voteOptions.get(rand.nextInt(voteOptions.size())));
            System.out.println(vote);

            p.accept();
            p.sendVote();

            while(participantsFinished.get() < details.size()){

            }
            System.out.println(":D");
        }
        catch(InterruptedException e){e.printStackTrace();}

    }

    public void join() throws InterruptedException {
        sendMessage("JOIN "+port);
    }

    public void initialise(){
        try {
            Socket client = server.accept();
            Thread t = new Thread(new ServerThread(client));
            t.start();
        }
        catch (IOException e) {e.printStackTrace();}
    }

    public void accept(){
        Thread t = new Thread(new AcceptThread());
        t.start();
    }

    private void sendMessage(String msg) throws InterruptedException {
        out.println(msg); out.flush();
        System.out.println(msg);
        log.logMessage(msg);
        //Thread.sleep(1000);
    }

    private void sendVote(){
        try{
            for(String s : details) {
                Socket client = new Socket("localhost", Integer.parseInt(s));

                out = new PrintWriter(client.getOutputStream());

                out.println(vote); out.flush();
                log.logMessage(vote.toString());
            }
        }
        catch(Exception e){e.printStackTrace();}
    }

    static class AcceptThread implements Runnable{

        @Override
        public void run() {
            for (int i = 0; i < details.size(); i++) {
                try {
                    System.out.println("Starting process " + i);
                    Socket client = server.accept();
                    new Thread(new ClientThread(client)).start();
                }
                catch (Exception e) {e.printStackTrace();}
            }
        }
    }

    static class ServerThread implements Runnable{
        Socket client;

        ServerThread(Socket client){this.client = client;}

        ArrayList<String> temp1 = new ArrayList<>();
        ArrayList<String> temp2 = new ArrayList<>();

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line;
                Boolean exit = false;
                while (!exit) {
                    line = in.readLine();
                    System.out.println(line);
                    try {
                        String[] messages = line.split(" ");
                        if (messages[0].equals("DETAILS")) {
                            for(int i = 1; i < messages.length; i++){
                                temp1.add(messages[i]);
                            }
                            System.out.println("details recieved");
                        }
                        if (messages[0].equals("VOTE_OPTIONS")) {
                            for(int i = 1; i < messages.length; i++){
                                temp2.add(messages[i]);
                            }
                            System.out.println("vote options recieved");
                            exit = true;
                        }

                    } catch (Exception e) {e.printStackTrace();}
                }
                client.close();
                details.addAll(temp1);
                voteOptions.addAll(temp2);
                synchronized (arrayLock){
                    arrayLock.notify();
                }
            }catch(Exception e){e.printStackTrace();}
        }
    }

    static class ClientThread implements Runnable{
        Socket client;

        ClientThread(Socket client){this.client = client;}

        @Override
        public void run() {
            try {
                client.setSoTimeout(timeOut);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line;
                Boolean cont = true;
                try {
                    while ((line = in.readLine()) != null && cont) {

                        System.out.println("recieved: " + line);
                        int sender = Integer.parseInt(line.split(",")[0].substring(1));

                    }
                }
                catch(SocketTimeoutException e){
                    participantsFinished.incrementAndGet();
                }
                client.close();
            }catch(Exception e){e.printStackTrace();}
        }
    }
}