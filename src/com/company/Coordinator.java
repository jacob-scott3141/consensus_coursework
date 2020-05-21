package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Coordinator{
    static ArrayList<Integer> ports = new ArrayList();
    static Object joinLock = new Object();
    static Map<Integer,Socket> sockets = new HashMap<>();
    static String options;

    public static void main(String [] args) {
        /*
        *
        * */
        try {
            ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
            int maxParticipants = Integer.parseInt(args[2]);
            int timeout = Integer.parseInt(args[3]);

            for(String s : args){
                System.out.print(s + " ");
            }
            System.out.println();

            options = "";
            for(String option : Arrays.copyOfRange(args, 4, args.length)){
                options = options + " " + option;
            }

            for (int i = 0; i < maxParticipants; i++) {
                try {
                    Socket client = ss.accept();
                    new Thread(new ServiceThread(client)).start();
                }
                catch (Exception e) {
                    System.out.println("error " + e);
                }
            }
            synchronized (joinLock){
                joinLock.wait(timeout);
                System.out.println(ports);
            }

            establish(ports);

            for(int i : ports){
                String details = "";
                for(Integer j : ports){
                    if(i != j){
                        details = details + " " + j.toString();
                    }
                }
                send("DETAILS" + details, i);
            }

            send("VOTE_OPTIONS" + options, ports);

        } catch (Exception e) {
            System.out.println("error " + e);
        }

    }

    static void establish(int port) throws IOException {
        Socket socket = new Socket("localhost",port);
        sockets.put(port,socket);
    }

    static void establish(ArrayList<Integer> ports) throws IOException {
        for(int port : ports) {
            Socket socket = new Socket("localhost", port);
            sockets.put(port, socket);
        }
    }

    static void send(String message, int port) throws IOException {
        PrintWriter out = new PrintWriter(sockets.get(port).getOutputStream());
        out.println("COORDINATOR: " + message); out.flush();
        System.out.println("TCP message " + message + " sent");
    }

    static void send(String message, ArrayList<Integer> ports) throws IOException {
        for(int port : ports) {
            PrintWriter out = new PrintWriter(sockets.get(port).getOutputStream());

            out.println("COORDINATOR: " + message);
            out.flush();
            System.out.println("TCP message " + message + " sent");
        }
    }

    static class ServiceThread implements Runnable{
        Socket client;
        ServiceThread(Socket c){client=c;}
        public void run(){
            try{
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line;
                while((line = in.readLine()) != null){
                    System.out.println(line+" received");
                    boolean addPort = true;
                    int newPort = Integer.parseInt(line.split(" ")[1]);
                    for(Integer i : ports){
                        if(newPort == i){
                            addPort = false;
                            break;
                        }
                    }

                    if(addPort){
                        ports.add(newPort);
                    }
                    synchronized (joinLock){
                        joinLock.notify();
                    }

                }
                client.close();
            }
            catch(Exception e){}
        }
    }
}