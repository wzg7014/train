package com.wzg.train.business.controller;

import com.wzg.train.business.req.TrainQueryReq;
import com.wzg.train.business.req.TrainSaveReq;
import com.wzg.train.business.resp.TrainQueryResp;
import com.wzg.train.business.service.TrainSeatService;
import com.wzg.train.business.service.TrainService;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/train")
public class TrainController {

    @Resource
    private TrainService trainService;


    @Resource
    private TrainSeatService trainSeatService;

    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryResp>> queryList() {
        List<TrainQueryResp> list = trainService.queryAll();
        return new CommonResp<>(list);
    }
}
