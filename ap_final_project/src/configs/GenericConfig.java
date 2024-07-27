package configs;


import graph.Agent;
import graph.ParallelAgent;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;
import java.io.IOException;
import java.lang.reflect.*;


public class GenericConfig implements Config {

    private String ConfFile;
    private List<ParallelAgent> parallelAgents;


    public GenericConfig() {
        parallelAgents = new ArrayList<>();
    }

    public void setConfFile(String ConfFile) {
        this.ConfFile = ConfFile;
    }

    @Override
    public void create() {
        List<String> lines = readConfFileLines(ConfFile);
        if (lines == null || lines.size() % 3 != 0) {
            throw new IllegalArgumentException("Invalid configuration file");
        }
        for (int i = 0; i < lines.size(); i += 3) {
            String[] subs = lines.get(i + 1).split(",");
            String[] pubs = lines.get(i + 2).split(",");
            try {
                Constructor<?> constructor = Class.forName(lines.get(i)).getDeclaredConstructor(String[].class, String[].class);
                Agent agent = (Agent) constructor.newInstance(subs, pubs);
                ParallelAgent parallelAgent = new ParallelAgent(agent, 10);
                parallelAgents.add(parallelAgent);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public String getName() {
        return "GenericConfig";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void close() {
        for (ParallelAgent parallelAgent : parallelAgents) {
            parallelAgent.close();
        }
    }

    public static List<String> readConfFileLines(String ConfFile) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(ConfFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
