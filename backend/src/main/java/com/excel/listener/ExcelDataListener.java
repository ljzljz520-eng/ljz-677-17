package com.excel.listener;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.excel.dto.ExcelDataDTO;
import com.excel.entity.ExcelData;
import com.excel.mapper.ExcelDataMapper;
import com.excel.utils.ValidationUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel 数据监听器
 * 使用SAX模式逐行解析，避免OOM
 */
public class ExcelDataListener implements ReadListener<ExcelDataDTO> {

    private static final Logger logger = LoggerFactory.getLogger(ExcelDataListener.class);

    /**
     * 每隔1000条存储数据库，然后清理list，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 缓存的数据
     */
    private List<ExcelData> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * 错误数据列表
     */
    @Getter
    private final List<ExcelDataDTO> errorList = new ArrayList<>();

    /**
     * 成功条数
     */
    @Getter
    private int successCount = 0;

    /**
     * 失败条数
     */
    @Getter
    private int failCount = 0;

    /**
     * 总条数
     */
    @Getter
    private int totalCount = 0;

    private final ExcelDataMapper excelDataMapper;
    private final String batchNo;

    public ExcelDataListener(ExcelDataMapper excelDataMapper, String batchNo) {
        this.excelDataMapper = excelDataMapper;
        this.batchNo = batchNo;
    }

    @Override
    public void invoke(ExcelDataDTO data, AnalysisContext context) {
        totalCount++;
        Integer rowIndex = context.readRowHolder().getRowIndex() + 1;
        data.setRowIndex(rowIndex);

        // 数据校验
        String errorMsg = ValidationUtils.validate(data);
        if (errorMsg != null) {
            data.setErrorMsg(errorMsg);
            errorList.add(data);
            failCount++;
            logger.warn("第{}行数据校验失败: {}", rowIndex, errorMsg);
            return;
        }

        // 转换为实体
        ExcelData entity = new ExcelData();
        BeanUtil.copyProperties(data, entity);
        entity.setBatchNo(batchNo);
        entity.setReportStatus(0);

        cachedDataList.add(entity);

        // 达到批量大小，进行存储
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存最后一批数据
        if (!cachedDataList.isEmpty()) {
            saveData();
        }
        logger.info("Excel解析完成！总计：{}条，成功：{}条，失败：{}条", totalCount, successCount, failCount);
    }

    /**
     * 批量保存数据
     */
    private void saveData() {
        logger.debug("开始批量保存数据，数量：{}", cachedDataList.size());
        try {
            for (ExcelData data : cachedDataList) {
                excelDataMapper.insert(data);
                successCount++;
            }
            logger.debug("批量保存成功，数量：{}", cachedDataList.size());
        } catch (Exception e) {
            logger.error("批量保存失败", e);
            // 单条重试
            for (ExcelData data : cachedDataList) {
                try {
                    excelDataMapper.insert(data);
                    successCount++;
                } catch (Exception ex) {
                    failCount++;
                    ExcelDataDTO errorDto = new ExcelDataDTO();
                    BeanUtil.copyProperties(data, errorDto);
                    errorDto.setErrorMsg("数据库保存失败: " + ex.getMessage());
                    errorList.add(errorDto);
                }
            }
        }
    }
}
