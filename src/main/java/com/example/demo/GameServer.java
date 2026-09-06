package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 5555;
    private static final int GAME_DURATION_MS = 30_000;


    private static final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    private static final Map<ClientHandler, ClientHandler> pairings = new ConcurrentHashMap<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        System.out.println("Server starting on port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket s = serverSocket.accept();
            ClientHandler handler = new ClientHandler(s);
            pool.submit(handler);
        }
    }

    // send msg to a specific handler (null-safe)
    public static void sendTo(ClientHandler h, String msg) {
        if (h != null) h.sendMessage(msg);
    }

    // check if user is online
    public static boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    // pair two clients into a game session
    public static synchronized void createSession(ClientHandler a, ClientHandler b) {
        if (a == null || b == null) return;
        if (pairings.containsKey(a) || pairings.containsKey(b)) return;

        pairings.put(a, b);
        pairings.put(b, a);

        // assign IDs: a -> 1, b -> 2
        a.sendMessage("ID:1");
        b.sendMessage("ID:2");

        // tell both to start
        a.sendMessage("START");
        b.sendMessage("START");

        // For simplicity keep them in the pair for a fixed duration and then end the game after GAME_DURATION_MS
        pool.submit(() -> {
            try {
                Thread.sleep(GAME_DURATION_MS);
            } catch (InterruptedException ignored) {}
            endSession(a, b);
        });
    }

    public static synchronized void endSession(ClientHandler a, ClientHandler b) {
        if (a != null) a.sendMessage("END");
        if (b != null) b.sendMessage("END");
        pairings.remove(a);
        pairings.remove(b);
    }

    // Remove user on disconnect
    public static synchronized void removeUser(String username, ClientHandler handler) {
        onlineUsers.remove(username, handler);
        // if user was paired, tell partner the other disconnected and cleanup
        ClientHandler partner = pairings.remove(handler);
        if (partner != null) {
            pairings.remove(partner);
            partner.sendMessage("DISCONNECTED");
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private volatile String username = null;
        private volatile boolean running = true;

        // authoritative state per session (kept minimal here)
        private double posX = 350;
        private int score = 0;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    handle(line);
                }
            } catch (IOException e) {
                // ignore
            } finally {
                cleanup();
            }
        }

        private void handle(String line) {
            if (line == null) return;
            try {
                if (line.startsWith("AUTH:")) {
                    String user = line.substring(5).trim();
                    // if already logged in with same name, reject
                    if (onlineUsers.putIfAbsent(user, this) == null) {
                        this.username = user;
                        sendMessage("AUTH_OK");
                        System.out.println(user + " authenticated");
                    } else {
                        sendMessage("AUTH_FAIL:AlreadyOnline");
                    }
                } else if (line.startsWith("INVITE:")) {
                    String target = line.substring(7).trim();
                    if (username == null) { sendMessage("ERROR:NotAuthed"); return; }
                    ClientHandler targetHandler = onlineUsers.get(target);
                    if (targetHandler == null) {
                        sendMessage("INVITE_FAIL:UserOffline");
                    } else if (pairings.containsKey(targetHandler) || pairings.containsKey(this)) {
                        sendMessage("INVITE_FAIL:UserBusy");
                    } else {
                        // send invite to target
                        targetHandler.sendMessage("INVITE_FROM:" + username);
                        sendMessage("INVITE_SENT:" + target);
                    }
                } else if (line.startsWith("INVITE_ACCEPT:")) {
                    String from = line.substring(14).trim(); // who invited this handler
                    ClientHandler inviter = onlineUsers.get(from);
                    if (inviter == null) {
                        sendMessage("INVITE_FAIL:InviterOffline");
                    } else if (pairings.containsKey(inviter) || pairings.containsKey(this)) {
                        sendMessage("INVITE_FAIL:Busy");
                    } else {
                        // pair them and start the session
                        createSession(inviter, this);
                        inviter.sendMessage("INVITE_ACCEPTED:" + username);
                        sendMessage("INVITE_ACCEPTED:" + from);
                    }
                } else if (line.startsWith("INVITE_REJECT:")) {
                    String from = line.substring(14).trim();
                    ClientHandler inviter = onlineUsers.get(from);
                    if (inviter != null) inviter.sendMessage("INVITE_REJECTED:" + username);
                } else if (line.startsWith("POS:")) {
                    // only accept POS updates if in a pair
                    ClientHandler partner = pairings.get(this);
                    if (partner != null) {
                        try {
                            String[] parts = line.split(":");
                            double x = Double.parseDouble(parts[1]);
                            this.posX = x;
                            // send positions to partner so both stay synced: format POS:<myX>:<partnerX>
                            partner.sendMessage("POS:" + partner.posX + ":" + this.posX);
                            // also send to self for local UI sync
                            sendMessage("POS:" + this.posX + ":" + partner.posX);
                        } catch (Exception ignored) {}
                    }
                } else if (line.equals("DESTROY")) {
                    // increment score for this player and notify partner
                    this.score++;
                    ClientHandler partner = pairings.get(this);
                    int s1 = (this == partner) ? 0 : this.score;
                    int s2 = partner != null ? partner.score : 0;
                    // broadcast SCORE to both
                    sendMessage("SCORE:" + this.score + ":" + (partner != null ? partner.score : 0));
                    if (partner != null) partner.sendMessage("SCORE:" + partner.score + ":" + this.score);
                } else if (line.equals("PING")) {
                    sendMessage("PONG");
                } else {
                    // unknown message
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String msg) {
            try {
                out.println(msg);
            } catch (Exception e) {
                // ignore
                running = false;
            }
        }

        private void cleanup() {
            running = false;
            try { socket.close(); } catch (IOException ignored) {}
            if (username != null) removeUser(username, this);
            // also if paired notify partner
            ClientHandler partner = pairings.remove(this);
            if (partner != null) {
                pairings.remove(partner);
                partner.sendMessage("DISCONNECTED");
            }
            System.out.println("Connection closed for " + username);
        }
    }
}
