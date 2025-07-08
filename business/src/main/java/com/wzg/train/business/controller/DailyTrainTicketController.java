package com.wzg.train.business.controller;

import com.wzg.train.business.req.DailyTrainTicketQueryReq;
import com.wzg.train.business.req.DailyTrainTicketSaveReq;
import com.wzg.train.business.resp.DailyTrainTicketQueryResp;
import com.wzg.train.business.service.DailyTrainTicketService;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/daily-train-ticket")
public class DailyTrainTicketController {

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> list = dailyTrainTicketService.queryList(req);
        return new CommonResp<>(list);
    }

}
