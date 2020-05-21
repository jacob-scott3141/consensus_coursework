package com.company;

import com.sun.xml.internal.ws.wsdl.writer.document.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Participant{
    PrintWriter out;
    ServerSocket server;
    Socket socket;
    ParticipantLogger log;

    static Object arrayLock = new Object();

    static int port;
    int serverPort;
    int timeOut;

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

            out = new PrintWriter(socket.getOutputStream());
            ParticipantLogger.initLogger(serverPort, port, timeOut);
            log = ParticipantLogger.getLogger();

            this.join();
        }catch(Exception e){System.out.println("error"+e);}
    }

    public static void main(String[] args){
        try {
            Participant p = new Participant(Integer.parseInt(args[2]), Integer.parseInt(args[0]), Integer.parseInt(args[3]));
            p.initialise();
            synchronized (arrayLock) {
                arrayLock.wait();
            }
        }
        catch(InterruptedException e){e.printStackTrace();}
        vote = new Vote(port,voteOptions.get(rand.nextInt(voteOptions.size())));
        System.out.println(vote);
    }

    public void join() throws InterruptedException {
        sendMessage("JOIN "+port);
    }

    public void initialise(){
        try {
            ServerSocket ss = new ServerSocket(port);
            Socket client = ss.accept();
            Thread t = new Thread(new ServerThread(client));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void accept(){
        try{
            ServerSocket ss = new ServerSocket(port);
            for(;;) {

                Socket client = ss.accept();
                Thread t = new Thread(new ClientThread(client));
                t.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sendMessage(String msg) throws InterruptedException {
        out.println(msg); out.flush();
        System.out.println(msg);
        log.logMessage(msg);
        //Thread.sleep(1000);
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
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("recieved: " + line);
                }
                client.close();
            }catch(Exception e){}
        }
    }
}