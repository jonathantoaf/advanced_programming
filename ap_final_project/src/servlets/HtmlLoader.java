package servlets;

import server.RequestParser;

import java.nio.file.Path;


public class HtmlLoader implements Servlet {
    private Path html_files_path;

    public HtmlLoader(String html_files_path) {
        this.html_files_path = Path.of(html_files_path);
        if (!this.html_files_path.toFile().exists()) {
            this.html_files_path.toFile().mkdir();
        }
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, java.io.OutputStream toClient) throws java.io.IOException {
//        get the file name from the uri
        System.out.println("get html file request");
        String[] uriSegments = ri.getUriSegments();
        String fileName = uriSegments[uriSegments.length - 1];
        System.out.println("get request for file: " + fileName);
        if (this.checkFileExists(fileName)) {
            System.out.println("File found");
            byte[] fileContent = java.nio.file.Files.readAllBytes(this.html_files_path.resolve(fileName));
            toClient.write("HTTP/1.1 200 OK\r\n".getBytes());
            toClient.write("Content-Type: text/html\r\n".getBytes());
            toClient.write(("Content-Length: " + fileContent.length + "\r\n").getBytes());
            toClient.write("\r\n".getBytes());
            toClient.write(fileContent);
        } else {
            System.out.println("File not found");
            byte[] fileContent = java.nio.file.Files.readAllBytes(this.html_files_path.resolve("404.html"));
            toClient.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            toClient.write("Content-Type: text/html\r\n".getBytes());
            toClient.write(("Content-Length: " + fileContent.length + "\r\n").getBytes());
            toClient.write("\r\n".getBytes());
            toClient.write(fileContent);

        }

    }

    @Override
    public void close() throws java.io.IOException {
        System.out.println("Closing HtmlLoader");
    }

    private boolean checkFileExists(String fileName) {
        return this.html_files_path.resolve(fileName).toFile().exists();
    }
}