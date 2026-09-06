package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class MultiplayerClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;
    private Consumer<String> onMessage;

    public MultiplayerClient(String host, int port, Consumer<String> onMessage) throws IOException {
        this.onMessage = onMessage;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        listenThread = new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    onMessage.accept(msg);
                }
            } catch (IOException e) {
                onMessage.accept("DISCONNECTED");
            }
        }, "ListenerThread");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void send(String msg) {
        if (out != null) out.println(msg);
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
