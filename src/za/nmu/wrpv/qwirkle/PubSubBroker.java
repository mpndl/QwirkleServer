package za.nmu.wrpv.qwirkle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PubSubBroker {
    // Collection of subscribers. Maps topic onto set of subscribers.
    private static final Map<String, Set<Subscriber>> subscribers = new ConcurrentHashMap<>();

    private final static Lock lock = new ReentrantLock();

    public static void subscribe(String topic, Subscriber subscriber) {
        // Get set of subscribers listening to topic. If none, create a new set.
        Set<Subscriber> subscriberSet;
        subscriberSet = subscribers.computeIfAbsent(topic, key -> new HashSet<>());

        // Add subscriber to the set.
        subscriberSet.add(subscriber);
    }

    public static void unsubscribe(String topic, Subscriber subscriber) {
        // Get set of subscribers listening to the topic.
        Set<Subscriber> subscriberSet;

        lock.lock();
        subscriberSet = subscribers.get(topic);

        // If no-one listening, stop.
        if(subscriberSet == null) {
            lock.unlock();
            return;
        }

        // Remove from set.
        subscriberSet.remove(subscriber);

        // Empty set? If so, remove the set.
        if(subscriberSet.size() == 0)
            subscribers.remove(topic);
        lock.unlock();
    }

    public static void unsubscribe(Subscriber subscriber) {
        // Getting topics, but copying to another structure since the
        // process of unsubscribing could remove a subscriber set, hence
        // modify the keySet while iterating through it - i.e. a problem.
        List<String> topics = new ArrayList<>();
        lock.lock();
        topics.addAll(subscribers.keySet());

        for (String topic : topics) {
            unsubscribe(topic, subscriber);
        }
        lock.unlock();
    }

    public static void publish(Object publisher, String topic, Map<String, Object> params) {
        Set<Subscriber> subscriberSet;

        lock.lock();
        subscriberSet = subscribers.get(topic);

        // If no subscribers for the topic, done!
        if(subscriberSet == null) {
            lock.unlock();
            return;
        }

        // Notify all subscribers of the publishing of the message.
        subscriberSet.forEach(
                subscriber -> subscriber.onPublished(publisher, topic, params));
        lock.unlock();
    }
}