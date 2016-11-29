package com.dongshujin.demo.test.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dongsj on 16/11/29.
 */
public class PlainEchoServer {

    public void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);              //#1
        try {
            while (true) {
                final Socket clientSocket = socket.accept();             //#2
                System.out.println("Accepted connection from " + clientSocket);
                new Thread(new Runnable() {                              //#3
                    @Override
                    public void run() {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                            while(true) {                                //#4
                                writer.println(reader.readLine());
                                writer.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        } }
                }).start();                                              //#5
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainEchoServer().serve(8989);
    }
}
