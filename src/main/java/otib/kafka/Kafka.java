package otib.kafka;

import burp.BurpExtender;
import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.clients.consumer.RangeAssignor;

import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.utils.Utils;

import burp.BurpExtender.LogEntry;


public class Kafka implements Runnable {
    public IBurpExtenderCallbacks Callbacks;
    public IExtensionHelpers helpers;
    public BurpExtender burpExtender;

    public List<BurpExtender.LogEntry> log;

    private KafkaProducer<byte[], byte[]> producer;

    @Override
    public void run() {
        Thread.currentThread().setContextClassLoader(Utils.class.getClassLoader());

        Properties props = new Properties();
        props.put("bootstrap.servers","192.168.0.1:19092");
        props.put("key.deserializer",ByteArrayDeserializer.class);
        props.put("value.deserializer", ByteArrayDeserializer.class);
        props.put("group.id", "BurpKafkaExt" + UUID.randomUUID().toString()); // Рандомная группа, чтобы не сдвигать offset'ы легальных пользователей
        props.put("max.partition.fetch.bytes", 10 * 1024 * 1024);
        props.put("key.serializer", ByteArraySerializer.class);
        props.put("value.serializer", ByteArraySerializer.class);

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props);
        producer = new KafkaProducer<>(props);
        Map<String, List<PartitionInfo>> topics = consumer.listTopics();

        for (Map.Entry<String, List<PartitionInfo>> entry:
                topics.entrySet()) {
            Callbacks.printOutput(entry.getKey());
        }

        consumer.subscribe(Arrays.asList("TestTopic1",
                "TestTopic2"));

        while(true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (ConsumerRecord<byte[],byte[]> record : records) {
                List<String> headers = new ArrayList<String>();
                headers.add("GET "+ record.topic() +" HTTP/1.1");
                headers.add("Host: 127.0.0.1");
                headers.add(BurpExtender.KAFKA_HEADER);

                String body = Base64.getEncoder().encodeToString(record.value());
                byte[] request = helpers.buildHttpMessage(headers, helpers.stringToBytes(body));
                // create a new log entry with the message details
                synchronized (log) {
                    int row = log.size();
                    Callbacks.printOutput("Topic " + String.valueOf(row));
                    log.add(new LogEntry(record.topic(), request, body));
                    burpExtender.fireTableRowsInserted(row,row);
                }
            }
        }
    }

    public boolean sendMessage(String topic, byte[] message) {
        producer.send(new ProducerRecord<>(topic, message));
        return true;
    }
}
