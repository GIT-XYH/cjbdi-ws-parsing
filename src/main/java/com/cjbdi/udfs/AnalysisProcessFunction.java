package com.cjbdi.udfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjbdi.doc.detector.DocDetector;
import com.cjbdi.doc.detector.DocType;
import com.cjbdi.fp.docs.Doc2Text;
import com.cjbdi.fp.text.html.HtmlParser;
import com.cjbdi.utils.CharsetUtil;
import com.cjbdi.wscommon.bean.WsBean;
import com.cjbdi.wscommon.bean.WsBeanWithFile;
import com.cjbdi.wscommon.bean.es.ESDataBean;
import com.cjbdi.wscommon.bean.es.ESEntitys;
import com.cjbdi.wscommon.bean.es.ESParagraphs;
import com.cjbdi.wscommon.bean.es.KafkaTopic2Bean;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

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
        String text = HtmlParser.getPlainText(c_wsText);
//        System.out.println(text);
        //将 text 转为 json 类型
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fullText", text);
        String fullText = JSONObject.toJSONString(jsonObject);
        try {
//            String post = sendPost("http://192.1.188.222:9001/getDoc", fullText);
            String post = "{\"code\":200,\"msg\":null,\"data\":{\"syncss\":0,\"docid\":null,\"uniqid\":\"0936529e-c385-436c-ae75-b052989611e3\",\"content\":\"贵州省铜仁市中级人民法院\\n民事判决书\\n（2018）黔 06民终609号\\n上诉人（原审被告）：中国平安财产保险股份有限公司贵州分公司普安支公司，住所地贵州省黔西南布依苗族自治州普安县盘水镇南大街。\\n负责人：杨兴贵，该公司经理。\\n委托诉讼代理人：赵朝忠，公司员工。\\n被上诉人（原审原告）：田春娥，女，1976年1月9日出生，土家族，务农，住贵州省沿河土家族自治县中界乡龙兴村下寨组。\\n委托诉讼代理人：张加顺，贵州精湛律师事务所律师。\\n被上诉人（原审被告）：张军，男，1983年11月3日出生，土家族，教师，住贵州省沿河土家族自治县和平镇团结街道文化路86号。\\n被上诉人（原审被告）：田丽琴（系张军之妻），女，1985年11月20日出生，土家族，无固定职业，住贵州省沿河土家族自治县和平镇团结街道文化路86号。\\n原审原告：张永波（系田春娥之夫），男，1975年9月4日出生，土家族，务农，住贵州省沿河土家族自治县中界乡龙兴村下寨组。\\n原审原告：张爽，男，2001年11月28日出生，土家族，学生，住贵州省沿河土家族自治县中界乡龙兴村下寨组。\\n法定代理人：张永波（系张爽之父），住贵州省沿河土家族自治县中界乡龙兴村下寨组。\\n法定代理人：田春娥（系张爽之母），住贵州省沿河土家族自治县中界乡龙兴村下寨组。\\n上诉人中国平安财产保险股份有限公司贵州分公司普安支公司（以下简称保险公司）因与被上诉人田春娥、张军、田丽琴，以及原审原告张永波、张爽机动车交通事故责任纠纷一案，不服贵州省沿河土家族自治县人民法院（2017）黔0627民初659号民事判决，向本院提起上诉。本院于2018年4月23日立案后，依法组成合议庭,开庭进行了审理。上诉人保险公司委托诉讼代理人赵朝忠、被上诉人田春娥及其委托诉讼代理人张加顺、张爽法定代理人田春娥到庭参加诉讼。张永波、张军、田丽琴经本院传票传唤，无正当理由拒不到庭。本案现已审理终结。\\n保险公司上诉请求：对田春娥治疗自身疾病的不合理费用30883.57元重新进行认定，并依法改判。事实和理由：田春娥所受外伤为左小腿软组织肿胀、左肘部左小腿皮肤挫伤，均系皮外伤，伤情极轻，住院72天明显不合理。且其所患“痹症”非外伤引起，属田春娥自身疾病，与本次交通事故无关联，该部分医疗费用及导致住院延长造成的损失不应由保险公司承担。\\n田春娥辩称，本人从沿河土家族自治县人民医院出院后，两次前往重庆第三军医大学第一附属医院检查，受伤的左小腿至今没有痊愈，该事实足以证明田春娥住院治疗72天是合理和必要的，一审法院认定的事实清楚，适用法律正确，请求维持原判。\\n张永波、田春娥、张爽向一审法院起诉请求：1.判令被告赔偿原告田春娥医疗费2718.70元、误工费12450元、护理费12450元、住院伙食补助费8300元、营养费2490元、交通费1975.5元、住宿费1600元、精神抚慰金3000元，共计44984.20元；2.判令被告赔偿原告张永波误工费1350元、护理费1350元、住院伙食补助费900元、营养费270元、交通费300元、精神抚慰金1000元，共计5170元；3.判令被告赔偿原告张爽护理费3900元、住院伙食补助费2600元、营养费780元、交通费300元、精神抚慰金1000元，共计8580元；4.案件受理费由被告承担。\\n一审法院认定事实：贵DBY196号小型轿车登记所有人为田丽琴，其在保险公司投保交强险和第三者商业险，保险期间是2016年1月31日至2017年1月30日。2016年9月30日，张军驾驶贵DBY196号小型轿车从沿河土家族自治县坝坨往县城方向行驶至四洲大道职校路段时，与张永波驾驶的乘坐田春娥、张爽的贵DDU200号二轮摩托车相撞，造成张永波、田春娥、张爽三人受伤的交通事故。该事故经沿河土家族自治县交警大队认定，张军违法驾驶车辆负事故全部责任。田春娥受伤后在沿河土家族自治县人民医院住院治疗72天，被中医诊断为“痹证”，西医诊断为“左小腿软组织肿胀、左肘部左小腿皮肤挫伤”；张永波在沿河土家族自治县人民医院住院治疗9天，被诊断为“全身多处软组织伤、左面部及膝部皮肤挫伤”；张爽在沿河土家族自治县人民医院住院治疗26天，被诊断为“全身多处软组织伤、左足背皮肤挫伤”，张军支付了三人在沿河土家族自治县人民医院住院治疗费用。田春娥在沿河土家族自治县人民院住院治疗期间，于2016年11月28日到重庆第三军医大学第一附属医院进行诊疗，张军支付给田春娥车费、住宿、生活费共计5100元。 2016年12月28日，田春娥到重庆第三军医大学第一附属医院诊疗4天, 花去诊疗费1512.5元；2017年1月3日至7日田春娥到重庆第三军医大学第一附属医院诊疗5天，支付诊疗费1006.2元。田春娥后两次到重庆诊疗花去交通费917元、住宿费500元、生活费26元。 一审确认田春娥损失：①医疗费2718.70元，根据提供的医疗发票认定2517.5元；②误工费，从2016年9月30日受伤之日起至2016年12月12日止共住院72天，重庆第一次检查诊疗2天、第二次检查诊疗4天、第三次诊疗5天，2016年11月27日到重庆诊疗的天数已含在沿河县人民医院住院的天数内，误工天数应按81天计算。田春娥无固定职业，根据2017年贵州省统计局、国家统计局贵州省城镇单位农、林、牧、渔业年平均工资53874元计算确认为11955.6元；③护理费，根据居民服务、修理和其他服务业标准确认为8487.47元；④住院伙食补助费 6480元；⑤交通费 917元；⑥住宿费500元；⑦生活费 26元，以上损失合计30883.57元。张永波损失：误工费1328.4元、护理费943.05元、住院伙食补助费720元，合计2991.45元。张爽损失：护理费2724.37元、住院伙食补助费 2080元，合计4804.37元。以上总计38679.39元。一审法院认为，公民的健康权受法律保护。行为人因过错侵害他人民事权益的，应当承担侵权责任。本案中，张军违法驾驶车辆发生交通事故，致张永波、田春娥、张爽受伤住院，侵害了其民事权益，张永波、田春娥、张爽请求赔偿，应予支持。经交警部门认定，张军对本案交通事故负全部责任，张军应赔偿张永波、田春娥、张爽的损失。张军所驾驶的贵DBY196号小型轿车登记所有人为田丽琴，肇事时在保险公司已投保交强险，根据《中华人民共和国道路交通安全法》第七十六条“机动车发生交通事故造成人身伤亡、财产损失的，由保险公司在机动车第三者责任强制保险责任限额范围内予以赔偿，不足部分，按照下列规定承担赔偿责任……”的规定，张永波、田春娥、张爽损失合计为38679.39元，属于交强险责任限额范围内，应由保险公司赔偿。 依照《中华人民共和国侵权责任法》第二条、第六条第一款、第十五条第一款第（六）项、第十六条，《中华人民共和国道路交通安全法》第七十六条，《最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释》第十七条、第十九条、第二十条、第二十一条、第二十二条、第二十三条，《中华人民共和国民事诉讼法》第六十四条第一款之规定，判决： 一、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿田春娥医疗费、误工费、护理费、交通费、住宿费、住院伙食补助费、生活费等共计30883.57元； 二、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿张永波误工费、护理费、住院伙食补助费共计2991.45元； 三、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿张爽护理费、住院伙食补助费共计4804.37元；四、驳回张永波、田春娥、张爽的其他诉讼请求。\\n二审中，当事人没有提交新证据。二审认定的事实与一审认定的事实一致。\\n本院认为，各方当事人对交通事故的发生、交警部门的责任认定，以及一审判决认定张永波、张爽因交通事故所受经济损失的数额均无异议，本院予以确认。田春娥因交通事故受伤，被送往沿河土家族自治县人民医院外科住院治疗，被诊断为左小腿软组织肿胀、左肘部左小腿皮肤挫伤，其病情经会诊后转入中医科，被诊断为痹症，继续给予活血、消肿、针刺等治疗，保险公司对田春娥提供的《住院病历》、《住院费用清单》等证据予以认可。现保险公司不能提供证明上述治疗方案、费用与田春娥外伤无关联，且系其治疗自身所患疾病的相关证据，根据《最高人民法院关于民事诉讼证据的若干规定》第二条“当事人对自己提出的诉讼请求所依据的事实或者反驳对方诉讼请求所依据的事实有责任提供证据加以证明。没有证据或者证据不足以证明当事人的事实主张的，由负有举证责任的当事人承担不利后果”的规定，对保险公司提出田春娥病情较轻、住院时间明显不合理，不应承担与交通事故无关的自身疾病所产生的医疗费用及导致住院延长造成的损失的上诉理由，本院不予支持。\\n综上所述，中国平安财产保险股份有限公司贵州分公司普安支公司的上诉请求不能成立，应予驳回；一审判决认定事实清楚，适用法律正确，应予维持。依照《中华人民共和国民事诉讼法》第一百四十四条、第一百七十条第一款第一项规定，判决如下：\\n驳回上诉，维持原判。\\n二审案件受理费572元，由中国平安财产保险股份有限公司贵州分公司普安支公司负担。\\n本判决为终审判决。\\n审 判 长 吴 晓 慧\\n审 判 员 张 红 芳\\n审 判 员 张 金 勇\\n二〇一八年七月十一日\\n书 记 员 郑 文\\n\",\"path\":\"20150000.zip\\\\/ffff6d12a48d49908ac1a84d00b74cf5.txt\",\"title\":\"贵州省铜仁市中级人民法院\",\"doctype\":\"判决书\",\"referencetype\":\"普通案例\",\"referencetypeorder\":null,\"province\":\"贵州省\",\"courtname\":\"铜仁市中级人民法院\",\"courtlevel\":\"中级法院\",\"courtid\":\"2832\",\"courtpid\":\"2783\",\"parentcourtname\":\"贵州省高级人民法院\",\"casetype\":\"民事\",\"procedure\":\"二审\",\"casecause\":\"保险纠纷\",\"casecausefull\":[\"民事\",\"与公司、证券、保险、票据等有关的民事纠纷\",\"保险纠纷\"],\"multicasecause\":[\"保险纠纷\",\"机动车交通事故责任纠纷\"],\"scoremap\":{\"民事/与公司、证券、保险、票据等有关的民事纠纷/保险纠纷\":3.2,\"民事/侵权责任纠纷/侵权责任纠纷/机动车交通事故责任纠纷\":1.0},\"caseid\":\"（2018）黔 06民终609号\",\"relatedcaseid\":[\"（2017）黔0627民初659号\"],\"precaseid\":\"（2017）黔0627民初659号\",\"acceptdate\":\"2018-04-23\",\"judgedate\":\"2018-07-11\",\"publishdate\":\"\",\"applicablelaws\":[\"最高人民法院关于民事诉讼证据的若干规定,第二条\",\"中华人民共和国民事诉讼法,第一百四十四条\",\"中华人民共和国民事诉讼法,第一百七十条,第一款,第一项\"],\"relatedlaws\":[\"中华人民共和国道路交通安全法,第七十六条\",\"中华人民共和国侵权责任法,第二条\",\"中华人民共和国侵权责任法,第六条,第一款\",\"中华人民共和国侵权责任法,第十五条,第一款,第六项\",\"中华人民共和国侵权责任法,第十六条\",\"中华人民共和国道路交通安全法,第七十六条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第十七条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第十九条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第二十条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第二十一条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第二十二条\",\"最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释,第二十三条\",\"中华人民共和国民事诉讼法,第六十四条,第一款\",\"最高人民法院关于民事诉讼证据的若干规定,第二条\",\"中华人民共和国民事诉讼法,第一百四十四条\",\"中华人民共和国民事诉讼法,第一百七十条,第一款,第一项\"],\"litigantslist\":[{\"type\":1,\"nametype\":1,\"name\":\"中国平安财产保险股份有限公司贵州分公司普安支公司\",\"companyname\":\"中国平安财产保险股份有限公司贵州分公司普安支公司\",\"gender\":null,\"nationality\":\"苗族自治州普安\",\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":\"贵州省黔西南布依苗族自治州普安县盘水镇南大街\",\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"上诉人\",\"原审被告\"],\"tags\":[\"住所地贵州省黔西南布依苗族自治州普安县盘水镇南大街\"]},{\"type\":8,\"nametype\":1,\"name\":\"杨兴贵\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"负责人\"],\"tags\":[\"该公司经理\"]},{\"type\":8,\"nametype\":1,\"name\":\"赵朝忠\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"委托诉讼代理人\"],\"tags\":[\"公司员工\"]},{\"type\":2,\"nametype\":2,\"name\":\"田春娥\",\"companyname\":null,\"gender\":\"女\",\"nationality\":\"土家族\",\"birthday\":\"1976-01-09\",\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":\"贵州省沿河土家族自治县中界乡龙兴村下寨组\",\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"被上诉人\",\"原审原告\"],\"tags\":[\"女\",\"1976年1月9日出生\",\"土家族\",\"务农\",\"住贵州省沿河土家族自治县中界乡龙兴村下寨组\"]},{\"type\":16,\"nametype\":2,\"name\":\"张加顺\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":\"贵州精湛律师事务所\",\"isAssigned\":null,\"identity\":[\"委托诉讼代理人\",\"律师\"],\"tags\":[\"贵州精湛律师事务所\"]},{\"type\":2,\"nametype\":2,\"name\":\"张军\",\"companyname\":null,\"gender\":\"男\",\"nationality\":\"土家族\",\"birthday\":\"1983-11-03\",\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":\"贵州省沿河土家族自治县和平镇团结街道文化路86号\",\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"被上诉人\",\"原审被告\"],\"tags\":[\"男\",\"1983年11月3日出生\",\"土家族\",\"教师\",\"住贵州省沿河土家族自治县和平镇团结街道文化路86号\"]},{\"type\":2,\"nametype\":2,\"name\":\"田丽琴\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"被上诉人\",\"原审被告\"],\"tags\":[\"系张军之妻\",\"女\",\"1985年11月20日出生\",\"土家族\",\"无固定职业\",\"住贵州省沿河土家族自治县和平镇团结街道文化路86号\"]},{\"type\":8,\"nametype\":2,\"name\":\"张永波\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"法定代理人\"],\"tags\":[\"系张爽之父\",\"住贵州省沿河土家族自治县中界乡龙兴村下寨组\"]},{\"type\":8,\"nametype\":2,\"name\":\"田春娥\",\"companyname\":null,\"gender\":null,\"nationality\":null,\"birthday\":null,\"idcard\":null,\"orgcard\":null,\"education\":null,\"profession\":null,\"address\":null,\"lawfirm\":null,\"isAssigned\":null,\"identity\":[\"法定代理人\"],\"tags\":[\"系张爽之母\",\"住贵州省沿河土家族自治县中界乡龙兴村下寨组\"]}],\"judgememberslist\":[{\"type\":31,\"name\":\"吴晓慧\",\"identity\":[\"审判长\"]},{\"type\":32,\"name\":\"张红芳\",\"identity\":[\"审判员\"]},{\"type\":32,\"name\":\"张金勇\",\"identity\":[\"审判员\"]},{\"type\":33,\"name\":\"郑文\",\"identity\":[\"书记员\"]}],\"paragraphs\":[{\"tag\":\"审理法院\",\"content\":\"贵州省铜仁市中级人民法院\"},{\"tag\":\"标题\",\"content\":\"民事判决书\"},{\"tag\":\"案号\",\"content\":\"(2018)黔 06民终609号\"},{\"tag\":\"当事人\",\"content\":\"上诉人(原审被告):中国平安财产保险股份有限公司贵州分公司普安支公司,住所地贵州省黔西南布依苗族自治州普安县盘水镇南大街。\"},{\"tag\":\"当事人\",\"content\":\"负责人:杨兴贵,该公司经理。\"},{\"tag\":\"当事人\",\"content\":\"委托诉讼代理人:赵朝忠,公司员工。\"},{\"tag\":\"当事人\",\"content\":\"被上诉人(原审原告):田春娥,女,1976年1月9日出生,土家族,务农,住贵州省沿河土家族自治县中界乡龙兴村下寨组。\"},{\"tag\":\"律师\",\"content\":\"委托诉讼代理人:张加顺,贵州精湛律师事务所律师。\"},{\"tag\":\"当事人\",\"content\":\"被上诉人(原审被告):张军,男,1983年11月3日出生,土家族,教师,住贵州省沿河土家族自治县和平镇团结街道文化路86号。\"},{\"tag\":\"当事人\",\"content\":\"被上诉人(原审被告):田丽琴(系张军之妻),女,1985年11月20日出生,土家族,无固定职业,住贵州省沿河土家族自治县和平镇团结街道文化路86号。\"},{\"tag\":\"当事人\",\"content\":\"原审原告:张永波(系田春娥之夫),男,1975年9月4日出生,土家族,务农,住贵州省沿河土家族自治县中界乡龙兴村下寨组。\"},{\"tag\":\"当事人\",\"content\":\"原审原告:张爽,男,2001年11月28日出生,土家族,学生,住贵州省沿河土家族自治县中界乡龙兴村下寨组。\"},{\"tag\":\"当事人\",\"content\":\"法定代理人:张永波(系张爽之父),住贵州省沿河土家族自治县中界乡龙兴村下寨组。\"},{\"tag\":\"当事人\",\"content\":\"法定代理人:田春娥(系张爽之母),住贵州省沿河土家族自治县中界乡龙兴村下寨组。\"},{\"tag\":\"审理经过\",\"content\":\"上诉人中国平安财产保险股份有限公司贵州分公司普安支公司(以下简称保险公司)因与被上诉人田春娥、张军、田丽琴,以及原审原告张永波、张爽机动车交通事故责任纠纷一案,不服贵州省沿河土家族自治县人民法院(2017)黔0627民初659号民事判决,向本院提起上诉。本院于2018年4月23日立案后,依法组成合议庭,开庭进行了审理。上诉人保险公司委托诉讼代理人赵朝忠、被上诉人田春娥及其委托诉讼代理人张加顺、张爽法定代理人田春娥到庭参加诉讼。张永波、张军、田丽琴经本院传票传唤,无正当理由拒不到庭。本案现已审理终结。\"},{\"tag\":\"诉称\",\"content\":\"保险公司上诉请求:对田春娥治疗自身疾病的不合理费用30883.57元重新进行认定,并依法改判。事实和理由:田春娥所受外伤为左小腿软组织肿胀、左肘部左小腿皮肤挫伤,均系皮外伤,伤情极轻,住院72天明显不合理。且其所患“痹症”非外伤引起,属田春娥自身疾病,与本次交通事故无关联,该部分医疗费用及导致住院延长造成的损失不应由保险公司承担。\"},{\"tag\":\"前审经过\",\"content\":\"田春娥辩称,本人从沿河土家族自治县人民医院出院后,两次前往重庆第三军医大学第一附属医院检查,受伤的左小腿至今没有痊愈,该事实足以证明田春娥住院治疗72天是合理和必要的,一审法院认定的事实清楚,适用法律正确,请求维持原判。\"},{\"tag\":\"前审经过\",\"content\":\"张永波、田春娥、张爽向一审法院起诉请求:1.判令被告赔偿原告田春娥医疗费2718.70元、误工费12450元、护理费12450元、住院伙食补助费8300元、营养费2490元、交通费1975.5元、住宿费1600元、精神抚慰金3000元,共计44984.20元;2.判令被告赔偿原告张永波误工费1350元、护理费1350元、住院伙食补助费900元、营养费270元、交通费300元、精神抚慰金1000元,共计5170元;3.判令被告赔偿原告张爽护理费3900元、住院伙食补助费2600元、营养费780元、交通费300元、精神抚慰金1000元,共计8580元;4.案件受理费由被告承担。\"},{\"tag\":\"前审经过\",\"content\":\"一审法院认定事实:贵DBY196号小型轿车登记所有人为田丽琴,其在保险公司投保交强险和第三者商业险,保险期间是2016年1月31日至2017年1月30日。2016年9月30日,张军驾驶贵DBY196号小型轿车从沿河土家族自治县坝坨往县城方向行驶至四洲大道职校路段时,与张永波驾驶的乘坐田春娥、张爽的贵DDU200号二轮摩托车相撞,造成张永波、田春娥、张爽三人受伤的交通事故。该事故经沿河土家族自治县交警大队认定,张军违法驾驶车辆负事故全部责任。田春娥受伤后在沿河土家族自治县人民医院住院治疗72天,被中医诊断为“痹证”,西医诊断为“左小腿软组织肿胀、左肘部左小腿皮肤挫伤”;张永波在沿河土家族自治县人民医院住院治疗9天,被诊断为“全身多处软组织伤、左面部及膝部皮肤挫伤”;张爽在沿河土家族自治县人民医院住院治疗26天,被诊断为“全身多处软组织伤、左足背皮肤挫伤”,张军支付了三人在沿河土家族自治县人民医院住院治疗费用。田春娥在沿河土家族自治县人民院住院治疗期间,于2016年11月28日到重庆第三军医大学第一附属医院进行诊疗,张军支付给田春娥车费、住宿、生活费共计5100元。 2016年12月28日,田春娥到重庆第三军医大学第一附属医院诊疗4天, 花去诊疗费1512.5元;2017年1月3日至7日田春娥到重庆第三军医大学第一附属医院诊疗5天,支付诊疗费1006.2元。田春娥后两次到重庆诊疗花去交通费917元、住宿费500元、生活费26元。 一审确认田春娥损失:①医疗费2718.70元,根据提供的医疗发票认定2517.5元;②误工费,从2016年9月30日受伤之日起至2016年12月12日止共住院72天,重庆第一次检查诊疗2天、第二次检查诊疗4天、第三次诊疗5天,2016年11月27日到重庆诊疗的天数已含在沿河县人民医院住院的天数内,误工天数应按81天计算。田春娥无固定职业,根据2017年贵州省统计局、国家统计局贵州省城镇单位农、林、牧、渔业年平均工资53874元计算确认为11955.6元;③护理费,根据居民服务、修理和其他服务业标准确认为8487.47元;④住院伙食补助费 6480元;⑤交通费 917元;⑥住宿费500元;⑦生活费 26元,以上损失合计30883.57元。张永波损失:误工费1328.4元、护理费943.05元、住院伙食补助费720元,合计2991.45元。张爽损失:护理费2724.37元、住院伙食补助费 2080元,合计4804.37元。以上总计38679.39元。一审法院认为,公民的健康权受法律保护。行为人因过错侵害他人民事权益的,应当承担侵权责任。本案中,张军违法驾驶车辆发生交通事故,致张永波、田春娥、张爽受伤住院,侵害了其民事权益,张永波、田春娥、张爽请求赔偿,应予支持。经交警部门认定,张军对本案交通事故负全部责任,张军应赔偿张永波、田春娥、张爽的损失。张军所驾驶的贵DBY196号小型轿车登记所有人为田丽琴,肇事时在保险公司已投保交强险,根据《中华人民共和国道路交通安全法》第七十六条“机动车发生交通事故造成人身伤亡、财产损失的,由保险公司在机动车第三者责任强制保险责任限额范围内予以赔偿,不足部分,按照下列规定承担赔偿责任……”的规定,张永波、田春娥、张爽损失合计为38679.39元,属于交强险责任限额范围内,应由保险公司赔偿。 依照《中华人民共和国侵权责任法》第二条、第六条第一款、第十五条第一款第(六)项、第十六条,《中华人民共和国道路交通安全法》第七十六条,《最高人民法院关于审理人身损害赔偿案件适用法律若干问题的解释》第十七条、第十九条、第二十条、第二十一条、第二十二条、第二十三条,《中华人民共和国民事诉讼法》第六十四条第一款之规定,判决: 一、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿田春娥医疗费、误工费、护理费、交通费、住宿费、住院伙食补助费、生活费等共计30883.57元; 二、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿张永波误工费、护理费、住院伙食补助费共计2991.45元; 三、由中国平安财产保险股份有限公司贵州分公司普安支公司于本判决生效后十日内赔偿张爽护理费、住院伙食补助费共计4804.37元;四、驳回张永波、田春娥、张爽的其他诉讼请求。\"},{\"tag\":\"前审经过\",\"content\":\"二审中,当事人没有提交新证据。二审认定的事实与一审认定的事实一致。\"},{\"tag\":\"本院认为\",\"content\":\"本院认为,各方当事人对交通事故的发生、交警部门的责任认定,以及一审判决认定张永波、张爽因交通事故所受经济损失的数额均无异议,本院予以确认。田春娥因交通事故受伤,被送往沿河土家族自治县人民医院外科住院治疗,被诊断为左小腿软组织肿胀、左肘部左小腿皮肤挫伤,其病情经会诊后转入中医科,被诊断为痹症,继续给予活血、消肿、针刺等治疗,保险公司对田春娥提供的《住院病历》、《住院费用清单》等证据予以认可。现保险公司不能提供证明上述治疗方案、费用与田春娥外伤无关联,且系其治疗自身所患疾病的相关证据,根据《最高人民法院关于民事诉讼证据的若干规定》第二条“当事人对自己提出的诉讼请求所依据的事实或者反驳对方诉讼请求所依据的事实有责任提供证据加以证明。没有证据或者证据不足以证明当事人的事实主张的,由负有举证责任的当事人承担不利后果”的规定,对保险公司提出田春娥病情较轻、住院时间明显不合理,不应承担与交通事故无关的自身疾病所产生的医疗费用及导致住院延长造成的损失的上诉理由,本院不予支持。\"},{\"tag\":\"本院认为\",\"content\":\"综上所述,中国平安财产保险股份有限公司贵州分公司普安支公司的上诉请求不能成立,应予驳回;一审判决认定事实清楚,适用法律正确,应予维持。依照《中华人民共和国民事诉讼法》第一百四十四条、第一百七十条第一款第一项规定,判决如下:\"},{\"tag\":\"裁判结果\",\"content\":\"驳回上诉,维持原判。\"},{\"tag\":\"裁判结果\",\"content\":\"二审案件受理费572元,由中国平安财产保险股份有限公司贵州分公司普安支公司负担。\"},{\"tag\":\"裁判结果\",\"content\":\"本判决为终审判决。\"},{\"tag\":\"审判人员\",\"content\":\"审判长吴晓慧\"},{\"tag\":\"审判人员\",\"content\":\"审判员张红芳\"},{\"tag\":\"审判人员\",\"content\":\"审判员张金勇\"},{\"tag\":\"日期\",\"content\":\"二〇一八年七月十一日\"},{\"tag\":\"书记员\",\"content\":\"书记员郑文\"}],\"features\":[],\"disputefocus\":[],\"focuscontent\":[],\"proof\":[],\"general\":[],\"judgeresult\":[{\"结案方式\":\"维持\",\"原文\":\"驳回上诉,维持原判。\",\"胜败诉结果\":\"胜诉\",\"刑事判决刑期\":[]},{\"原告\":[{\"名称\":\"中国平安财产保险股份有限公司贵州分公司普安支公司\"}],\"结案方式\":null,\"原文\":\"二审案件受理费572元,由中国平安财产保险股份有限公司贵州分公司普安支公司负担。\",\"胜败诉结果\":\"胜诉\",\"金额\":[{\"数额\":572.0,\"原文\":\"572元\"}],\"刑事判决刑期\":[]},{\"结案方式\":\"判决\",\"原文\":\"本判决为终审判决。\",\"胜败诉结果\":\"胜诉\",\"刑事判决刑期\":[]}],\"conclusions\":null,\"procuratoratecaseid\":[],\"extra\":null,\"amount\":0.0,\"question\":null,\"qcasecause\":null,\"kwords\":null,\"segtype\":null,\"zhichanFrq\":{\"诉讼\":2,\"驳回诉讼请求\":1,\"证据\":24,\"当事人\":2},\"flag\":0,\"jafs\":null},\"traceId\":null}\n";
            JSONObject jo1 = JSON.parseObject(post);
            String data = jo1.getString("data");
            JSONObject jo2 = JSON.parseObject(data);
            ESDataBean esDataBean = JSON.parseObject(data, ESDataBean.class);

            //解析结果和 bean 名称不一样的字段
            esDataBean.court = jo2.getString("courtname");
            esDataBean.applicable_law = jo2.getString("applicablelaws");
            esDataBean.related_law = jo2.getString("relatedlows");
            esDataBean.pcourt = jo2.getString("parentcourtname");

            ArrayList<ESEntitys> esEntitys = new ArrayList<>();
            JSONArray litigantslist = jo2.getJSONArray("litigantslist");
            for (int i = 0; i < litigantslist.size(); i++) {
                ESEntitys entitys = litigantslist.getObject(i, ESEntitys.class);
                esEntitys.add(entitys);
            }
            esDataBean.entities = esEntitys;


            JSONArray judgememberslist = jo2.getJSONArray("judgememberslist");
            String clerk1 = "";
            for (int i = 0; i < judgememberslist.size(); i++) {
                ESDataBean esDataBean1 = judgememberslist.getObject(i, ESDataBean.class);
            }
            esDataBean.clerk = clerk1;


//            List<ESParagraphs> paragraphs = esDataBean.paragraphs;
//            HashMap<String, String> stringStringHashMap = new HashMap<>();
//            for (ESParagraphs paragraph : paragraphs) {
//
//                if (stringStringHashMap.containsKey(paragraph.tag)) {
//                    stringStringHashMap.put(paragraph.tag, stringStringHashMap.get(paragraph.tag) + paragraph.content);
//                } else {
//                    stringStringHashMap.put(paragraph.tag, paragraph.content);
//                }
//
//            }
//            esDataBean.court = stringStringHashMap.getOrDefault("审理法院", null);
//            esDataBean.before_instance = stringStringHashMap.getOrDefault("审理经过", null);
//            esDataBean.appellant_opinion = stringStringHashMap.getOrDefault("诉称", null);
//            esDataBean.court_consider = stringStringHashMap.getOrDefault("本院认为", null);
//            esDataBean.judge_decision = stringStringHashMap.getOrDefault("裁判结果", null);
//            esDataBean.clerk = stringStringHashMap.getOrDefault("书记员", null);
//

//            JSONArray paragraphsArray= jo2.getJSONArray("paragraphs");
//          for (int i = 0; i < paragraphsArray.size(); i++) {
//                ESParagraphs temp = paragraphsArray.getObject(i, ESParagraphs.class);
//                String content = temp.content;
//                String tag = temp.tag;
//                if (tag.equals("审理法院"))
//                {
//                    esDataBean.court = content;
//                } else if (tag.equals("案号")) {
//                    esDataBean.caseid = content;
//                }
//
//            }
//            System.out.println(jo2);
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
