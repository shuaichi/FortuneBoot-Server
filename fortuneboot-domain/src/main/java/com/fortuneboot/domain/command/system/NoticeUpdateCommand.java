package com.fortuneboot.domain.command.system;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoticeUpdateCommand extends NoticeAddCommand {

    @NotNull
    @Positive
    protected Long noticeId;

}
