package com.excel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResultDTO {

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 总记录数
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 错误数据列表
     */
    private List<ExcelDataDTO> errorList;

    /**
     * 导入状态：processing, completed, failed
     */
    private String status;

    /**
     * 提示消息
     */
    private String message;
}
