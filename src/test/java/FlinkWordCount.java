import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Author: XYH
 * @Date: 2021/11/23 4:06 下午
 * @Description: flink 测试 wordCount
 */
public class FlinkWordCount {
    public static void main(String[] args) throws Exception {
        //创建流式计算的ExecutionEnvironment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //调用Source，指定Socket地址和端口
        DataStream<String> lines = env.socketTextStream(args[0], Integer.parseInt(args[1]));
        //切分压平并将单词和一放入元组中
        DataStream<Tuple2<String, Integer>> words = lines.
                flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> collector)
                            throws Exception {
                        String[] words = line.split(" ");
                        for (String word : words) {
                            collector.collect(Tuple2.of(word, 1));
                        }
                    }
                });
        //按照key分组并聚合
        DataStream<Tuple2<String, Integer>> result = words.keyBy(0).sum(1);
        //将结果打印到控制台
        result.print();
        //执行
        env.execute("StreamingWordCount");
    }
}
