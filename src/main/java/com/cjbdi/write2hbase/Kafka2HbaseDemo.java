package com.cjbdi.write2hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * @ Author: XYH
 * @ Date: 2021/11/27
 * @ Description:将kafka test中的数据消费到HBase的t_xyh中
 */
public class Kafka2HbaseDemo {
    public static void main(String[] args) {
        //配置Kafka连接信息
        Properties prop=new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"rookiex01:9092");
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        prop.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,30000);
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);//不允许自动提交
        prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG,"eventAttend");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        //创建kafka消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
        
        //设置消费的Topic
        consumer.subscribe(Collections.singleton("test"));//设置读取的topic

        //配置HBase连接信息
        Configuration conf = HBaseConfiguration.create();
//        conf.set("hbase.rootdir","hdfs://bd-01:9000/hbase");
        conf.set("hbase.zookeeper.quorum","rookiex01");
        conf.set("hbase.zookeeper.property.clientPort","2181");
//        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        conf.set("hbase.client.keyvalue.maxsize","102400000");

        try {
            //创建连接HBase实例
            Connection connection = ConnectionFactory.createConnection(conf);
            //将读取的数据设置放入指定表中
            Table eventAttendTable = connection.getTable(TableName.valueOf("t_xyh"));
            //开始读取Topic中的数据
            while (true) {
            
                //每100毫秒拉取一次数据
                ConsumerRecords<String, String> poll = consumer.poll(Duration.ofMillis(100));
                
                //创建List，保存Put类型的数据，需要方在while里定义，否则集合会无限增大，最终会导致OOM
                List<Put> datas=new ArrayList<>();
                
                //将拉取的数据转换成Put类型并add到集合中
                for (ConsumerRecord<String, String> record : poll) {
                
                    //输出拉取的数据，防止在拉取数据的时候失败
                    System.out.println(record.value());
                    
                    //对数据按","号分割
                    String[] split = record.value().split(",");
                    
                    //将拆分的数据放入Put对象中
//                    Put put = new Put(Bytes.toBytes((split[0]+split[1]+split[2]).hashCode()));

                    String rowKey = new Date().getTime() + "";
                    System.out.println("rowKey 为: " + rowKey);
                    //指定ROWKEY的值
                    Put put = new Put(Bytes.toBytes(rowKey));
                    //为Put对象中的数据指定列簇与列名
                    put.addColumn("f1".getBytes(),"eventid".getBytes(),split[0].getBytes());
                    put.addColumn("f1".getBytes(),"userid".getBytes(),split[1].getBytes());
                    put.addColumn("f1".getBytes(),"state".getBytes(),split[2].getBytes());

					//将处理后的Put对象添加到集合中
                    datas.add(put);
                }
                
                //将一次拉取的数据put到HBase中
                eventAttendTable.put(datas);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

