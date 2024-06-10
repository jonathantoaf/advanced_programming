package test;

import java.util.List;

public class PlusAgent implements Agent {

    private String name;
    private List<String> subs;
    private List<String> pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Double x;
    private Double y;

    public PlusAgent(List<String> subs, List<String> pubs) {
        this.subs = subs.subList(0, 2);
        this.pubs = pubs.subList(0, 1);
        this.subscribe();
        this.reset();
        this.name = "PlusAgent";
    }

    private void subscribe() {
        for (String s : this.subs) {
            this.topicManager.getTopic(s).subscribe(this);
        }
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
        if (topic.equals(this.subs.get(0))) {
            this.x = msg.asDouble;
        } else if (topic.equals(this.subs.get(1))) {
            this.y = msg.asDouble;
        }
        if (this.x != 0.0 && this.y != 0.0) {
            this.topicManager.getTopic(this.pubs.get(0)).publish(new Message(this.x + this.y));
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
