package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjbdi.bean.WsBean;
import com.cjbdi.bean.WsBeanFromKafka;
import com.cjbdi.doc.detector.DocDetector;
import com.cjbdi.doc.detector.DocType;
import com.cjbdi.fp.docs.Doc2Text;
import com.cjbdi.fp.text.html.HtmlParser;
import com.cjbdi.utils.CharsetUtil;
import com.sun.tools.internal.ws.wsdl.document.WSDLDocument;
import org.apache.commons.io.FileUtils;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

/**
 * @Author: XYH
 * @Date: 2021/12/1 9:35 下午
 * @Description: TODO
 */
public class AnalysisProcessFunction extends ProcessFunction<WsBeanFromKafka, String> {

    private OutputTag<String> writeErrorData;
    public AnalysisProcessFunction(OutputTag<String> outputTag) {
        this.writeErrorData = outputTag;
    }

    public static String sendPost(String url, String param, Map<String, String> header) throws UnsupportedEncodingException, IOException{
        PrintWriter out;
        out = null;
        BufferedReader in = null;
        String result = "";
        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection conn = realUrl.openConnection();
        //设置超时时间
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        // 设置通用的请求属性
        if (header!=null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 获取URLConnection对象对应的输出流
        out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        out.print(param);
        // flush输出流的缓冲
        out.flush();
        // 定义BufferedReader输入流来读取URL的响应
        in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf8"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if(out!=null){
            out.close();
        }
        if(in!=null){
            in.close();
        }
        return result;
    }
    @Override
    public void processElement(WsBeanFromKafka wsBeanFromKafka, Context context, Collector<String> collector) throws Exception {

        byte[] wsDoc = null;
        String c_wsText = "";
        /**
         * 先将 base64格式的文本转成二进制的文本
         */
        try {
            WsBean wsBean = wsBeanFromKafka.getWsBean();
            String base64File = wsBeanFromKafka.getBase64File();
            //wsDoc 就是二进制格式的文书原文件
            wsDoc = Base64.getDecoder().decode(base64File);
//            FileUtils.writeByteArrayToFile(new File("/Users/xuyuanhang/Desktop/code/cjbdi-ws-parsing/data/wsDoc"), wsDoc);

        } catch (Exception e) {
            e.printStackTrace();
            context.output(writeErrorData, JSON.toJSONString(wsBeanFromKafka));
        }
        //将二进制文件转为 text
        DocType type = DocDetector.detect(wsDoc);
        if (type == null) {
            if (CharsetUtil.isUTF8(wsDoc)) {
                c_wsText = new String(wsDoc, "UTF8");
            } else {
                c_wsText = new String(wsDoc, "GBK");
            }
        } else {
            try {
                Doc2Text dt = new Doc2Text();
                if (!dt.handleData(wsDoc)) {

                }
                c_wsText = dt.getPlainText();
            }catch (Exception e) {
            }
        }
        if (c_wsText == null || c_wsText.isEmpty()) {
            c_wsText = "";
        }
//        System.out.println(c_wsText);
        String text = HtmlParser.getPlainText(c_wsText);
//        System.out.println(text);
        //将 text 转为 json 类型
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fullText", text);
        String fullText = JSONObject.toJSONString(jsonObject);
        System.out.println(fullText);
        // TODO: 2021/12/6 格式规范, 按照解析格式
//        String post = sendPost("http://192.1.188.222:9001/getDoc", fullText, null);
        collector.collect(fullText);
    }
}
