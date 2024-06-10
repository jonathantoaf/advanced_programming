package test;

import java.util.List;
import java.util.ArrayList;

public class Topic {
    public final String name;
    private List<Agent> subs;
    private List<Agent> pubs;

    Topic(String name) {
        this.name = name;
        this.subs = new ArrayList<Agent>();
        this.pubs = new ArrayList<Agent>();
    }

    public void subscribe(Agent a) {
        this.subs.add(a);
    }

    public void unsubscribe(Agent a) {
        this.subs.remove(a);
    }

    public void publish(Message m) {
        for (Agent ag : this.subs) {
            ag.callback(this.name, m);
        }
    }

    public void addPublisher(Agent a) {
        this.pubs.add(a);
    }

    public void removePublisher(Agent a) {
        this.pubs.remove(a);
    }

    public List<Agent> getSubs() {
        return this.subs;
    }

    public List<Agent> getPubs() {
        return this.pubs;
    }

}
