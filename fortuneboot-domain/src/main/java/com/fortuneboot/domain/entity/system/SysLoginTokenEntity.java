package com.fortuneboot.domain.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录令牌持久化表，用于替代 Redis 存储登录用户信息
 *
 * @author fortuneboot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_token")
@Schema(title = "SysLoginTokenEntity对象", description = "登录令牌持久化表")
public class SysLoginTokenEntity extends Model<SysLoginTokenEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "令牌唯一标识（UUID）")
    @TableField("token_key")
    private String tokenKey;

    @Schema(description = "登录用户信息（JSON序列化）")
    @TableField("login_user_json")
    private String loginUserJson;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "登录IP")
    @TableField("login_ip")
    private String loginIp;

    @Schema(description = "过期时间")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}