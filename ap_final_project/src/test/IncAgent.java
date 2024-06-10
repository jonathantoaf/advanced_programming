package test;

import java.util.List;

public class IncAgent implements Agent {

    private final String name;
    private final String subs;
    private final String pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Double x;

    public IncAgent(List<String> subs, List<String> pubs) {
        this.subs = subs.getFirst();
        this.pubs = pubs.getFirst();
        this.subscribe();
        this.reset();
        this.name = "IncAgent";
    }

    private void subscribe() {
        this.topicManager.getTopic(this.subs).subscribe(this);
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
        this.x = msg.asDouble;
        this.topicManager.getTopic(this.pubs).publish(new Message(this.x + 1));
        this.reset();

    }

    @Override
    public void close() {
        this.topicManager.getTopic(this.subs).unsubscribe(this);
        this.topicManager.getTopic(this.pubs).removePublisher(this);

    }
}
