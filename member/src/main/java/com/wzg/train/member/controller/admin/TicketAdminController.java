package com.wzg.train.member.controller.admin;

import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.member.req.TicketQueryReq;
import com.wzg.train.member.req.TicketSaveReq;
import com.wzg.train.member.resp.TicketQueryResp;
import com.wzg.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Resource
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req) {
        PageResp<TicketQueryResp> list = ticketService.queryList(req);
        return new CommonResp<>(list);
    }
}
