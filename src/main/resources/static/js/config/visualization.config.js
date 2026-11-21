// 安全态势可视化配置文件
const visualizationConfig = {
    // 颜色配置
    colors: {
        // 态势等级颜色
        situationLevels: {
            CRITICAL: '#dc3545',  // 危急 - 红色
            HIGH: '#fd7e14',      // 高风险 - 橙色
            MEDIUM: '#ffc107',    // 中风险 - 黄色
            LOW: '#28a745',       // 低风险 - 绿色
            SAFE: '#17a2b8'       // 安全 - 青色
        },
        // 威胁类型颜色
        threatTypes: {
            Ransomware: '#dc3545',
            Phishing: '#fd7e14',
            Malware: '#ffc107',
            SQLi: '#28a745',
            XSS: '#17a2b8',
            BruteForce: '#6610f2',
            DDoS: '#6f42c1',
            Unknown: '#6c757d'
        },
        // 图表通用颜色
        chart: [
            '#1f77b4', '#ff7f0e', '#2ca02c', '#d62728',
            '#9467bd', '#8c564b', '#e377c2', '#7f7f7f',
            '#bcbd22', '#17becf'
        ]
    },
    
    // 态势仪表盘配置
    dashboard: {
        refreshInterval: 30000,  // 30秒刷新一次
        widgets: [
            {
                id: 'situation-overview',
                title: '安全态势总览',
                size: 'large',
                position: { row: 1, col: 1, rowspan: 1, colspan: 2 }
            },
            {
                id: 'asset-risk-distribution',
                title: '资产风险分布',
                size: 'medium',
                position: { row: 2, col: 1, rowspan: 1, colspan: 1 }
            },
            {
                id: 'threat-intelligence',
                title: '威胁情报概览',
                size: 'medium',
                position: { row: 2, col: 2, rowspan: 1, colspan: 1 }
            },
            {
                id: 'situation-trend',
                title: '态势趋势分析',
                size: 'large',
                position: { row: 3, col: 1, rowspan: 1, colspan: 2 }
            },
            {
                id: 'dimension-analysis',
                title: '安全维度分析',
                size: 'medium',
                position: { row: 1, col: 3, rowspan: 1, colspan: 1 }
            },
            {
                id: 'key-metrics',
                title: '关键指标',
                size: 'medium',
                position: { row: 2, col: 3, rowspan: 1, colspan: 1 }
            },
            {
                id: 'asset-risk-heatmap',
                title: '资产风险热力图',
                size: 'large',
                position: { row: 3, col: 3, rowspan: 1, colspan: 1 }
            }
        ]
    },
    
    // 图表配置
    charts: {
        // 态势指数仪表盘配置
        gauge: {
            min: 0,
            max: 100,
            sectors: [
                { min: 0, max: 20, color: '#17a2b8', label: '安全' },
                { min: 20, max: 40, color: '#28a745', label: '低风险' },
                { min: 40, max: 60, color: '#ffc107', label: '中风险' },
                { min: 60, max: 80, color: '#fd7e14', label: '高风险' },
                { min: 80, max: 100, color: '#dc3545', label: '危急' }
            ],
            animation: {
                duration: 1500,
                easing: 'easeOutQuart'
            }
        },
        
        // 趋势图配置
        trend: {
            timeFormat: 'YYYY-MM-DD HH:mm',
            interval: 'auto',
            animation: true,
            tooltip: {
                enabled: true,
                formatter: 'function'
            },
            legend: {
                enabled: true,
                position: 'top'
            }
        },
        
        // 饼图配置
        pie: {
            animation: true,
            tooltip: {
                enabled: true,
                formatter: 'function'
            },
            legend: {
                enabled: true,
                position: 'right'
            }
        },
        
        // 柱状图配置
        bar: {
            animation: true,
            tooltip: {
                enabled: true,
                formatter: 'function'
            },
            legend: {
                enabled: true,
                position: 'top'
            },
            axis: {
                x: {
                    labelRotation: 45
                },
                y: {
                    beginAtZero: true
                }
            }
        },
        
        // 热力图配置
        heatmap: {
            radius: 15,
            intensity: 0.8,
            animation: true,
            tooltip: {
                enabled: true,
                formatter: 'function'
            }
        }
    },
    
    // 表格配置
    tables: {
        pageSize: 10,
        pagination: true,
        sorting: true,
        filter: true,
        exportable: true
    },
    
    // 告警配置
    alerts: {
        autoHide: 5000,
        position: 'top-right',
        maximumVisible: 5
    },
    
    // 响应式布局配置
    responsive: {
        breakpoints: {
            xs: 576,
            sm: 768,
            md: 992,
            lg: 1200,
            xl: 1600
        },
        layouts: {
            xs: 'single-column',
            sm: 'two-column',
            md: 'three-column',
            lg: 'three-column',
            xl: 'four-column'
        }
    }
};

// 导出配置
if (typeof module !== 'undefined' && module.exports) {
    module.exports = visualizationConfig;
} else if (typeof define === 'function' && define.amd) {
    define([], function() {
        return visualizationConfig;
    });
} else {
    window.visualizationConfig = visualizationConfig;
}