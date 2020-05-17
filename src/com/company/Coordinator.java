package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Coordinator {
    public static void main(String [] args){
        try{
            ServerSocket ss = new ServerSocket(4322);
            for(;;){
                try{
                    Socket client = ss.accept();
                    new Thread(new ServerThread(client)).start();
                }
                catch(Exception e){
                    System.out.println(e);
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    static class ServerThread implements Runnable{
        Socket client;

        public ServerThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try{
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String line;

                PrintWriter out = new PrintWriter(client.getOutputStream());
                while((line = in.readLine()) != null){
                    System.out.print("\n" + line+" received from " + client.getInetAddress());
                    Thread.sleep(1000);
                    out.println("Acknowleded"); out.flush();
                    System.out.println(" (Acknowledged)");
                }
                client.close();
            }
            catch(Exception e){

            }
        }
    }
}
