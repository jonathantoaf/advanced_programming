package test;

import java.util.ArrayList;

public class Topic {
    public final String name;
    private ArrayList<Agent> subs;
    private ArrayList<Agent> pubs;

    Topic(String name) {
        this.name = name;
        this.subs = new ArrayList<Agent>();
        this.pubs = new ArrayList<Agent>();
    }

    public void subscribe(Agent a) {
        for (Agent ag : this.subs) {
            if (ag.getName().equals(a.getName())) {
                System.out.printf("Agent %s already subscribed to topic %s\n", a.getName(), this.name);
                return;
            }
        }
        this.subs.add(a);
    }

    public void unsubscribe(Agent a) {
        for (Agent ag : this.subs) {
            if (ag.getName().equals(a.getName())) {
                this.subs.remove(ag);
                return;
            }
        }
        System.out.printf("Agent %s is not subscribed to topic %s\n", a.getName(), this.name);
    }

    public void publish(Message m){
        for (Agent ag : this.subs) {
            ag.callback(this.name, m);
        }
    }

    public void addPublisher(Agent a){
        for (Agent ag : this.pubs) {
            if (ag.getName().equals(a.getName())) {
                System.out.printf("Agent %s already published to topic %s\n", a.getName(), this.name);
                return;
            }
        }
        this.pubs.add(a);
    }

    public void removePublisher(Agent a){
        for (Agent ag : this.pubs) {
            if (ag.getName().equals(a.getName())) {
                this.pubs.remove(ag);
                return;
            }
        }
        System.out.printf("Agent %s is not published to topic %s\n", a.getName(), this.name);
    }

    }
