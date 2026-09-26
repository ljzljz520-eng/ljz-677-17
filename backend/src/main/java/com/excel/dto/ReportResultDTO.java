package com.excel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportResultDTO {

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 上报总数
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
     * 上报状态
     */
    private String status;

    /**
     * 失败数据列表
     */
    private List<ReportErrorItem> errorList;

    /**
     * 提示消息
     */
    private String message;

    @Data
    @Builder
    public static class ReportErrorItem {
        private Long id;
        private String dataCode;
        private String name;
        private String errorMsg;
    }
}
