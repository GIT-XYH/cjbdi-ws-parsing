package com.cjbdi

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

/**
 * 生产者轮询写入多个分区
 */
object ProducerDemo2 {

  def main(args: Array[String]): Unit = {

    // 1 配置参数
    val props = new Properties()
    // 连接kafka节点
    props.setProperty("bootstrap.servers", "node-1.51doit.cn:9092,node-2.51doit.cn:9092,node-3.51doit.cn:9092")
    //指定key序列化方式
    props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    //指定value序列化方式
    props.setProperty("value.serializer", classOf[StringSerializer].getName) // 两种写法都行

    val topic = "test" //三个分区{0, 1, 2}

    // 2 kafka的生产者
    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)
    for (i <- 51 to 2000) {
      // 3 封装的对象
      val record = new ProducerRecord[String, String](topic, "kafka," + i)
      producer.send(record) //将将数据发送到Kafka（是一条一条的发送吗？，还是先在客户端缓存，达到一定的大小在批量发送）
    }

    println("message send success")

    //producer.flush()
    // 释放资源
    producer.close()
  }

}
