package com.cjbdi.config;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.io.IOException;

import static com.cjbdi.config.KafkaConfig.*;

/**
 * @Author: XYH
 * @Date: 2021/12/3 1:12 下午
 * @Description: flink 运行环境和参数配置
 */
public class FlinkConfig {
    //运行环境设置
    public static final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
    public static KafkaSource<String> kafkaSource;
    public static KafkaSink<String> jsonErrorSink;
    public static KafkaSink<String> analysisErrorSink;
    public static KafkaSink<String> finalSink;

    /**
     * TODO: 2021/12/2 定义各类失败标签
     */
    //jsonToBean失败标签
    public static OutputTag<String> jsonErrorData = new OutputTag<String>("json-error-data") {};
    //主进程解析失败标签
    public static OutputTag<String> analysisError = new OutputTag<String>("jobs-error") {};
    //最终结果写入topic失败标签
    public static OutputTag<String> writeErrorData = new OutputTag<String>("toKafka-error") {};

    public static void flinkEnv(ParameterTool parameterTool) {
        //配置 flink 的运行环境
        //获取并传递全局参数
        env.getConfig().setGlobalJobParameters(parameterTool);
        //开启 checkpoint
        env.enableCheckpointing(1000L * 60, CheckpointingMode.EXACTLY_ONCE);
        env.setStateBackend(new HashMapStateBackend());
        //配置 checkPint 的参数
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointStorage(parameterTool.getRequired("checkpoint-dir"));
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        checkpointConfig.setCheckpointTimeout(Integer.MAX_VALUE);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, Time.seconds(10L)));

        /**
         * 设置 flink 的 source 和 sink
         */
        //kafkaSource 从 kafka 读取原始数据
        KafkaConfig.KafkaEnv(parameterTool);
        kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(KafkaConfig.brokers)
                .setTopics(inputTopic)
                .setGroupId(KafkaConfig.groupId)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
                .setProperty("commit.offsets.on.checkpoint", "true")
                .build();
        //kafkaSink 将 jsonToBean 失败的数据放到kafka 指定的 topic 中
        jsonErrorSink = KafkaSink.<String>builder()
                .setBootstrapServers(KafkaConfig.brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(jsonErrorTopic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                .build()
                )
                .build();
        analysisErrorSink = KafkaSink.<String>builder()
                .setBootstrapServers(KafkaConfig.brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(analysisErrorTopic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                .build()
                )
                .build();
        //kafkaSink 将最终处理好的数据输出到 kafka 指定的 topic 中
        finalSink = KafkaSink.<String>builder()
                .setBootstrapServers(KafkaConfig.brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(outputTopic)
                        .setValueSerializationSchema(new SimpleStringSchema()).build())
                .build();
    }
}
