package com.fortuneboot.domain.dto.monitor;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.fortuneboot.common.constant.Constants;

import java.lang.management.ManagementFactory;

import lombok.Data;

/**
 * JVM相关信息
 *
 * @author ruoyi
 */
@Data
public class JvmInfo {

    /**
     * 当前JVM占用的内存总数(M)
     */
    private double total;

    /**
     * JVM最大可用内存总数(M)
     */
    private double max;

    /**
     * JVM空闲内存(M)
     */
    private double free;

    /**
     * JDK版本
     */
    private String version;

    /**
     * JDK路径
     */
    private String home;

    public double getTotal() {
        return NumberUtil.div(total, Constants.MB, 2);
    }

    public double getMax() {
        return NumberUtil.div(max, Constants.MB, 2);
    }

    public double getFree() {
        return NumberUtil.div(free, Constants.MB, 2);
    }

    public double getUsed() {
        return NumberUtil.div(total - free, Constants.MB, 2);
    }

    public double getUsage() {
        return total == 0 ? 0 : NumberUtil.div((total - free) * 100, total, 2);
    }

    /**
     * 获取JDK名称
     */
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }

    /**
     * JDK启动时间
     */
    public String getStartTime() {
        return DateUtil.format(DateUtil.date(ManagementFactory.getRuntimeMXBean().getStartTime()),
                DatePattern.NORM_DATETIME_PATTERN);
    }

    /**
     * JDK运行时间
     */
    public String getRunTime() {
        return DateUtil.formatBetween(DateUtil.date(ManagementFactory.getRuntimeMXBean().getStartTime()),
                DateUtil.date());
    }

    /**
     * 运行参数
     */
    public String getInputArgs() {
        String args = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
        // Native 镜像下没有传统的 JVM 启动参数
        return "[]".equals(args) ? "[Native 机器码执行]" : args;
    }

    /**
     * JDK路径 / Native 引擎
     */
    public String getHome() {
        // Native 镜像下脱离 JRE 运行，home 为 null 是正常现象
        return home != null ? home : "Substrate VM (Native Image 独立运行)";
    }
}
