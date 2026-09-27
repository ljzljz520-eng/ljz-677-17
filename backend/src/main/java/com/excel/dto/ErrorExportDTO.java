package com.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 异常数据导出模板
 * 前7列与导入模板（ExcelDataDTO）列顺序完全一致，方便修正后直接复制回模板重新上传；
 * 末尾追加错误原因和原行号，仅作为修正参考，重新上传时会被自动忽略
 */
@Data
public class ErrorExportDTO {

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

    @ExcelProperty(value = "错误原因", index = 7)
    @ColumnWidth(40)
    private String errorMsg;

    @ExcelProperty(value = "原行号", index = 8)
    @ColumnWidth(10)
    private Integer rowIndex;
}
