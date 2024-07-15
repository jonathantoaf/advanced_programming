package servlets;

import test.Message;
import test.RequestParser;
import test.Servlet;
import test.TopicManagerSingleton;
import views.HtmlTableWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;



public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        if (ri.getParameters().size() == 2) {
            try {
                String topic = ri.getParameters().get("topic");
                Message message = new Message(ri.getParameters().get("message"));
//                TopicManagerSingleton.get().getTopic(topic).publish(message);
                Collection<test.Topic> topics = TopicManagerSingleton.get().getTopics();
                ArrayList<String> topicNames = new ArrayList<>();
                for (test.Topic t : topics) {
                    topicNames.add(t.name);
                }
                ArrayList<String> tableHtml = HtmlTableWriter.getTableHtml(topicNames);
                System.out.println("Html generated for table");
                toClient.write("HTTP/1.1 200 OK\r\n".getBytes());
                toClient.write("Content-Type: text/html\r\n".getBytes());
                toClient.write("\r\n".getBytes());
                for (String line : tableHtml) {
                    toClient.write(line.getBytes());
                    toClient.write("\n".getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: " + e.getMessage());
                toClient.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes());
                toClient.write("Content-Type: text/html\r\n".getBytes());
                toClient.write("\r\n".getBytes());
                toClient.write("<html><body><h1>500 Internal Server Error</h1><p>".getBytes());
                toClient.write(e.getMessage().getBytes());
            }

        } else {
            toClient.write("HTTP/1.1 400 Bad Request\r\n".getBytes());
            toClient.write("Content-Type: text/html\r\n".getBytes());
            toClient.write("\r\n".getBytes());
            toClient.write("<html><body><h1>400 Bad Request</h1><p>No content received in the request.</p></body></html>".getBytes());
        }
    }

    @Override
    public void close() throws IOException {

    }
}
