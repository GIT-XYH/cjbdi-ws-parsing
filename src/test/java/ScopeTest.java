import com.cjbdi.config.FlinkConfig;
import com.cjbdi.config.KafkaConfig;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.IOException;

/**
 * @Author: XYH
 * @Date: 2021/12/3 2:16 下午
 * @Description: TODO
 */
public class ScopeTest {
    public static void main(String[] args) throws IOException {
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile("/Users/xuyuanhang/Desktop/code/demo/cjbdi-ws-parsingDemo/src/main/resources/parameter.properties");
        KafkaConfig.KafkaEnv(parameterTool);
        System.out.println(KafkaConfig.brokers);
    }
}
