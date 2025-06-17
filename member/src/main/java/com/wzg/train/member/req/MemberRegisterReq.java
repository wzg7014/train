package com.wzg.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MemberRegisterReq {

    @NotBlank(message = "【手机号】不能为空")
    private String mobile;


}
