package com.cjbdi.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: XYH
 * @Date: 2021/12/2 11:54 上午
 * @Description: 不含文书的bean
 */

@ToString
@Data
@AllArgsConstructor
public class WsBean {
    private String fbId;               //法标ID
    private String host;               //文书记录主机
    private String database;           //文书记录库(省份)
    private String schema;             //文书所属架构(案件类型)
    private String t_c_baah;           //案件案号
    private String t_n_jbfy;           //经办法院ID
    private String t_c_jbfymc;         //经办法院名称
    private String ws_c_nr;            //文书下载URL
    private String ws_n_lb;            //文书类别
    private String t_d_sarq;           //案件收案日期
    private String t_c_stm;
    private String ws_c_mc;
}
