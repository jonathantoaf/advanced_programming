package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import server.MyHTTPServer;
import server.RequestParser;
import servlets.Servlet;
import server.RequestParser.RequestInfo;


public class MainTrain5 { // RequestParser
    

    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                            "Host: example.com\n" +
                            "Content-Length: 5\n"+
                            "\n" +
                            "filename=\"hello_world.txt\"\n"+
                            "\n" +
                            "hello world!\n"+
                            "\n" ;

        BufferedReader input=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestInfo requestInfo = RequestParser.parseRequest(input);

            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            } 
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            } 
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }        
    }


    public static void testServer() throws Exception{
//        show how much treads are running
        System.out.println("Number of threads: " + Thread.activeCount());
        int threads = Thread.activeCount();
        // Test server

		MyHTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/hello", new HelloServlet());
        server.addServlet("GET", "/add", new addServlet());
        server.start();

        // Test hello servlet send a http request to the server and check the response body are equal to "Hello, world!"
        Socket clientSocket = new Socket("localhost", 8080);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.println("GET /hello HTTP/1.1");
        out.println("Host: localhost");
        out.println();

        String response = in.readLine();
        if (!response.equals("HTTP/1.1 200 OK")) {
            System.out.println("Hello servlet test failed (-10)");
        }
        String line;
        StringBuilder responseBody = new StringBuilder();

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
        }
        while ((line = in.readLine()) != null) {
            responseBody.append(line);
        }
        if (!responseBody.toString().equals("Hello, world!")) {
            System.out.println("Hello servlet test failed (-10)");
        }
        in.close();
        out.close();
        clientSocket.close();

//        close the server wait 2 sec and check if num of threads equal

        server.close();
        Thread.sleep(2000);
        System.out.println("Number of threads: " + Thread.activeCount());

        if(Thread.activeCount() != threads){
            System.out.println("Server did not close all threads (-10)");
        }


    }
    
    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try{
            testServer(); // 60
        }catch(Exception e){
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
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