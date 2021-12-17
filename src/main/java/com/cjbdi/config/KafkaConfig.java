package com.cjbdi.config;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Properties;

/**
 * @Author: XYH
 * @Date: 2021/12/3 1:12 下午
 * @Description: 配置 kafka 相关参数信息
 */
public class KafkaConfig {

    public static String brokers;
    public static String brokers2;
    public static String groupId;
    public static String inputTopic;
    public static String outputTopic;
    public static String jsonErrorTopic;
    public static String analysisErrorTopic;

    static Properties properties = null;
    public static void KafkaEnv(ParameterTool parameterTool) {
        //配置kafka参数设置
        properties = new Properties();
        properties.setProperty("max.request.size", "214748364");
        properties.setProperty("compression.type", "gzip");
        properties.setProperty("buffer.memory", "335544320");
        properties.setProperty("batch.size", "1638400");
        properties.setProperty("max.block.ms", "214748364");

        //指定 kafka topic 等相关参数
        brokers = parameterTool.getRequired("bootstrap-servers");
        brokers2 = parameterTool.getRequired("bootstrap-servers2");
        groupId = parameterTool.getRequired("input-group-id");
        inputTopic = parameterTool.getRequired("input-topic");
        jsonErrorTopic = parameterTool.getRequired("json-error-topic");
        analysisErrorTopic = parameterTool.getRequired("analysis-error-topic");
        outputTopic = parameterTool.getRequired("output-topic");
    }
}
