package configs;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;

import java.util.ArrayList;

public class Graph extends ArrayList<Node> {
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkIfNodeExists(String nodeName) {
        for (Node node : this)
            if (node.getName().equals(nodeName))
                return true;
        return false;
    }

    private Integer getNodeIndex(String nodeName) {
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).getName().equals(nodeName))
                return i;
        return -1;
    }

    public void printGraphNodes() {
        for (Node node : this) {
            System.out.print(node.getName() + "  ");
        }
        System.out.println();
    }

    private void reset() {
        this.clear();
    }

    public void printGraphWithNodesAndEdges() {
        for (Node node : this) {
            if (!node.getEdges().isEmpty()) {
                System.out.print(node.getName() + " -> ");
                for (Node edge : node.getEdges()) {
                    System.out.print(edge.getName() + " ");
                }
                System.out.println();
            }
        }
    }

    public void createFromTopics() {
        this.reset();
        for (Topic topic : topicManager.getTopics()) {
            String topicNodeName = String.format("T%s", topic.name);
            if (!checkIfNodeExists(topicNodeName))
                this.add(new Node(topicNodeName));
            for (Agent subscriber : topic.getSubs()) {
                String agentNodeName = String.format("A%s", subscriber.getName());
                if (!checkIfNodeExists(agentNodeName))
                    this.add(new Node(String.format(agentNodeName)));
                this.get(getNodeIndex(topicNodeName)).addEdge(this.get(getNodeIndex(agentNodeName)));
            }
            for (Agent publisher : topic.getPubs()) {
                String agentNodeName = String.format("A%s", publisher.getName());
                if (!checkIfNodeExists(agentNodeName))
                    this.add(new Node(String.format(agentNodeName)));
                this.get(getNodeIndex(agentNodeName)).addEdge(this.get(getNodeIndex(topicNodeName)));
            }
        }
    }


}
