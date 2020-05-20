package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Coordinator{
    static ArrayList<Integer> ports = new ArrayList();
    static Object lock1 = new Object();

    public static void main(String [] args) {
        /*
        *
        * */
        try {
            ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
            int maxParticipants = Integer.parseInt(args[2]);
            int timeout = Integer.parseInt(args[3]);
            for (int i = 0; i < maxParticipants; i++) {
                try {
                    Socket client = ss.accept();
                    new Thread(new ServiceThread(client)).start();
                } catch (Exception e) {
                    System.out.println("error " + e);
                }
            }
            synchronized (lock1){
                lock1.wait();
                System.out.println(ports);
            }

            for(int i : ports){
                ack("ack",i);
            }

        } catch (Exception e) {
            System.out.println("error " + e);
        }

    }

    static void ack(String message, int port) throws IOException {
        Socket socket = new Socket("localhost",port);
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        out.println("COORDINATOR: " + message); out.flush();
        System.out.println("TCP message " + message + " sent");
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
                    synchronized (lock1){
                        lock1.notify();
                    }

                }
                client.close();
            }
            catch(Exception e){}
        }
    }
}