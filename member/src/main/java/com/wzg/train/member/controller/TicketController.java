package com.wzg.train.member.controller;


import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.member.req.TicketQueryReq;
import com.wzg.train.member.resp.TicketQueryResp;
import com.wzg.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private static final Logger LOG = LoggerFactory.getLogger(TicketController.class);
    @Autowired
    private TicketService ticketService;




    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> query(@Valid TicketQueryReq req) {
        CommonResp<PageResp<TicketQueryResp>> commonResp = new CommonResp<>();
        req.setMemberId(LoginMemberContext.getId());
        PageResp<TicketQueryResp> pageResp = ticketService.queryList(req);
        commonResp.setContent(pageResp);
        return commonResp;
    }


}

