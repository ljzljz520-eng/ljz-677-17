package com.excel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导入异常数据
 * 保存导入时校验失败/保存失败的原始行，用于导出异常模板供用户修正后重新上传
 */
@Data
@TableName("import_error")
public class ImportError {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 导入批次号（任务号）
     */
    private String batchNo;

    /**
     * 原始Excel行号
     */
    private Integer rowIndex;

    /**
     * 数据编号
     */
    private String dataCode;

    /**
     * 姓名
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
     * 错误原因
     */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
