package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

import java.util.HashMap;
import java.util.Map;

public class SumAgent implements Agent {

    private String name;
    private String[] subs;
    private String[] pubs;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
    private Map<String, Double> inputs;

    public SumAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.inputs = new HashMap<>();
        this.subscribe();
        this.register();
        this.reset();
        this.name = "SumAgent";
    }

    private void subscribe() {
        for (String sub : this.subs) {
            this.topicManager.getTopic(sub).subscribe(this);
        }
    }

    private void register() {
        for (String pub : this.pubs) {
            this.topicManager.getTopic(pub).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        for (String sub : this.subs) {
            this.inputs.put(sub, 0.0);
        }
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        this.inputs.put(topic, msg.asDouble);

        if (this.allInputsNonZero()) {
            double sum = this.inputs.values().stream().mapToDouble(Double::doubleValue).sum();
            this.topicManager.getTopic(this.pubs[0]).publish(new Message(sum));
            this.reset();
        }
    }

    private boolean allInputsNonZero() {
        return this.inputs.values().stream().allMatch(value -> value != 0.0);
    }

    @Override
    public void close() {
        for (String sub : this.subs) {
            this.topicManager.getTopic(sub).unsubscribe(this);
        }
        for (String pub : this.pubs) {
            this.topicManager.getTopic(pub).removePublisher(this);
        }
    }
}