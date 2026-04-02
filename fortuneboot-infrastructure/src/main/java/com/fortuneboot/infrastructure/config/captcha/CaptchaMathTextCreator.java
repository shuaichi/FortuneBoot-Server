package com.fortuneboot.infrastructure.config.captcha;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * verification code of math Generator
 * <p>
 * 不再依赖 kaptcha 的 DefaultTextCreator，纯文本生成，兼容 GraalVM native image。
 *
 * @author valarchie
 */
public class CaptchaMathTextCreator {

    public String getText() {
        int x = RandomUtil.randomInt(13);
        int y = RandomUtil.randomInt(13);
        Operand randomOperand = EnumUtil.getEnumAt(Operand.class, RandomUtil.randomInt(4));

        StringBuilder mathExpression = new StringBuilder();
        int result = randomOperand.generateMathExpression(x, y, mathExpression);

        mathExpression.append("=?@").append(result);
        return mathExpression.toString();
    }

    enum Operand {
        /**
         * 加减乘除操作  用来生成验证码的图片表达式
         */
        ADD {
            @Override
            public int generateMathExpression(int x, int y, StringBuilder expression) {
                expression.append(x);
                expression.append("+");
                expression.append(y);
                return x + y;
            }
        },
        MINUS {
            @Override
            public int generateMathExpression(int x, int y, StringBuilder expression) {
                expression.append(Math.max(x, y));
                expression.append("-");
                expression.append(Math.min(x, y));
                return Math.abs(x - y);
            }
        },
        MULTIPLE {
            @Override
            public int generateMathExpression(int x, int y, StringBuilder expression) {
                expression.append(x);
                expression.append("*");
                expression.append(y);
                return x * y;
            }
        },
        DIVIDE {
            @Override
            public int generateMathExpression(int x, int y, StringBuilder expression) {
                // Judge whether an integer can be divided
                if (x != 0 && y % x == 0) {
                    expression.append(y);
                    expression.append("/");
                    expression.append(x);
                    return y / x;
                } else {
                    // use add addition instead
                    return Operand.ADD.generateMathExpression(x, y, expression);
                }
            }
        };
        public abstract int generateMathExpression(int x, int y, StringBuilder expression);
    }
}