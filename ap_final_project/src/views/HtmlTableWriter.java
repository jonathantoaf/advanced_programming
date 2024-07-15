package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HtmlTableWriter {

    public static ArrayList<String> getTableHtml(ArrayList<String> topicNames) throws IOException {
        // Read the template HTML file
        String htmlTemplate = new String(Files.readAllBytes(Paths.get("html_files/graph.html")));

        // Generate the table rows
        StringBuilder tableRows = new StringBuilder();
        if (topicNames.isEmpty()) {
            tableRows.append("<tr><td colspan=\"2\" class=\"no-data\">No data available</td></tr>");
        } else {
            for (String topic : topicNames) {
                tableRows.append("<tr>")
                        .append("<td>").append(topic).append("</td>")
                        .append("<td>0</td>")
                        .append("</tr>");
            }
        }

        // Replace the placeholder with the actual table rows
        String htmlContent = htmlTemplate.replace("{{TABLE_ROWS}}", tableRows.toString());

        // Convert the htmlContent to an ArrayList of Strings
        ArrayList<String> htmlLines = new ArrayList<>();
        for (String line : htmlContent.split("\n")) {
            htmlLines.add(line);
        }

        return htmlLines;
    }
}
