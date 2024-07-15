package test;

import java.util.List;

public class PlusAgent implements Agent {

    private String name;
    private String[] subs;
    private String[] pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Double x;
    private Double y;

    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.subscribe();
        this.register();
        this.reset();
        this.name = "PlusAgent";
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
        this.x = 0.0;
        this.y = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        if (topic.equals(this.subs[0])) {
            this.x = msg.asDouble;
        } else if (topic.equals(this.subs[1])) {
            this.y = msg.asDouble;
        }
        if (this.x != 0.0 && this.y != 0.0) {
            this.topicManager.getTopic(this.pubs[0]).publish(new Message(this.x + this.y));
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
