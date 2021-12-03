//import com.alibaba.fastjson.JSON;
//import com.cjbdi.bean.WsBeanFromKafka;
//import org.apache.commons.io.FileUtils;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
///**
// * @Author: XYH
// * @Date: 2021/12/1 10:54 下午
// * @Description: TODO
// */
//public class Test {
//    public static void main(String[] args) throws IOException {
//        WsBeanFromKafka wsBeanFromKafka = new WsBeanFromKafka("15", "192.8.21.224", "fb_00", "db_msys", "（2006）浦民二（商）初字第1000号", "12345", "XX法院", "/aaa/vvv/ccc/ddd/qer.html", "255", "2020-01-01 00:00:00", "xxx");
//
//        byte[] bytes = FileUtils.readFileToByteArray(new File("/Users/ikazuchi-akira/IdeaProjects/test/java/src/test/ws/涉诉信息服务系统_模块设计_融合库文书ETL模块_20210927.docx"));
//
//        String s = Base64.getEncoder().encodeToString(bytes);
//
//        WsBeanDownloaded wsBeanDownloaded = new WsBeanDownloaded(wsBeanFromKafka, s);
//
//        String s1 = JSON.toJSONString(wsBeanDownloaded);
//
//        FileUtils.writeStringToFile(new File("/Users/ikazuchi-akira/IdeaProjects/test/java/src/test/ws/output.json"), s1, StandardCharsets.UTF_8);
//
//        String s2 = FileUtils.readFileToString(new File("/Users/ikazuchi-akira/IdeaProjects/test/java/src/test/ws/output.json"), StandardCharsets.UTF_8);
//
//        WsBeanDownloaded wsBeanDownloaded1 = JSON.parseObject(s2, WsBeanDownloaded.class);
//
//        byte[] decode = Base64.getDecoder().decode(wsBeanDownloaded1.getBase64File());
//
//        FileUtils.writeByteArrayToFile(new File("/Users/ikazuchi-akira/IdeaProjects/test/java/src/test/ws/output.docx"), decode);
//    }
//}
