package com.fortuneboot.domain.command.user;

import com.fortuneboot.common.annotation.ExcelColumn;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class AddUserCommand {

    @ExcelColumn(name = "用户名")
    private String username;

    @ExcelColumn(name = "昵称")
    private String nickname;

    @ExcelColumn(name = "邮件")
    private String email;

    @ExcelColumn(name = "电话号码")
    private String phoneNumber;

    @ExcelColumn(name = "性别")
    private Integer sex;

    @ExcelColumn(name = "头像")
    private String avatar;

    @ExcelColumn(name = "密码")
    private String password;

    @ExcelColumn(name = "状态")
    private Integer status;

    @ExcelColumn(name = "角色ID")
    private Long roleId;

    @ExcelColumn(name = "备注")
    private String remark;

    /**
     * 来源
     */
    private Integer source;
}
