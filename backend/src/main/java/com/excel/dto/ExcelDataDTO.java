package com.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExcelDataDTO {

    @ExcelProperty(value = "数据编号", index = 0)
    @ColumnWidth(15)
    private String dataCode;

    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(12)
    private String name;

    @ExcelProperty(value = "身份证号", index = 2)
    @ColumnWidth(22)
    private String idCard;

    @ExcelProperty(value = "手机号", index = 3)
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty(value = "金额", index = 4)
    @ColumnWidth(12)
    private BigDecimal amount;

    @ExcelProperty(value = "地址", index = 5)
    @ColumnWidth(30)
    private String address;

    @ExcelProperty(value = "备注", index = 6)
    @ColumnWidth(25)
    private String remark;

    /**
     * 行号，用于错误定位
     */
    private Integer rowIndex;

    /**
     * 错误信息
     */
    private String errorMsg;
}
