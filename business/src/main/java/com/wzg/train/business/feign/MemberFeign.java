package com.wzg.train.business.feign;


import com.wzg.train.common.req.MemberTicketReq;
import com.wzg.train.common.resp.CommonResp;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "member",url = "http://127.0.0.1:8001/member")
public interface MemberFeign {

    @PostMapping("/feign/ticket/save")
    public CommonResp<Object> save(@Valid @RequestBody MemberTicketReq ticketReq);
}
