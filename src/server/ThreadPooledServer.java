package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable {

    private final int serverPort;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean isStopped;

    /**
     * Constructor
     *
     * @param port port of the server
     */
    public ThreadPooledServer(int port) {
        serverPort = port;
        isStopped = false;

        //Maximum clients to handle at the same time.
        threadPool = Executors.newFixedThreadPool(100);
        //newFixedThreadPool can be replaced by newCachedThreadPool if 
        //there should be no limit
    }

    /**
     * Stop the server.(thread-safe method)
     */
    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * Run the server: 1) create the server 2) while server is online: accept
     * clients and handle them in ClientHandlerRunnable class
     */
    @Override
    public void run() {
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            threadPool.execute(
                    new ClientHandlerRunnable(clientSocket));
        }
        threadPool.shutdown();
        System.out.println("Server is offline.");
    }

    /**
     * Check if server is stopped.(thread-safe method)
     *
     * @return true if server is stopped, false - otherwise
     */
    private synchronized boolean isStopped() {
        return isStopped;
    }

    /**
     * initialize server socket on the given(in the constructor) port
     */
    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port", e);
        }
    }
}
