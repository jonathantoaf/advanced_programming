package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        try {
            String[] firstLine = reader.readLine().split(" ");
            String httpCommand = firstLine[0];
            String uri = firstLine[1];
            // Split URI into path and query parameters
            String[] uriParts = uri.split("\\?");
            String[] uriSegments = RequestParser.parseUriSegments(uriParts[0]);
            Map<String, String> parameters = new HashMap<>();
            if (uriParts.length > 1) {
                parameters = RequestParser.parseParameters(uriParts[1]);
            }
//            parse header
            Map<String, String> headers = new HashMap<>();
            String line = "";
            int contentLength = 0;
            while (!(line = reader.readLine()).isEmpty()) {
                String[] headerParts = line.split(": ");
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                    if (headerParts[0].equalsIgnoreCase("Content-Length")) {
                        contentLength = Integer.parseInt(headerParts[1]);
                    }
                }
            }
            StringBuilder content = new StringBuilder();
            if (contentLength > 0) {
                while (!(line = reader.readLine()).isEmpty()) {
                    if (line.contains("filename=")) {
                        parameters.put("filename", line.split("filename=")[1]);
                    }
                }
                while (!(line = reader.readLine()).isEmpty()) {
                    content.append(line).append("\n");
                }
                while (reader.ready()) {
                    reader.readLine();
                }
            }
            return new RequestInfo(httpCommand, uri, uriSegments, parameters, content.toString().getBytes());
        } catch (Exception e) {
            throw new IOException("Error parsing request invalid format:" + e.getMessage());
        }
    }

    private static void skipHeaders(BufferedReader reader) throws IOException {
        while (!reader.readLine().isEmpty()) {
            // Skip headers
        }
    }

    private static String[] parseUriSegments(String uriPart) {
        return uriPart.substring(1).split("/");
    }

    private static Map<String, String> parseParameters(String uriPart) {
        Map<String, String> parameters = new java.util.HashMap<>();
        String[] parametersString = uriPart.split("&");
        for (String parameter : parametersString) {
            String[] keyValue = parameter.split("=");
            parameters.put(keyValue[0], keyValue[1]);
        }
        return parameters;
    }

    private static byte[] parseContent(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line = reader.readLine();
        while (!line.isEmpty()) {
            content.append(line).append("\n");
            line = reader.readLine();
        }
        return content.toString().getBytes();
    }

    // RequestInfo given internal class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
