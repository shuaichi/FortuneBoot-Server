package com.fortuneboot.domain.entity.system;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 如果Entity的字段和复杂查询不匹配时   自定义类来接收
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchUserDO extends SysUserEntity {
}
