package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


public class TopicManagerSingleton {


    public static class TopicManager {
        private static final TopicManager instance = new TopicManager();
        private ConcurrentHashMap<String, Topic> topics;

        private TopicManager() {
            this.topics = new ConcurrentHashMap<String, Topic>();
        }

        public Topic getTopic(String name) {
            if (this.topics.containsKey(name)) {
                return this.topics.get(name);
            } else {
                Topic t = new Topic(name);
                this.topics.put(name, t);
                return t;
            }
        }

        public boolean containsTopic(String name) {
            return this.topics.containsKey(name);
        }

        public Collection<Topic> getTopics() {
            return this.topics.values();
        }

        public void clear() {
            this.topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }

}
