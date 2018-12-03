package com.example.mtosq.deepbleu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import com.google.gson.Gson;

public class NetworkPlayer extends Player {

    private static Socket clientConnection = new Socket();

    public static synchronized void setClientConnection(Socket clientConnection){
        NetworkPlayer.clientConnection = clientConnection;
    }

    Gson gson = new Gson();

    public NetworkPlayer(String name, boolean isWhite) {
        super(name, isWhite);
        //clientConnection = new Socket();
    }

    public NetworkPlayer(String name, boolean isWhite, Socket clientConnection) {
        super(name, isWhite);
        clientConnection = clientConnection;
    }

    public void connect() {
        try {
            InetAddress ia = InetAddress.getByName("10.0.2.2");
            SocketAddress sa = new InetSocketAddress(ia, 1994);
            clientConnection.connect(sa);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connect(String addr, int port) {
        try {
            InetAddress ia = InetAddress.getByName(addr);
            SocketAddress sa = new InetSocketAddress(ia, port);
            clientConnection.connect(sa);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connect(String addr, int port, String username, String password) {
        try {
            InetAddress ia = InetAddress.getByName(addr);
            SocketAddress sa = new InetSocketAddress(ia, port);
            clientConnection.connect(sa);
            try {
                BufferedWriter buffOut = new BufferedWriter(
                        new OutputStreamWriter( this.getSocket().getOutputStream() ) );
                AuthPair logMeIn = new AuthPair(username, password);
                String moveJson = gson.toJson(logMeIn);
                buffOut.write(moveJson);
                buffOut.newLine();
                buffOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Socket getSocket() {
        return clientConnection;
    }

    @Override
    public ChessMove getMove(Board b) {
        try {
            ChessMove mostRecentMove = b.moveHistory.get(b.moveHistory.size()-1);
            gson.toJson(mostRecentMove);
            try {
                BufferedWriter buffOut = new BufferedWriter(
                        new OutputStreamWriter( this.getSocket().getOutputStream() ) );
                String moveJson = gson.toJson(mostRecentMove);
                System.out.println("Writing ChessMove json to network...");
                System.out.println(moveJson);
                buffOut.write(moveJson);
                buffOut.newLine();
                buffOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            System.out.println("NetworkPlayer trying to read line...");
            String line = null;
            while (line == null)
                line = reader.readLine();
            System.out.println("Network player read this line: " + line);
            ChessMove networkMove = gson.fromJson(line, ChessMove.class);
            return networkMove;
        } catch (IOException e) {
            return null;
        }
    }

}
