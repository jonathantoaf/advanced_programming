package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
    private BlockingQueue<Message> queue;
    private Agent agent;
    private Thread readThread;
    private static final String divider = "Divider:";


    ParallelAgent(Agent agent, int capacity) {
        this.queue = new ArrayBlockingQueue<Message>(capacity);
        this.agent = agent;
        this.readThread = new Thread(this::readThreadFunction);
        this.readThread.start();
    }

    public String getName() {
        return this.agent.getName();
    }

    public void reset() {
        this.agent.reset();
    }

    public void callback(String topic, Message msg) {
        try {
            Message topicMessageMerge = new Message(String.format("%s%s%s", topic, divider, msg.asText));
            System.out.println("ParallelAgent callback: " + topicMessageMerge.asText);
            this.queue.put(topicMessageMerge);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.readThread.interrupt();
        this.agent.close();
        System.out.println("ParallelAgent close");
    }

    private String getTopic(Message msg) {
        System.out.println("ParallelAgent getTopic: " + msg.asText.split(divider, 2)[0]);
        return msg.asText.split(divider, 2)[0];
    }

    private Message getMessage(Message msg) {
//        get all the text after the first colon
        System.out.println("ParallelAgent getMessage: " + msg.asText.split(divider, 2)[1]);
        return new Message(msg.asText.split(divider, 2)[1]);
    }

    private void readThreadFunction() {
        while (true) {
            try {
                Message msg = this.queue.take();
                this.agent.callback(getTopic(msg), getMessage(msg));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}