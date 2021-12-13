package com.cjbdi.jobs;

import com.cjbdi.config.FlinkConfig;
import com.cjbdi.config.KafkaConfig;
import com.cjbdi.udfs.WsSourceJsonToWsBeanFunction;
import com.cjbdi.udfs.AnalysisProcessFunction;
import com.cjbdi.wscommon.bean.WsBeanWithFile;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import static com.cjbdi.config.FlinkConfig.*;

/**
 * @Author: XYH
 * @Date: 2021/12/1 2:35 下午
 * @Description: 使用 flink 从 kafka 消费数据, 解析, 然后再写回 kafka
 */
public class WsParsing {
    public static void main(String[] args) throws Exception {

        //获取全局参数
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(args[0]);
        //flink 相关配置
        FlinkConfig.flinkEnv(parameterTool);
        //整个 job 的并行度
        int parallelism = env.getParallelism();

        System.out.println("当前 job 的执行环境并行度为: " + parallelism);
        //kafka 相关配置
        KafkaConfig.KafkaEnv(parameterTool);
        //从指定的kafkaSouse中读取数据
        DataStreamSource<String> kafkaStream = FlinkConfig.env.fromSource(FlinkConfig.kafkaSource, WatermarkStrategy.noWatermarks(), "ws-source");
        //将kafka中的json转成bean
        SingleOutputStreamOperator<WsBeanWithFile> wsBeanStream = kafkaStream.process(new WsSourceJsonToWsBeanFunction(jsonErrorData));
        //获取json解析失败的数据
        DataStream<String> jsonErrorStream = wsBeanStream.getSideOutput(jsonErrorData);
        //将 json 解析失败的数据放到 kafka 指定的 topic 中
        jsonErrorStream.sinkTo(jsonErrorSink);
//         TODO: 2021/12/4 解析文书, 标签参数待定
        SingleOutputStreamOperator<String> analysisStream = wsBeanStream.process(new AnalysisProcessFunction(writeErrorData));
        DataStream<String> analysisStreamSideOutput = analysisStream.getSideOutput(writeErrorData);
        analysisStreamSideOutput.sinkTo(analysisErrorSink);
//         TODO: 2021/12/4 将解析好的 json 写入 kafka
        analysisStream.sinkTo(finalSink);
        FlinkConfig.env.execute();
    }
}
