package views;

import configs.Graph;
import configs.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HtmlGraphWriter {

    public static ArrayList<String> getGraphHTML(Graph graph) {
        ArrayList<String> html = new ArrayList<>();
        try {
            // Read the template HTML
            List<String> lines = Files.readAllLines(Paths.get("html_files/graph.html"));
            for (String line : lines) {
                if (line.trim().equals("// Nodes will be inserted here by Java code")) {
                    for (Node node : graph) {
                        String nodeName = node.getName().substring(1); // Remove the initial 'T' or 'A'
                        if (node.getName().startsWith("T")) {
                            html.add(String.format("{ id: '%s', label: '%s', shape: 'box', color: '#FFC1B1', font: { size: 20 }, widthConstraint: { minimum: 30 }, heightConstraint: { minimum: 30 } },", nodeName, nodeName));

                        } else {
                            html.add(String.format("{ id: '%s', label: '%s', shape: 'circle', color: '#A1C1F1', font: { size: 20 } },", nodeName, nodeName));
                        }
                    }
                } else if (line.trim().equals("// Edges will be inserted here by Java code")) {
                    for (Node node : graph) {
                        String fromNodeName = node.getName().substring(1); // Remove the initial 'T' or 'A'
                        for (Node edge : node.getEdges()) {
                            String toNodeName = edge.getName().substring(1); // Remove the initial 'T' or 'A'
                            html.add(String.format("{ from: '%s', to: '%s' },", fromNodeName, toNodeName));
                        }
                    }
                } else {
                    html.add(line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading the template HTML file", e);
        }
        return html;
    }

}