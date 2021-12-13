package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.cjbdi.bean.WsBeanFromKafka;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Calendar;


/**
 * @Author: XYH
 * @Date: 2021/12/1 9:35 下午
 * @Description: TODO
 */
public class WsSourceJsonToWsBeanFunction extends ProcessFunction<String, WsBeanFromKafka> {
    private final OutputTag<String> outputTag;
    private boolean flag;
    private int startTime;
    private int stopTime;
    private ValueState<Calendar> calendarValueState;
    private ValueStateDescriptor<Calendar> valueStateDescriptor;

    public WsSourceJsonToWsBeanFunction(OutputTag<String> outputTag) {
        this.outputTag = outputTag;
    }

    @Override
    public void open(Configuration parameters) {
        ParameterTool globalJobParameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        flag = globalJobParameters.getBoolean("execution-time-flag", false);
        startTime = globalJobParameters.getInt("execution-time-start", 0);
        stopTime = globalJobParameters.getInt("execution-time-stop", 24);

//        valueStateDescriptor = new ValueStateDescriptor<>("calendar", Calendar.class);
//        StateTtlConfig.newBuilder(1000L * 60)
//                .setUpdateType
//        calendarValueState = getRuntimeContext().getState(valueStateDescriptor)

    }

    /**
     * @param s
     * @param context
     * @param collector
     * @return void
     * @Description 将从 kafkaSource 获取的 json 字符串解析成 bean, 解析失败的使用侧流输出, 打上指定标签
     */
    @Override
    public void processElement(String s, Context context, Collector<WsBeanFromKafka> collector) {

        try {
            WsBeanFromKafka wsBeanFromKafka = JSON.parseObject(s, WsBeanFromKafka.class);
            collector.collect(wsBeanFromKafka);
        } catch (Exception e) {
            e.printStackTrace();
            context.output(outputTag, s);
        }

    }
}
