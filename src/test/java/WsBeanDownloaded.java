import com.cjbdi.bean.WsBeanFromKafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


/**
 * @Author: XYH
 * @Date: 2021/12/1 10:54 下午
 * @Description:
 */
@Data
@AllArgsConstructor
@ToString
public class WsBeanDownloaded {
    private WsBeanFromKafka wsBeanFromKafka;              //文书元数据
    private String base64File;          //文书原文件,为Base64编码的字节数组
}
