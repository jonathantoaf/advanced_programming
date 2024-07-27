package graph;

import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {

    private final String name;
    private final String firstTopic;
    private final String secondTopic;
    private final String resultTopic;
    private final BinaryOperator<Double> operator;
    private Double firstMessage;
    private Double secondMessage;
    private final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

    public BinOpAgent(String name, String firstTopic, String secondTopic, String resultTopic, BinaryOperator<Double> operator) {
        this.name = name;
        this.firstTopic = firstTopic;
        this.secondTopic = secondTopic;
        this.resultTopic = resultTopic;
        this.operator = operator;
        this.reset();
        this.topicManager.getTopic(firstTopic).subscribe(this);
        this.topicManager.getTopic(secondTopic).subscribe(this);
        this.topicManager.getTopic(resultTopic).addPublisher(this);

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.firstMessage = 0.0;
        this.secondMessage = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        if (topic.equals(this.firstTopic)) {
            this.firstMessage = msg.asDouble;
        } else if (topic.equals(this.secondTopic)) {
            this.secondMessage = msg.asDouble;
        }
        if (this.firstMessage != 0.0 && this.secondMessage != 0.0) {
            this.topicManager.getTopic(this.resultTopic).publish(new Message(this.operator.apply(this.firstMessage, this.secondMessage)));
            this.reset();
        }
    }

    @Override
    public void close() {
        this.topicManager.getTopic(this.firstTopic).unsubscribe(this);
        this.topicManager.getTopic(this.secondTopic).unsubscribe(this);
        this.topicManager.getTopic(this.resultTopic).removePublisher(this);
    }
}