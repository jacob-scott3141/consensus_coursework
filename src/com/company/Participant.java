package com.company;

import java.io.PrintWriter;
import java.net.Socket;

class TCPSender{
    public static void main(String [] args){
        try{
            Socket socket = new Socket("koestler.ecs.soton.ac.uk",4322);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            for(int i=0;i<10;i++){
                out.println("TCP message "+i); out.flush();
                System.out.println("TCP message "+i+" sent");
                Thread.sleep(1000);
            }
        }catch(Exception e){System.out.println("error"+e);}
    }
}