package com.excel.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excel.dto.ApiResponse;
import com.excel.dto.ErrorExportDTO;
import com.excel.dto.ExcelDataDTO;
import com.excel.dto.ImportResultDTO;
import com.excel.dto.ReportResultDTO;
import com.excel.entity.ExcelData;
import com.excel.entity.ImportRecord;
import com.excel.service.ExcelImportService;
import com.excel.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Tag(name = "Excel导入管理", description = "Excel数据导入与上报接口")
public class ExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);

    private final ExcelImportService excelImportService;
    private final ReportService reportService;

    @PostMapping("/import")
    @Operation(summary = "导入Excel", description = "上传Excel文件进行数据导入")
    public ApiResponse<ImportResultDTO> importExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ApiResponse.error("仅支持Excel文件（.xlsx或.xls）");
            }

            Long userId = (Long) authentication.getPrincipal();
            ImportResultDTO result = excelImportService.importExcel(file, userId);
            return ApiResponse.success("导入完成", result);
        } catch (Exception e) {
            logger.error("Excel导入失败", e);
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    @Operation(summary = "获取导入记录", description = "分页获取导入记录列表")
    public ApiResponse<Page<ImportRecord>> getImportRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ImportRecord> page = excelImportService.getImportRecords(pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/data/{batchNo}")
    @Operation(summary = "获取批次数据", description = "根据批次号分页获取数据")
    public ApiResponse<Page<ExcelData>> getDataByBatch(
            @PathVariable String batchNo,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ExcelData> page = excelImportService.getDataByBatch(batchNo, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @PostMapping("/report/{batchNo}")
    @Operation(summary = "上报数据", description = "将指定批次数据上报到国家平台")
    public ApiResponse<ReportResultDTO> reportData(@PathVariable String batchNo) {
        try {
            ReportResultDTO result = reportService.reportToNationalPlatform(batchNo);
            return ApiResponse.success("上报完成", result);
        } catch (Exception e) {
            logger.error("数据上报失败", e);
            return ApiResponse.error("上报失败: " + e.getMessage());
        }
    }

    @GetMapping("/report/failed/{batchNo}")
    @Operation(summary = "获取上报失败数据", description = "获取指定批次上报失败的数据")
    public ApiResponse<List<ExcelData>> getFailedReportData(@PathVariable String batchNo) {
        List<ExcelData> failedList = reportService.getFailedReportData(batchNo);
        return ApiResponse.success(failedList);
    }

    @PostMapping("/report/retry/{batchNo}")
    @Operation(summary = "重试上报", description = "重新上报失败的数据")
    public ApiResponse<ReportResultDTO> retryReport(@PathVariable String batchNo) {
        try {
            // 先重置失败数据状态
            reportService.resetFailedData(batchNo);
            // 再次上报
            ReportResultDTO result = reportService.reportToNationalPlatform(batchNo);
            return ApiResponse.success("重新上报完成", result);
        } catch (Exception e) {
            logger.error("重新上报失败", e);
            return ApiResponse.error("重新上报失败: " + e.getMessage());
        }
    }

    @GetMapping("/template")
    @Operation(summary = "下载导入模板", description = "下载Excel导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("数据导入模板", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 生成模板数据
        List<ExcelDataDTO> templateData = new ArrayList<>();
        ExcelDataDTO example = new ExcelDataDTO();
        example.setDataCode("DATA001");
        example.setName("张三");
        example.setIdCard("110101199001011234");
        example.setPhone("13800138000");
        example.setAmount(new BigDecimal("1000.00"));
        example.setAddress("北京市朝阳区xxx街道");
        example.setRemark("示例数据");
        templateData.add(example);

        EasyExcel.write(response.getOutputStream(), ExcelDataDTO.class)
                .sheet("数据导入模板")
                .doWrite(templateData);
    }

    @GetMapping("/export/errors/{batchNo}")
    @Operation(summary = "导出错误数据", description = "导出上报失败的数据为Excel")
    public void exportErrors(@PathVariable String batchNo, HttpServletResponse response) throws IOException {
        List<ExcelData> failedList = reportService.getFailedReportData(batchNo);

        // 转换为导出DTO，列顺序与导入模板一致，最后一列为错误原因
        List<ErrorExportDTO> exportList = new ArrayList<>();
        for (ExcelData data : failedList) {
            ErrorExportDTO dto = new ErrorExportDTO();
            dto.setDataCode(data.getDataCode());
            dto.setName(data.getName());
            dto.setIdCard(data.getIdCard());
            dto.setPhone(data.getPhone());
            dto.setAmount(data.getAmount());
            dto.setAddress(data.getAddress());
            dto.setRemark(data.getRemark());
            dto.setErrorMsg(data.getReportMessage());
            exportList.add(dto);
        }

        String fileName = "上报失败数据_" + batchNo + "_共" + exportList.size() + "条";
        writeErrorExcel(response, fileName, "上报失败数据", exportList);
    }

    @GetMapping("/export/import-errors/{batchNo}")
    @Operation(summary = "下载异常数据模板", description = "导出导入校验失败的异常数据，列顺序与导入模板一致，修正后可直接重新上传")
    public void exportImportErrors(@PathVariable String batchNo, HttpServletResponse response) throws IOException {
        List<ExcelDataDTO> errorList = excelImportService.getImportErrorData(batchNo);

        if (errorList.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(JSONUtil.toJsonStr(ApiResponse.error(404, "该批次没有可导出的异常数据")));
            return;
        }

        // 转换为导出DTO，列顺序与导入模板一致，最后一列为错误原因
        List<ErrorExportDTO> exportList = new ArrayList<>();
        for (ExcelDataDTO error : errorList) {
            ErrorExportDTO dto = new ErrorExportDTO();
            dto.setDataCode(error.getDataCode());
            dto.setName(error.getName());
            dto.setIdCard(error.getIdCard());
            dto.setPhone(error.getPhone());
            dto.setAmount(error.getAmount());
            dto.setAddress(error.getAddress());
            dto.setRemark(error.getRemark());
            dto.setErrorMsg(error.getErrorMsg());
            exportList.add(dto);
        }

        String fileName = "导入异常数据_" + batchNo + "_共" + exportList.size() + "条";
        writeErrorExcel(response, fileName, "异常数据", exportList);
    }

    /**
     * 写出异常数据Excel，文件名带任务号（批次号）和错误数量
     */
    private void writeErrorExcel(HttpServletResponse response, String fileName, String sheetName,
                                 List<ErrorExportDTO> exportList) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ErrorExportDTO.class)
                .sheet(sheetName)
                .doWrite(exportList);
    }
}
