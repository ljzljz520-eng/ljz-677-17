package com.excel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.excel.dto.ReportResultDTO;
import com.excel.entity.ExcelData;
import com.excel.mapper.ExcelDataMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据上报服务
 * 模拟数据上报到国家平台
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ExcelDataMapper excelDataMapper;

    /**
     * 上报数据到国家平台
     * 模拟上报过程，可能出现部分失败的情况
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportResultDTO reportToNationalPlatform(String batchNo) {
        logger.info("开始上报数据到国家平台，批次号: {}", batchNo);

        // 获取待上报数据
        List<ExcelData> pendingList = excelDataMapper.selectList(
                new LambdaQueryWrapper<ExcelData>()
                        .eq(ExcelData::getBatchNo, batchNo)
                        .eq(ExcelData::getReportStatus, 0)
        );

        if (pendingList.isEmpty()) {
            return ReportResultDTO.builder()
                    .batchNo(batchNo)
                    .totalCount(0)
                    .successCount(0)
                    .failCount(0)
                    .status("no_data")
                    .message("没有待上报的数据")
                    .errorList(new ArrayList<>())
                    .build();
        }

        int totalCount = pendingList.size();
        int successCount = 0;
        int failCount = 0;
        List<ReportResultDTO.ReportErrorItem> errorList = new ArrayList<>();

        Random random = new Random();

        // 模拟上报过程
        for (ExcelData data : pendingList) {
            try {
                // 模拟上报到国家平台
                boolean success = simulateReport(data, random);

                if (success) {
                    // 上报成功
                    excelDataMapper.update(null,
                            new LambdaUpdateWrapper<ExcelData>()
                                    .eq(ExcelData::getId, data.getId())
                                    .set(ExcelData::getReportStatus, 1)
                                    .set(ExcelData::getReportMessage, "上报成功")
                                    .set(ExcelData::getReportTime, LocalDateTime.now())
                    );
                    successCount++;
                } else {
                    // 上报失败
                    String errorMsg = generateErrorMessage(data, random);
                    excelDataMapper.update(null,
                            new LambdaUpdateWrapper<ExcelData>()
                                    .eq(ExcelData::getId, data.getId())
                                    .set(ExcelData::getReportStatus, 2)
                                    .set(ExcelData::getReportMessage, errorMsg)
                                    .set(ExcelData::getReportTime, LocalDateTime.now())
                    );
                    failCount++;
                    errorList.add(ReportResultDTO.ReportErrorItem.builder()
                            .id(data.getId())
                            .dataCode(data.getDataCode())
                            .name(data.getName())
                            .errorMsg(errorMsg)
                            .build());
                }
            } catch (Exception e) {
                failCount++;
                String errorMsg = "系统异常: " + e.getMessage();
                excelDataMapper.update(null,
                        new LambdaUpdateWrapper<ExcelData>()
                                .eq(ExcelData::getId, data.getId())
                                .set(ExcelData::getReportStatus, 2)
                                .set(ExcelData::getReportMessage, errorMsg)
                                .set(ExcelData::getReportTime, LocalDateTime.now())
                );
                errorList.add(ReportResultDTO.ReportErrorItem.builder()
                        .id(data.getId())
                        .dataCode(data.getDataCode())
                        .name(data.getName())
                        .errorMsg(errorMsg)
                        .build());
            }
        }

        String status = failCount == 0 ? "success" : (successCount == 0 ? "failed" : "partial_success");
        String message = String.format("上报完成，总计%d条，成功%d条，失败%d条", totalCount, successCount, failCount);

        logger.info("数据上报完成: {}", message);

        return ReportResultDTO.builder()
                .batchNo(batchNo)
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .status(status)
                .message(message)
                .errorList(errorList)
                .build();
    }

    /**
     * 模拟上报过程
     * 模拟可能的失败情况
     */
    private boolean simulateReport(ExcelData data, Random random) {
        // 模拟5%的失败率
        return random.nextInt(100) >= 5;
    }

    /**
     * 生成模拟错误信息
     */
    private String generateErrorMessage(ExcelData data, Random random) {
        String[] errorMessages = {
                "国家平台返回：数据格式不符合规范",
                "国家平台返回：重复数据已存在",
                "国家平台返回：身份证号校验失败",
                "国家平台返回：手机号格式错误",
                "国家平台返回：金额超出限额",
                "国家平台返回：服务暂时不可用",
                "国家平台返回：数据校验超时"
        };
        return errorMessages[random.nextInt(errorMessages.length)];
    }

    /**
     * 获取上报失败的数据
     */
    public List<ExcelData> getFailedReportData(String batchNo) {
        return excelDataMapper.selectList(
                new LambdaQueryWrapper<ExcelData>()
                        .eq(ExcelData::getBatchNo, batchNo)
                        .eq(ExcelData::getReportStatus, 2)
        );
    }

    /**
     * 重新上报失败的数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetFailedData(String batchNo) {
        excelDataMapper.update(null,
                new LambdaUpdateWrapper<ExcelData>()
                        .eq(ExcelData::getBatchNo, batchNo)
                        .eq(ExcelData::getReportStatus, 2)
                        .set(ExcelData::getReportStatus, 0)
                        .set(ExcelData::getReportMessage, null)
                        .set(ExcelData::getReportTime, null)
        );
    }
}
