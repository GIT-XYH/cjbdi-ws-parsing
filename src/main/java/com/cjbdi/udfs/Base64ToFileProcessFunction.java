package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.cjbdi.wscommon.bean.WsBean;
import com.cjbdi.wscommon.bean.WsBeanWithFile;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * @Author: XYH
 * @Date: 2021/12/3 2:35 下午
 * @Description: TODO
 */
public class Base64ToFileProcessFunction extends ProcessFunction<WsBeanWithFile, String> {
    private OutputTag<String> writeErrorData;
    private String fileDir;
    private SimpleDateFormat simpleDateFormat;

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool globalJobParameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        fileDir = globalJobParameters.get("file-dir");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public Base64ToFileProcessFunction(OutputTag<String> outputTag) {
        this.writeErrorData = outputTag;
    }


    @Override
    public void processElement(WsBeanWithFile wsBeanFromKafka, Context context, Collector<String> collector) throws Exception {

        try {
//            WsBeanDownloaded wsBeanDownloaded = JSON.parseObject(s, WsBeanDownloaded.class);
            WsBean wsBean = wsBeanFromKafka.getWsBean();
            String base64File = wsBeanFromKafka.getBase64File();
            //将 base64 格式的转成二进制格式的
            byte[] file = Base64.getDecoder().decode(base64File);
//            String fileName = wsBean.getFbId() + "&" +
//                    wsBean.getSchema() + "&" +
//                    wsBean.getT_c_stm() + "&" +
//                    wsBean.getT_c_baah() + "&" +
//                    wsBean.getWs_c_nr().replaceAll("/", "#");
//
//
//            String path = fileDir + wsBean.getHost() + "/" +
//                    wsBean.getDatabase() + "/" +
//                    wsBean.getSchema() + "/" +
//                    simpleDateFormat.format(new Date()) + "/";
//
//            File parent = new File(path);
//            if (!parent.exists()) {
//                parent.mkdirs();
//            }
//
//            File target = new File(parent, fileName);
//            if (target.exists()) {
//                target.delete();
//            }
            //将文件写出测试是否正确
//            FileUtils.writeByteArrayToFile(new File("/Users/xuyuanhang/Desktop/code/demo/cjbdi-ws-parsingDemo/data/wsDoc.doc"), file);
        } catch (Exception e) {
            e.printStackTrace();
            context.output(writeErrorData, JSON.toJSONString(wsBeanFromKafka));
        }
    }
}
