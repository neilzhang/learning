package com.neil.learning.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class NetIODemo {

    public static void main(String[] args) {
        Server server = null;
        try {
            server = new Server(8080);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //等待服务启动完成
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (Client client = new Client("127.0.0.1", 8080)) {
            String request = "hello server";
            System.out.println("send request to the server: " + request);
            String response = client.write(request.getBytes(StandardCharsets.UTF_8));
            System.out.println("get response from the server: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stop();
    }

    static class Server {

        private ServerSocket serverSocket;

        private Thread serverThread;

        private volatile boolean isStop = false;

        public Server(int port) throws IOException {
            this.serverSocket = new ServerSocket(port);
        }

        public void start() {
            if (serverThread == null) {
                serverThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (!isStop) {
                                Socket accept = serverSocket.accept();
                                new Thread(new ServerHandler(accept)).start();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            stop();
                        }
                    }
                });
            }
            serverThread.setDaemon(false);
            serverThread.start();
        }

        public void stop() {
            if (!isStop) {
                isStop = true;

                if (!Thread.currentThread().equals(serverThread)) {
                    serverThread.interrupt();
                }
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ServerHandler implements Runnable {

        private Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] data = new byte[1024];
                inputStream.read(data);
                System.out.println("server received data: " + new String(data, StandardCharsets.UTF_8));

                OutputStream outputStream = socket.getOutputStream();
                String response = "hello client";
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                System.out.println("server return data: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Client implements Closeable {

        private Socket socket;

        public Client(String host, int port) throws IOException {
            this.socket = new Socket(host, port);
        }

        public String write(byte[] data) throws IOException {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();

            //等待客户端得到响应
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            InputStream inputStream = socket.getInputStream();
            byte[] response = new byte[1024];
            inputStream.read(response);
            return new String(response, StandardCharsets.UTF_8);
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}
