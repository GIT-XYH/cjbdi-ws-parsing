package com.cjbdi

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

/**
 * 不指定分区，但是指定key
 */
object ProducerDemo4 {

  def main(args: Array[String]): Unit = {

    // 1 配置参数
    val props = new Properties()
    // 连接kafka节点
    props.setProperty("bootstrap.servers", "node-1.51doit.cn:9092,node-2.51doit.cn:9092,node-3.51doit.cn:9092")
    //指定key序列化方式
    props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    //指定value序列化方式
    props.setProperty("value.serializer", classOf[StringSerializer].getName) // 两种写法都行

    val topic = "test" //6个分区{0, 1, 2, 3, 4, 5}

    // 2 kafka的生产者
    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)
    for (i <- 3201 to 3250) {
      // 3 封装的对象
      //指定分包编号，写入到指定的分区中
      val record = new ProducerRecord[String, String](topic, "hbase66666", "kafka," + i)
      producer.send(record)
    }

    println("message send success")

    //producer.flush()
    // 释放资源
    producer.close()
  }

}
