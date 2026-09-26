package com.excel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("excel_data")
public class ExcelData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据编号
     */
    private String dataCode;

    /**
     * 名称
     */
    private String name;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 上传批次号
     */
    private String batchNo;

    /**
     * 上报状态：0-待上报 1-已上报 2-上报失败
     */
    private Integer reportStatus;

    /**
     * 上报结果信息
     */
    private String reportMessage;

    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
