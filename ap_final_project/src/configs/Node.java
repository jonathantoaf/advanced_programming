package configs;

import graph.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<Node>();
        this.msg = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public void addEdge(Node n) {
        for (Node node : this.edges) {
            if (node.getName().equals(n.getName())) {
                return;
            }
        }
        this.edges.add(n);
    }

    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();
        Set<Node> recursionStack = new HashSet<>();
        return dfs(this, visited, recursionStack);
    }

    private boolean dfs(Node currentNode, Set<Node> visited, Set<Node> recursionStack) {
        visited.add(currentNode);
        recursionStack.add(currentNode);
        for (Node adjacentNode : currentNode.edges) {
            if (!visited.contains(adjacentNode)) {
                if (dfs(adjacentNode, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(adjacentNode)) {
                return true;
            }
        }
        recursionStack.remove(currentNode);
        return false;
    }

}