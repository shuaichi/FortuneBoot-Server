package com.fortuneboot.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 用户注册成功事件
 * @author zhangchi118
 * @date 2026/3/13 13:26
 **/
@Getter
@ToString
@AllArgsConstructor
public class UserRegisteredEvent {

    /**
     * 新注册用户的 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

}