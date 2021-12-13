package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjbdi.doc.detector.DocDetector;
import com.cjbdi.doc.detector.DocType;
import com.cjbdi.fp.docs.Doc2Text;
import com.cjbdi.fp.text.html.HtmlParser;
import com.cjbdi.utils.CharsetUtil;
import com.cjbdi.wscommon.bean.WsBean;
import com.cjbdi.wscommon.bean.WsBeanWithFile;
import com.cjbdi.wscommon.bean.es.ESDataBean;
import com.cjbdi.wscommon.bean.es.KafkaTopic2Bean;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * @Author: XYH
 * @Date: 2021/12/1 9:35 下午
 * @Description: TODO
 */
public class AnalysisProcessFunction extends ProcessFunction<WsBeanWithFile, String> {

    private OutputTag<String> parsingErrorData;
    public AnalysisProcessFunction(OutputTag<String> outputTag) {
        this.parsingErrorData = outputTag;
    }

    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
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
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void processElement(WsBeanWithFile wsBeanFromKafka, Context context, Collector<String> collector) throws Exception {

        byte[] wsDoc = null;
        String c_wsText = "";
        WsBean wsBean = null;
        /**
         * 先将 base64格式的文本转成二进制的文本
         */
        try {
            wsBean = wsBeanFromKafka.getWsBean();
            String base64File = wsBeanFromKafka.getBase64File();
            //wsDoc 就是二进制格式的文书原文件
            wsDoc = Base64.getDecoder().decode(base64File);
//            FileUtils.writeByteArrayToFile(new File("/Users/xuyuanhang/Desktop/code/cjbdi-ws-parsing/data/wsDoc"), wsDoc);

        } catch (Exception e) {
            e.printStackTrace();
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
//        System.out.println(fullText);
        // TODO: 2021/12/6 格式规范, 按照解析格式
//        String post = sendPost("http://192.1.188.222:9001/getDoc", fullText);
//        collector.collect(fullText);

        try {
//            String post = sendPost("http://192.1.188.222:9001/getDoc", fullText);
            String post = "{\"code\":200,\"msg\":null,\"data\":{\"syncss\":0,\"docid\":null,\"uniqid\":\"b0ecab72-9025-4c7d-bd31-1aed335b7d75\",\"content\":\"遵义市中级人民法院\\nzdqz\\n传 票\\n案 号\\n(2018)黔03民终5625号\\n案 由\\n财产损害赔偿纠纷\\n被传唤人\\n贵州建工集团第五建筑工程有限责任公司\\n单位或住址\\n传唤事由\\n开庭\\n应到时间\\n2018年10月17日\\n上午9时0分至10时30分\\n应到处所\\n注意事项\\n被传唤人必须准时达到应到处所。\\n本传票由被传唤人携带来院报到。\\n被传唤人收到传票后，应在送达回\\n证上签名或盖章。\\n审判员\\n二Ｏ一八年十月十一日\\n\",\"path\":\"20150000.zip\\\\/ffff6d12a48d49908ac1a84d00b74cf5.txt\",\"title\":\"遵义市中级人民法院\",\"doctype\":null,\"referencetype\":\"普通案例\",\"referencetypeorder\":null,\"province\":\"贵州省\",\"courtname\":\"遵义市中级人民法院\",\"courtlevel\":\"中级法院\",\"courtid\":\"2800\",\"courtpid\":\"2783\",\"parentcourtname\":\"贵州省高级人民法院\",\"casetype\":\"民事\",\"procedure\":\"二审\",\"casecause\":\"财产损害赔偿纠纷\",\"casecausefull\":[\"民事\",\"物权纠纷\",\"物权保护纠纷\",\"财产损害赔偿纠纷\"],\"multicasecause\":[\"财产损害赔偿纠纷\"],\"scoremap\":{\"民事/物权纠纷/物权保护纠纷/财产损害赔偿纠纷\":2.0},\"caseid\":\"（2018）黔03民终5625号\",\"relatedcaseid\":[],\"precaseid\":null,\"acceptdate\":\"\",\"judgedate\":null,\"publishdate\":\"\",\"applicablelaws\":[],\"relatedlaws\":[],\"litigantslist\":[],\"judgememberslist\":[],\"paragraphs\":[{\"tag\":\"审理法院\",\"content\":\"遵义市中级人民法院\"},{\"tag\":\"标题\",\"content\":\"zdqz\"},{\"tag\":\"标题\",\"content\":\"传票\"},{\"tag\":\"标题\",\"content\":\"案号\"},{\"tag\":\"案号\",\"content\":\"(2018)黔03民终5625号\"},{\"tag\":\"标题\",\"content\":\"案由\"},{\"tag\":\"标题\",\"content\":\"财产损害赔偿纠纷\"},{\"tag\":\"标题\",\"content\":\"被传唤人\"},{\"tag\":\"标题\",\"content\":\"贵州建工集团第五建筑工程有限责任公司\"},{\"tag\":\"标题\",\"content\":\"单位或住址\"},{\"tag\":\"标题\",\"content\":\"传唤事由\"},{\"tag\":\"标题\",\"content\":\"开庭\"},{\"tag\":\"标题\",\"content\":\"应到时间\"},{\"tag\":\"标题\",\"content\":\"2018年10月17日\"},{\"tag\":\"标题\",\"content\":\"上午9时0分至10时30分\"},{\"tag\":\"标题\",\"content\":\"应到处所\"},{\"tag\":\"标题\",\"content\":\"注意事项\"},{\"tag\":\"标题\",\"content\":\"被传唤人必须准时达到应到处所。\"},{\"tag\":\"标题\",\"content\":\"本传票由被传唤人携带来院报到。\"},{\"tag\":\"标题\",\"content\":\"被传唤人收到传票后,应在送达回\"},{\"tag\":\"标题\",\"content\":\"证上签名或盖章。\"},{\"tag\":\"标题\",\"content\":\"审判员\"},{\"tag\":\"标题\",\"content\":\"二O一八年十月十一日\"}],\"features\":[],\"disputefocus\":[],\"focuscontent\":[],\"proof\":[],\"general\":[],\"judgeresult\":[],\"conclusions\":null,\"procuratoratecaseid\":[],\"extra\":null,\"amount\":0.0,\"question\":null,\"qcasecause\":null,\"kwords\":null,\"segtype\":null,\"zhichanFrq\":null,\"flag\":0,\"jafs\":null},\"traceId\":null}";
            ESDataBean esDataBean = JSON.parseObject(post, ESDataBean.class);
            KafkaTopic2Bean kafkaTopic2Bean = new KafkaTopic2Bean(wsBean, esDataBean);
            String jsonString = jsonObject.toJSONString(kafkaTopic2Bean);
            System.out.println(jsonString);
            collector.collect(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            context.output(parsingErrorData, JSON.toJSONString(c_wsText));
        }

    }
}
