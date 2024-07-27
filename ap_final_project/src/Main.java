import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import server.HTTPServer;
import server.MyHTTPServer;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader(Path.of("config_files")));
        server.addServlet("GET", "/app", new HtmlLoader("html_files"));

        server.start();
        System.in.read();
        server.close();

    }
}
