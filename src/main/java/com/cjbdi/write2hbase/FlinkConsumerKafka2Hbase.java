package com.cjbdi.write2hbase;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

/**
 * @Author: XYH
 * @Date: 2021/11/26 10:54 上午
 * @Description:
 */
public class FlinkConsumerKafka2Hbase {
    public static void main(String[] args) throws Exception {
        //flink执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000);

        Properties prop=new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"rookiex01:9092");
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        prop.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,30000);
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);//不允许自动提交
        prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG,"eventAttend");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        //配置数据源, 是一个 kafka 的消费者
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("test", new SimpleStringSchema(), prop);

        //从最开始消费
        consumer.setStartFromEarliest();
        DataStreamSource<String> stream = env.addSource(consumer);
        stream.print();
        env.execute();
    }
}
