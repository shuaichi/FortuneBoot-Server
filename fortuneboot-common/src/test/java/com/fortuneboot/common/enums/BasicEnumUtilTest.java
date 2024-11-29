package com.fortuneboot.common.enums;


import com.fortuneboot.common.enums.common.YesOrNoEnum;
import org.junit.Assert;
import org.junit.Test;

public class BasicEnumUtilTest {

    @Test
    public void testFromValue() {

        YesOrNoEnum yes = BasicEnumUtil.fromValue(YesOrNoEnum.class, 1);
        YesOrNoEnum no = BasicEnumUtil.fromValue(YesOrNoEnum.class, 0);

        Assert.assertEquals(yes.getDescription(), "是");
        Assert.assertEquals(no.getDescription(), "否");

    }
}
