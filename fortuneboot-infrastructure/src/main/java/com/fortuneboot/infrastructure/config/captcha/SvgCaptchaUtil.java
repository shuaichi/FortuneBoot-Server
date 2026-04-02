package com.fortuneboot.infrastructure.config.captcha;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;

import java.nio.charset.StandardCharsets;

/**
 * 纯 SVG 方式生成验证码图片，不依赖 AWT，兼容 GraalVM native image。
 *
 * @author fortuneboot
 */
public final class SvgCaptchaUtil {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;

    private static final String[] COLORS = {
            "#3498db", "#e74c3c", "#2ecc71", "#9b59b6",
            "#e67e22", "#1abc9c", "#34495e", "#d35400"
    };

    private SvgCaptchaUtil() {
    }

    /**
     * 生成数学验证码
     *
     * @return CaptchaResult 包含 SVG Base64 和答案
     */
    public static CaptchaResult createMathCaptcha() {
        CaptchaMathTextCreator creator = new CaptchaMathTextCreator();
        String text = creator.getText();
        String[] parts = text.split("@");
        String expression = parts[0];
        String answer = parts[1];

        String svg = renderSvg(expression);
        String base64 = toDataUri(svg);
        return new CaptchaResult(base64, answer);
    }

    /**
     * 生成字符验证码
     *
     * @param length 字符长度
     * @return CaptchaResult 包含 SVG Base64 和答案
     */
    public static CaptchaResult createCharCaptcha(int length) {
        String chars = RandomUtil.randomString("abcdefghjkmnpqrstuvwxyz23456789", length);
        String svg = renderSvg(chars);
        String base64 = toDataUri(svg);
        return new CaptchaResult(base64, chars);
    }

    /**
     * 将文本渲染为 SVG 字符串
     */
    private static String renderSvg(String text) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='")
                .append(WIDTH).append("' height='").append(HEIGHT).append("'>");

        // 背景
        svg.append("<rect width='100%' height='100%' fill='#f0f0f0'/>");

        // 干扰线
        for (int i = 0; i < 5; i++) {
            svg.append("<line x1='").append(RandomUtil.randomInt(WIDTH))
                    .append("' y1='").append(RandomUtil.randomInt(HEIGHT))
                    .append("' x2='").append(RandomUtil.randomInt(WIDTH))
                    .append("' y2='").append(RandomUtil.randomInt(HEIGHT))
                    .append("' stroke='").append(randomColor())
                    .append("' stroke-width='1' opacity='0.5'/>");
        }

        // 干扰圆点
        for (int i = 0; i < 10; i++) {
            svg.append("<circle cx='").append(RandomUtil.randomInt(WIDTH))
                    .append("' cy='").append(RandomUtil.randomInt(HEIGHT))
                    .append("' r='").append(RandomUtil.randomInt(1, 4))
                    .append("' fill='").append(randomColor())
                    .append("' opacity='0.4'/>");
        }

        // 绘制文字
        int charCount = text.length();
        int startX = 10;
        int spacing = (WIDTH - 20) / Math.max(charCount, 1);

        for (int i = 0; i < charCount; i++) {
            int x = startX + i * spacing;
            int y = 35 + RandomUtil.randomInt(-8, 8);
            int rotation = RandomUtil.randomInt(-20, 20);
            int fontSize = RandomUtil.randomInt(24, 34);

            svg.append("<text x='").append(x)
                    .append("' y='").append(y)
                    .append("' font-size='").append(fontSize)
                    .append("' font-family='Arial, sans-serif'")
                    .append(" font-weight='bold'")
                    .append(" fill='").append(randomColor()).append("'")
                    .append(" transform='rotate(").append(rotation)
                    .append(" ").append(x).append(" ").append(y).append(")'")
                    .append(">")
                    .append(escapeXml(String.valueOf(text.charAt(i))))
                    .append("</text>");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private static String randomColor() {
        return COLORS[RandomUtil.randomInt(COLORS.length)];
    }

    /**
     * SVG 内容转为 data URI（含 base64 编码），前端可直接作为 img src 使用
     */
    private static String toDataUri(String svg) {
        return "data:image/svg+xml;base64," + Base64.encode(svg.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&apos;")
                .replace("\"", "&quot;");
    }

    /**
     * 验证码结果
     */
    public static class CaptchaResult {
        private final String imageBase64;
        private final String answer;

        public CaptchaResult(String imageBase64, String answer) {
            this.imageBase64 = imageBase64;
            this.answer = answer;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public String getAnswer() {
            return answer;
        }
    }

}