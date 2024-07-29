/**
 * This package contains classes related to the HTTP server implementation.
 * It includes the `MyHTTPServer` class, which serves as the core component
 * for handling HTTP requests and managing servlets.
 */
package server;

import servlets.Servlet;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The MyHTTPServer class is a simple multi-threaded HTTP server implementation.
 * It supports GET, POST, and DELETE HTTP methods, allowing the registration of
 * servlets to handle specific URI paths for these methods.
 *
 * <p>Each incoming request is processed in a separate thread using an ExecutorService.</p>
 */
public class MyHTTPServer extends Thread implements HTTPServer {

    private int port;
    private final ConcurrentMap<String, Servlet> servletsGet = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Servlet> servletsPost = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Servlet> servletsDelete = new ConcurrentHashMap<>();
    private ExecutorService threadPool;
    private volatile boolean running;
    private ServerSocket serverSocket;

    /**
     * Constructs a new MyHTTPServer instance.
     *
     * @param port     the port on which the server will listen for incoming connections
     * @param nThreads the number of threads to allocate in the thread pool
     */
    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
        this.running = false;
    }

    /**
     * Adds a servlet to handle a specific HTTP command and URI.
     *
     * @param httpCommand the HTTP command (e.g., "GET", "POST", "DELETE")
     * @param uri         the URI path the servlet will handle
     * @param s           the servlet instance to handle the request
     */
    public void addServlet(String httpCommand, String uri, Servlet s) {
        getRequestMap(httpCommand).put(uri, s);
    }

    /**
     * Removes a servlet that was handling a specific HTTP command and URI.
     *
     * @param httpCommand the HTTP command (e.g., "GET", "POST", "DELETE")
     * @param uri         the URI path the servlet was handling
     */
    public void removeServlet(String httpCommand, String uri) {
        getRequestMap(httpCommand).remove(uri);
    }

    /**
     * Retrieves the appropriate servlet for the given request information.
     *
     * @param ri the request information
     * @return the servlet to handle the request, or null if no matching servlet is found
     */
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

    /**
     * Returns the map of servlets for the given HTTP command.
     *
     * @param httpCommand the HTTP command (e.g., "GET", "POST", "DELETE")
     * @return the map of servlets for the specified HTTP command
     */
    private ConcurrentMap<String, Servlet> getRequestMap(String httpCommand) {
        String upperHttpCommand = httpCommand.toUpperCase();
        ConcurrentMap<String, Servlet> servlets;
        switch (upperHttpCommand) {
            case "GET":
                servlets = this.servletsGet;
                break;
            case "POST":
                servlets = this.servletsPost;
                break;
            case "DELETE":
                servlets = this.servletsDelete;
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP command: " + httpCommand);
        }
        return servlets;
    }

    /**
     * Handles a client connection, parsing the request and invoking the appropriate servlet.
     *
     * @param clientSocket the client socket connection
     */
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

    /**
     * Starts the server, listening for incoming connections and dispatching them to handler threads.
     */
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

    /**
     * Closes all registered servlets.
     */
    private void closeServlets() {
        for (Servlet servlet : servletsGet.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Servlet servlet : servletsPost.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Servlet servlet : servletsDelete.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the server and shuts down all resources.
     */
    public void close() {
        this.running = false;
        this.closeServlets();
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
