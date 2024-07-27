package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

public class LogAgent implements Agent {

    private String name;
    private String[] subs;
    private String[] pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Double x;

    public LogAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.subscribe();
        this.register();
        this.reset();
        this.name = "LogAgent";
    }

    private void subscribe() {
        this.topicManager.getTopic(this.subs[0]).subscribe(this);
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
        this.x = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        this.x = msg.asDouble;
        if (this.x > 0) { // Logarithm is only defined for positive numbers
            this.topicManager.getTopic(this.pubs[0]).publish(new Message(Math.log10(this.x)));
        } else {
            this.topicManager.getTopic(this.pubs[0]).publish(new Message(Double.NaN));
        }
        this.reset();
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