package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.cjbdi.bean.WsBean;
import com.cjbdi.bean.WsBeanFromKafka;
import com.sun.tools.internal.ws.wsdl.document.WSDLDocument;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.security.PublicKey;
import java.util.Base64;

/**
 * @Author: XYH
 * @Date: 2021/12/1 9:35 下午
 * @Description: TODO
 */
public class AnalysisProcessFunction extends ProcessFunction<WsBeanFromKafka, Object> {

    private OutputTag<String> writeErrorData;
    public AnalysisProcessFunction(OutputTag<String> outputTag) {
        this.writeErrorData = outputTag;
    }
    @Override
    public void processElement(WsBeanFromKafka wsBeanFromKafka, Context context, Collector<Object> collector) throws Exception {

        /**
         * 先将 base64格式的文本转成二进制的文本
         */
        try {
            WsBean wsBean = wsBeanFromKafka.getWsBean();
            String base64File = wsBeanFromKafka.getBase64File();
            //wsDoc 就是二进制格式的文书原文件
            byte[] wsDoc = Base64.getDecoder().decode(base64File);
        } catch (Exception e) {
            e.printStackTrace();
            context.output(writeErrorData, JSON.toJSONString(wsBeanFromKafka));
        }

    }
}
