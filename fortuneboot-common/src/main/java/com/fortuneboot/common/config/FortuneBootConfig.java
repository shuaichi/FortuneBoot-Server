package com.fortuneboot.common.config;

import com.fortuneboot.common.constant.Constants;
import java.io.File;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * TODO 移走  不合适放在这里common包底下
 * @author valarchie
 */
@Component
@ConfigurationProperties(prefix = "fortuneboot")
@Data
public class FortuneBootConfig {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 实例演示开关
     */
    private static boolean demoEnabled;

    /**
     * 上传路径
     */
    private static String fileBaseDir;

    /**
     * 获取地址开关
     */
    private static boolean addressEnabled;

    /**
     * 验证码类型
     */
    private static String captchaType;

    /**
     * rsa private key  静态属性的注入！！ set方法一定不能是static 方法
     */
    private static String rsaPrivateKey;

    /**
     * rsa public key
     */
    private static String rsaPublicKey;

    private static String apiPrefix;

    public static String getFileBaseDir() {
        return fileBaseDir;
    }

    public void setFileBaseDir(String fileBaseDir) {
        FortuneBootConfig.fileBaseDir = fileBaseDir  + File.separator + Constants.RESOURCE_PREFIX;
    }

    public static String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiDocsPathPrefix) {
        FortuneBootConfig.apiPrefix = apiDocsPathPrefix;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        FortuneBootConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        FortuneBootConfig.captchaType = captchaType;
    }

    public static String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        FortuneBootConfig.rsaPrivateKey = rsaPrivateKey;
    }

    public static String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        FortuneBootConfig.rsaPublicKey = rsaPublicKey;
    }

    public static boolean isDemoEnabled() {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled) {
        FortuneBootConfig.demoEnabled = demoEnabled;
    }

}
