package com.wzg.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class MemberSendCodeReq {

    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;
}
