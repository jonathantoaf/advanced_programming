package test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
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
        for (int i = uriSegments.length -1; i >= 0; i--) {
            String[] uriSegmentsPrefix = Arrays.copyOf(uriSegments, i+1);
            String uriPrefix = "/" + String.join("/", uriSegmentsPrefix);
            if ((servlet = getRequestMap(httpCommand).get(uriPrefix)) != null) {
                break;
            }
        }
        return servlet;
    }

    private ConcurrentMap<String, Servlet> getRequestMap(String httpCommand) {
        return switch (httpCommand.toUpperCase()) {
            case "GET" -> this.servletsGet;
            case "POST" -> this.servletsPost;
            case "DELETE" -> this.servletsDelete;
            default -> throw new IllegalArgumentException("Invalid http command: " + httpCommand);
        };
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
            this.running = true;
            System.out.println("Server started on port " + this.port);

            while (this.running) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.running = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.threadPool.shutdown();
        try {
            this.threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MyHTTPServer server = new MyHTTPServer(8080, 10);
        server.addServlet("GET", "/hello", new HelloServlet());
        server.addServlet("POST", "/add", new addServlet());
        server.start();

        // To stop the server, you can use a separate thread or signal handling.
        System.out.println("Press Enter to stop the server...");
        System.in.read();
        server.close();
    }
}


class HelloServlet implements Servlet {
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String responseBody = "Hello, world!";
        toClient.write(("HTTP/1.1 200 OK\n" +
                "Content-Type: text/plain\n" +
                "Content-Length: " + responseBody.length() + "\n" +
                "\n" +
                responseBody).getBytes());
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}

class addServlet implements Servlet {
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
//       get parameters
        Map<String, String> params = ri.getParameters();
        String a = params.get("a");
        String b = params.get("b");
        int sum = Integer.parseInt(a) + Integer.parseInt(b);
        String responseBody = "Sum: " + sum;
        toClient.write(("HTTP/1.1 200 OK\n" +
                "Content-Type: text/plain\n" +
                "Content-Length: " + responseBody.length() + "\n" +
                "\n" +
                responseBody).getBytes());
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
