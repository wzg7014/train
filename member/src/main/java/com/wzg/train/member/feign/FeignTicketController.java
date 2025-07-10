package com.wzg.train.member.feign;

import com.wzg.train.common.req.MemberTicketReq;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/ticket")
public class FeignTicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody MemberTicketReq ticketReq) {
        ticketService.save(ticketReq);
        return new CommonResp<>();
    }

}

