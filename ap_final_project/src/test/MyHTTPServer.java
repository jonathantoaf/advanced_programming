package test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.*;


public class MyHTTPServer extends Thread implements HTTPServer {

    private int port;
    private final ConcurrentMap<String, Servlet> servletsGet = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Servlet> servletsPost = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Servlet> servletsDelete = new ConcurrentHashMap<>();
    private ExecutorService threadPool;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
        this.running = false;
    }

    public void addServlet(String httpCommand, String uri, Servlet s) {
        getRequestMap(httpCommand).put(uri, s);
    }

    public void removeServlet(String httpCommand, String uri) {
        getRequestMap(httpCommand).remove(uri);
    }

    private Servlet getServlet(RequestParser.RequestInfo ri) {
        String httpCommand = ri.getHttpCommand();
        String[] uriSegments = ri.getUriSegments();
        Servlet servlet = null;
        for (int i = uriSegments.length - 1; i >= 0; i--) {
            String[] uriSegmentsPrefix = Arrays.copyOf(uriSegments, i + 1);
            String uriPrefix = "/" + String.join("/", uriSegmentsPrefix);
            if ((servlet = getRequestMap(httpCommand).get(uriPrefix)) != null) {
                break;
            }
        }
        return servlet;
    }

    private ConcurrentMap<String, Servlet> getRequestMap(String httpCommand) {
        String upperHttpCommand = httpCommand.toUpperCase();
        ConcurrentMap<String, Servlet> servlets;
        if (upperHttpCommand.equals("GET")) {
            servlets = this.servletsGet;
        } else if (upperHttpCommand.equals("POST")) {
            servlets = this.servletsPost;
        } else if (upperHttpCommand.equals("DELETE")) {
            servlets = this.servletsDelete;
        } else {
            throw new IllegalArgumentException("Invalid HTTP command: " + httpCommand);
        }
        return servlets;
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream output = clientSocket.getOutputStream()) {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            Servlet servlet = this.getServlet(requestInfo);
            if (servlet != null) {
                servlet.handle(requestInfo, output);
            } else {
                String responseBody = "404 Not Found";
                output.write(("HTTP/1.1 404 Not Found\n" +
                        "Content-Type: text/plain\n" +
                        "Content-Length: " + responseBody.length() + "\n" +
                        "\n" +
                        responseBody).getBytes());
                output.flush();
            }
            input.close();
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setSoTimeout(1000); // Set the timeout to 1 second
            this.running = true;
            System.out.println("Server started on port " + this.port);

            while (this.running) {
                try {
                    Socket clientSocket = this.serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (SocketTimeoutException e) {
                    // Timeout occurred, check if the server is still running
                    // Continue to the next iteration of the loop
                } catch (IOException e) {
                    if (this.running) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Server socket closed, stopping server.");
                    }
                }
            }
            System.out.println("Server stopped");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                    this.serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        this.running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("ExecutorService did not terminate");
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

}




