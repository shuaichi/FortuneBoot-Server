package com.fortuneboot.domain.command.system;

import com.fortuneboot.common.annotation.ExcelColumn;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class AddRoleCommand {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    /**
     * 角色权限
     */
    @ExcelColumn(name = "角色权限")
    @NotBlank(message = "权限字符不能为空")
    @Size(max = 100, message = "权限字符长度不能超过100个字符")
    private String roleKey;

    /**
     * 角色排序
     */
    @ExcelColumn(name = "角色排序")
    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    @ExcelColumn(name = "数据范围")
    private String dataScope;

    @PositiveOrZero
    private String status;

    @NotNull
    private List<Long> menuIds;

    @NotNull
    private Boolean allowRegister;

    private String remark;

}
