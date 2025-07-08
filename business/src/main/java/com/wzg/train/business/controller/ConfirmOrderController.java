package com.wzg.train.business.controller;

import com.wzg.train.business.req.ConfirmOrderDoReq;
import com.wzg.train.business.req.ConfirmOrderQueryReq;
import com.wzg.train.business.resp.ConfirmOrderQueryResp;
import com.wzg.train.business.service.ConfirmOrderService;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

}
