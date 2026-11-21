package com.example.securitydefense.controller;

import com.example.securitydefense.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportController {

    /**
     * 获取安全事件统计报表
     */
    @GetMapping("/event-statistics")
    public Result<Map<String, Object>> getEventStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String type) {
        log.info("获取安全事件统计报表");
        
        Map<String, Object> statistics = new HashMap<>();
        Random random = new Random();
        
        // 按类型统计
        Map<String, Integer> byType = new HashMap<>();
        byType.put("SQL注入", 156);
        byType.put("XSS攻击", 89);
        byType.put("DDoS攻击", 45);
        byType.put("暴力破解", 234);
        byType.put("恶意扫描", 123);
        statistics.put("byType", byType);
        
        // 按级别统计
        Map<String, Integer> byLevel = new HashMap<>();
        byLevel.put("高危", 89);
        byLevel.put("中危", 234);
        byLevel.put("低危", 324);
        statistics.put("byLevel", byLevel);
        
        // 趋势数据
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            dates.add(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("MM-dd")));
            counts.add(random.nextInt(50) + 20);
        }
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", dates);
        trend.put("counts", counts);
        statistics.put("trend", trend);
        
        statistics.put("total", 647);
        statistics.put("handled", 589);
        statistics.put("pending", 58);
        
        return Result.success(statistics);
    }

    /**
     * 获取威胁情报分析
     */
    @GetMapping("/threat-intelligence")
    public Result<Map<String, Object>> getThreatIntelligence(
            @RequestParam(required = false) String period) {
        log.info("获取威胁情报分析");
        
        Map<String, Object> intelligence = new HashMap<>();
        
        // TOP攻击源
        List<Map<String, Object>> topSources = new ArrayList<>();
        String[] ips = {"45.142.212.61", "192.168.1.100", "103.224.182.245", "10.0.0.23", "172.16.5.88"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> source = new HashMap<>();
            source.put("ip", ips[i]);
            source.put("country", i % 2 == 0 ? "中国" : "美国");
            source.put("count", 156 - i * 20);
            source.put("risk", i % 3 == 0 ? "高" : "中");
            topSources.add(source);
        }
        intelligence.put("topSources", topSources);
        
        // 攻击手法分布
        List<Map<String, Object>> attackMethods = new ArrayList<>();
        String[] methods = {"漏洞利用", "社会工程", "暴力破解", "恶意软件", "钓鱼攻击"};
        int[] values = {234, 189, 156, 98, 67};
        for (int i = 0; i < methods.length; i++) {
            Map<String, Object> method = new HashMap<>();
            method.put("name", methods[i]);
            method.put("value", values[i]);
            attackMethods.add(method);
        }
        intelligence.put("attackMethods", attackMethods);
        
        // 威胁评级
        intelligence.put("threatLevel", "中等");
        intelligence.put("threatScore", 67);
        
        return Result.success(intelligence);
    }

    /**
     * 获取合规性报告
     */
    @GetMapping("/compliance")
    public Result<Map<String, Object>> getComplianceReport(
            @RequestParam(required = false) String standard) {
        log.info("获取合规性报告，标准: {}", standard);
        
        Map<String, Object> compliance = new HashMap<>();
        
        // 合规项检查
        List<Map<String, Object>> items = new ArrayList<>();
        String[] categories = {"访问控制", "数据保护", "日志审计", "漏洞管理", "应急响应"};
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", categories[i]);
            item.put("total", 10);
            item.put("passed", 8 - i);
            item.put("failed", 2 + i);
            item.put("score", (80 - i * 10));
            items.add(item);
        }
        compliance.put("items", items);
        
        // 总体评分
        compliance.put("overallScore", 78);
        compliance.put("complianceRate", 78.5);
        compliance.put("standard", standard != null ? standard : "等保2.0三级");
        
        // 不合规项
        List<Map<String, Object>> nonCompliant = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> nc = new HashMap<>();
            nc.put("id", i + 1);
            nc.put("description", "需要加强" + categories[i] + "措施");
            nc.put("severity", i == 0 ? "高" : "中");
            nc.put("suggestion", "建议立即整改");
            nonCompliant.add(nc);
        }
        compliance.put("nonCompliant", nonCompliant);
        
        return Result.success(compliance);
    }

    /**
     * 生成自定义报表
     */
    @PostMapping("/custom")
    public Result<Map<String, Object>> generateCustomReport(@RequestBody Map<String, Object> data) {
        log.info("生成自定义报表: {}", data);
        
        Map<String, Object> report = new HashMap<>();
        report.put("id", System.currentTimeMillis());
        report.put("name", data.get("name"));
        report.put("type", data.get("type"));
        report.put("status", "生成中");
        report.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Result.success(report);
    }

    /**
     * 导出报表
     */
    @GetMapping("/{reportId}/export")
    public void exportReport(
            @PathVariable Long reportId,
            @RequestParam String format,
            @RequestParam(required = false) String reportData,
            HttpServletResponse response) throws IOException {
        log.info("导出报表，ID: {}, 格式: {}", reportId, format);
        
        try {
            // 设置响应头
            String fileName = "report_" + reportId + "." + format.toLowerCase();
            response.setContentType(getContentType(format));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // 生成报表内容
            String content = generateReportContent(reportId, format, reportData);
            
            // 直接写入响应输出流
            response.getOutputStream().write(content.getBytes("UTF-8"));
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出报表失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("报表导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取内容类型
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "excel":
                return "application/vnd.ms-excel";
            case "csv":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * 生成报表内容
     */
    private String generateReportContent(Long reportId, String format, String reportData) {
        StringBuilder content = new StringBuilder();
        
        // 根据不同格式生成相应的内容
        switch (format.toLowerCase()) {
            case "pdf":
                return generatePdfContent(reportId, reportData);
            case "excel":
                return generateExcelContent(reportId, reportData);
            case "csv":
                return generateCsvContent(reportId, reportData);
            default:
                return generateDefaultContent(reportId, reportData);
        }
    }
    
    /**
     * 生成PDF格式内容
     */
    private String generatePdfContent(Long reportId, String reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全防御系统报表\n");
        content.append("==================\n\n");
        
        content.append("报表ID: ").append(reportId).append("\n");
        content.append("报表类型: PDF\n");
        content.append("生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("----------------------------------------\n\n");
        
        if (reportData != null && !reportData.isEmpty()) {
            content.append("报表配置信息:\n");
            content.append(parseReportDataForDisplay(reportData)).append("\n\n");
        }
        
        content.append("PDF报表说明:\n");
        content.append("- 本报表为PDF格式，适合打印和归档\n");
        content.append("- 包含完整的数据分析和图表\n");
        content.append("- 支持密码保护和数字签名\n\n");
        
        content.append("========================================\n");
        content.append("安全防御系统 - 专业安全报表解决方案\n");
        
        return content.toString();
    }
    
    /**
     * 生成Excel格式内容
     */
    private String generateExcelContent(Long reportId, String reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全防御系统报表\t\n");
        content.append("==================\t\n\n");
        
        content.append("报表ID:\t").append(reportId).append("\t\n");
        content.append("报表类型:\tExcel\t\n");
        content.append("生成时间:\t").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\t\n");
        content.append("----------------------------------------\t\n\n");
        
        if (reportData != null && !reportData.isEmpty()) {
            content.append("报表配置信息:\t\n");
            content.append(parseReportDataForDisplay(reportData).replace("\n", "\t\n")).append("\t\n\n");
        }
        
        content.append("Excel报表说明:\t\n");
        content.append("- 本报表为Excel格式，支持数据编辑\t\n");
        content.append("- 包含结构化数据表格\t\n");
        content.append("- 支持公式计算和图表制作\t\n\n");
        
        content.append("========================================\t\n");
        content.append("安全防御系统 - 专业安全报表解决方案\t\n");
        
        return content.toString();
    }
    
    /**
     * 生成CSV格式内容
     */
    private String generateCsvContent(Long reportId, String reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全防御系统报表,\n");
        
        content.append("报表ID:,").append(reportId).append("\n");
        content.append("报表类型:,CSV\n");
        content.append("生成时间:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        
        if (reportData != null && !reportData.isEmpty()) {
            content.append("\n报表配置信息:\n");
            content.append(parseCsvReportData(reportData)).append("\n");
        }
        
        content.append("\nCSV报表说明:\n");
        content.append("- 本报表为CSV格式，适合数据导入\n");
        content.append("- 包含纯文本数据\n");
        content.append("- 兼容各种数据处理工具\n");
        
        return content.toString();
    }
    
    /**
     * 生成默认格式内容
     */
    private String generateDefaultContent(Long reportId, String reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全防御系统报表\n");
        content.append("==================\n\n");
        
        content.append("报表ID: ").append(reportId).append("\n");
        content.append("生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("----------------------------------------\n\n");
        
        if (reportData != null && !reportData.isEmpty()) {
            content.append("报表配置信息:\n");
            content.append(parseReportDataForDisplay(reportData)).append("\n\n");
        }
        
        content.append("========================================\n");
        content.append("安全防御系统 - 专业安全报表解决方案\n");
        
        return content.toString();
    }
    
    /**
     * 解析报表数据用于显示
     */
    private String parseReportDataForDisplay(String reportData) {
        try {
            // URL解码
            String decodedData = java.net.URLDecoder.decode(reportData, "UTF-8");
            
            // 简单美化JSON显示
            return decodedData.replace(",", ",\n  ")
                             .replace("{", "{\n  ")
                             .replace("}", "\n}")
                             .replace(":\"", ": \"")
                             .replace("\"}", "\"\n}");
        } catch (Exception e) {
            return "无法解析报表数据: " + reportData;
        }
    }
    
    /**
     * 解析报表数据用于CSV显示
     */
    private String parseCsvReportData(String reportData) {
        try {
            // URL解码
            String decodedData = java.net.URLDecoder.decode(reportData, "UTF-8");
            
            // 简单处理JSON为CSV格式
            return decodedData.replace("{", "")
                             .replace("}", "")
                             .replace("\"", "")
                             .replace(",", ",\n");
        } catch (Exception e) {
            return "无法解析报表数据: " + reportData;
        }
    }
    
    /**
     * 获取报表列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取报表列表");
        
        List<Map<String, Object>> records = new ArrayList<>();
        String[] types = {"安全事件统计", "威胁情报分析", "合规性检查", "漏洞扫描", "性能分析"};
        
        for (int i = 0; i < pageSize; i++) {
            Map<String, Object> report = new HashMap<>();
            report.put("id", (page - 1) * pageSize + i + 1);
            report.put("name", types[i % types.length] + "-" + (i + 1));
            report.put("type", types[i % types.length]);
            report.put("status", i % 3 == 0 ? "已完成" : "生成中");
            report.put("createTime", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            report.put("creator", "admin");
            records.add(report);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", 28);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }
    
    /**
     * 导出事件统计数据 - 处理OPTIONS预检请求
     */
    @RequestMapping(value = "/event-statistics/export", method = RequestMethod.OPTIONS)
    public void exportEventStatisticsOptions(HttpServletResponse response) {
        log.info("收到OPTIONS预检请求 for /event-statistics/export");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Max-Age", "3600");
        log.info("OPTIONS预检请求处理完成");
    }
    
    /**
     * 导出事件统计数据
     */
    @PostMapping("/event-statistics/export")
    public void exportEventStatistics(
            @RequestBody Map<String, Object> reportData,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) throws IOException {
        log.info("开始导出事件统计数据，格式: {}", format);
        log.info("收到的报表数据: {}", reportData);
        
        try {
            // 设置响应头
            String fileName = "事件统计报表." + getExtension(format);
            // 对文件名进行URL编码以支持中文字符
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setContentType(getContentType(format));
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            log.info("设置响应头完成，文件名: {}", fileName);
            
            // 生成报表内容
            String content = generateEventStatisticsContent(reportData, format);
            log.info("生成的报表内容长度: {}", content.length());
            
            // 写入响应
            response.getWriter().write(content);
            response.getWriter().flush();
            log.info("报表导出成功");
        } catch (Exception e) {
            log.error("导出事件统计数据失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("报表导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成事件统计数据内容
     */
    private String generateEventStatisticsContent(Map<String, Object> reportData, String format) {
        StringBuilder content = new StringBuilder();
        
        switch (format.toLowerCase()) {
            case "pdf":
                return generateEventStatisticsPdfContent(reportData);
            case "excel":
                return generateEventStatisticsExcelContent(reportData);
            case "csv":
                return generateEventStatisticsCsvContent(reportData);
            default:
                return generateEventStatisticsDefaultContent(reportData);
        }
    }
    
    /**
     * 生成事件统计数据PDF内容
     */
    private String generateEventStatisticsPdfContent(Map<String, Object> reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全事件统计报表\n");
        content.append("==================\n\n");
        
        content.append("生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("----------------------------------------\n\n");
        
        // 添加统计数据
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) reportData.get("stats");
        if (stats != null) {
            content.append("统计概览:\n");
            content.append("  总事件数: ").append(stats.get("total")).append("\n");
            content.append("  高危事件: ").append(stats.get("high")).append("\n");
            content.append("  已处理: ").append(stats.get("handled")).append("\n");
            content.append("  待处理: ").append(stats.get("pending")).append("\n\n");
        }
        
        // 添加详细数据
        content.append("详细统计数据:\n");
        content.append("事件类型,总数,高危,中危,低危,已处理,待处理,处理率,平均处理时间\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailData = (List<Map<String, Object>>) reportData.get("detailData");
        if (detailData != null) {
            for (Map<String, Object> row : detailData) {
                content.append(row.get("type")).append(",")
                       .append(row.get("total")).append(",")
                       .append(row.get("high")).append(",")
                       .append(row.get("medium")).append(",")
                       .append(row.get("low")).append(",")
                       .append(row.get("handled")).append(",")
                       .append(row.get("pending")).append(",")
                       .append(row.get("rate")).append("%,")
                       .append(row.get("avgTime")).append("\n");
            }
        }
        
        content.append("\n========================================\n");
        content.append("安全防御系统 - 专业安全报表解决方案\n");
        
        return content.toString();
    }
    
    /**
     * 生成事件统计数据Excel内容
     */
    private String generateEventStatisticsExcelContent(Map<String, Object> reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全事件统计报表\t\n");
        content.append("==================\t\n\n");
        
        content.append("生成时间:\t").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\t\n");
        content.append("----------------------------------------\t\n\n");
        
        // 添加统计数据
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) reportData.get("stats");
        if (stats != null) {
            content.append("统计概览:\t\n");
            content.append("  总事件数:\t").append(stats.get("total")).append("\t\n");
            content.append("  高危事件:\t").append(stats.get("high")).append("\t\n");
            content.append("  已处理:\t").append(stats.get("handled")).append("\t\n");
            content.append("  待处理:\t").append(stats.get("pending")).append("\t\n\n");
        }
        
        // 添加详细数据
        content.append("详细统计数据:\t\n");
        content.append("事件类型\t总数\t高危\t中危\t低危\t已处理\t待处理\t处理率\t平均处理时间\t\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailData = (List<Map<String, Object>>) reportData.get("detailData");
        if (detailData != null) {
            for (Map<String, Object> row : detailData) {
                content.append(row.get("type")).append("\t")
                       .append(row.get("total")).append("\t")
                       .append(row.get("high")).append("\t")
                       .append(row.get("medium")).append("\t")
                       .append(row.get("low")).append("\t")
                       .append(row.get("handled")).append("\t")
                       .append(row.get("pending")).append("\t")
                       .append(row.get("rate")).append("%\t")
                       .append(row.get("avgTime")).append("\t\n");
            }
        }
        
        content.append("\n========================================\t\n");
        content.append("安全防御系统 - 专业安全报表解决方案\t\n");
        
        return content.toString();
    }
    
    /**
     * 生成事件统计数据CSV内容
     */
    private String generateEventStatisticsCsvContent(Map<String, Object> reportData) {
        StringBuilder content = new StringBuilder();
        
        content.append("安全事件统计报表,\n");
        content.append("生成时间:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("----------------------------------------,\n");
        
        // 添加统计数据
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) reportData.get("stats");
        if (stats != null) {
            content.append("统计概览,\n");
            content.append("总事件数,").append(stats.get("total")).append("\n");
            content.append("高危事件,").append(stats.get("high")).append("\n");
            content.append("已处理,").append(stats.get("handled")).append("\n");
            content.append("待处理,").append(stats.get("pending")).append("\n");
            content.append("\n");
        }
        
        // 添加详细数据
        content.append("详细统计数据,\n");
        content.append("事件类型,总数,高危,中危,低危,已处理,待处理,处理率,平均处理时间\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailData = (List<Map<String, Object>>) reportData.get("detailData");
        if (detailData != null) {
            for (Map<String, Object> row : detailData) {
                content.append("\"").append(row.get("type")).append("\",")
                       .append(row.get("total")).append(",")
                       .append(row.get("high")).append(",")
                       .append(row.get("medium")).append(",")
                       .append(row.get("low")).append(",")
                       .append(row.get("handled")).append(",")
                       .append(row.get("pending")).append(",")
                       .append(row.get("rate")).append("%,")
                       .append("\"").append(row.get("avgTime")).append("\"\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * 生成事件统计数据默认内容
     */
    private String generateEventStatisticsDefaultContent(Map<String, Object> reportData) {
        return generateEventStatisticsExcelContent(reportData);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getExtension(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "pdf";
            case "excel":
                return "xls";
            case "csv":
                return "csv";
            default:
                return "txt";
        }
    }
}
