package com.cjbdi.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: XYH
 * @Date: 2021/12/1 10:54 上午
 * @Description: 字段和文书源文件结合成的bean
 */


@Data
@ToString
@AllArgsConstructor
public class WsBeanFromKafka {
    private WsBean wsBean;
    private String base64File;          //文书原文件,为Base64编码的字节数组
}
