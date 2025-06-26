package com.wzg.train.member.controller;

import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.member.req.PassengerQueryReq;
import com.wzg.train.member.req.PassengerSaveReq;
import com.wzg.train.member.resp.PassengerQuaryResp;
import com.wzg.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq req) {
        passengerService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<List<PassengerQuaryResp>> queryList(@Valid PassengerQueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        List<PassengerQuaryResp> list = passengerService.queryList(req);
        return new CommonResp<>(list);
    }


}
