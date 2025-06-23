package com.wzg.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MemberLoginReq {

    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;

    @NotBlank(message = "【短信验证码】不能为空")
    private String code;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MemberLoginReq{");
        sb.append("mobile='").append(mobile).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
