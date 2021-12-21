package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.cjbdi.wscommon.bean.WsBeanWithFile;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author: XYH
 * @Date: 2021/12/1 9:35 下午
 * @Description: TODO
 */
public class WsBeanParsingToJsonProcessFunction extends ProcessFunction<WsBeanWithFile, String> {
    @Override
    public void processElement(WsBeanWithFile wsBeanParsing, Context context, Collector<String> collector) throws Exception {
        try {
            String s = JSON.toJSONString(wsBeanParsing);
            collector.collect(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
