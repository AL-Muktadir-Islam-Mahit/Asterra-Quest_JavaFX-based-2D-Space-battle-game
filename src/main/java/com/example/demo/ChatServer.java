
// alwys run this first 1!!!!!!

package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static Set<PrintWriter> clients = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println(" ok done ! start hoise " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("cilent done also !");
            new ClientHandler(client).start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clients) {
                    clients.add(out);
                }

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("💬 " + msg);
                    synchronized (clients) {
                        for (PrintWriter client : clients) {
                            client.println(msg);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("discont properly !");
            } finally {
                synchronized (clients) {
                    clients.remove(out);
                }
            }
        }
    }
}
