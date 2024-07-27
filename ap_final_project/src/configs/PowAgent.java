package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

public class PowAgent implements Agent {

    private String name;
    private String[] subs;
    private String[] pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Double base;
    private Double exponent;

    public PowAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.subscribe();
        this.register();
        this.reset();
        this.name = "PowAgent";
    }

    private void subscribe() {
        this.topicManager.getTopic(this.subs[0]).subscribe(this);
        this.topicManager.getTopic(this.subs[1]).subscribe(this);
    }

    private void register() {
        this.topicManager.getTopic(this.pubs[0]).addPublisher(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.base = null;
        this.exponent = null;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        if (topic.equals(this.subs[0])) {
            this.base = msg.asDouble;
        } else if (topic.equals(this.subs[1])) {
            this.exponent = msg.asDouble;
        }

        if (this.base != null && this.exponent != null) {
            this.topicManager.getTopic(this.pubs[0]).publish(new Message(Math.pow(this.base, this.exponent)));
            this.reset();
        }
    }

    @Override
    public void close() {
        for (String s : this.subs) {
            this.topicManager.getTopic(s).unsubscribe(this);
        }
        for (String s : this.pubs) {
            this.topicManager.getTopic(s).removePublisher(this);
        }
    }
}