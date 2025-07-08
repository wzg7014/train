package com.wzg.train.business.controller;

import com.wzg.train.business.req.StationQueryReq;
import com.wzg.train.business.req.StationSaveReq;
import com.wzg.train.business.resp.StationQueryResp;
import com.wzg.train.business.service.StationService;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private StationService stationService;

    @GetMapping("/query-all")
    public CommonResp<List<StationQueryResp>> queryList() {
        List<StationQueryResp> list = stationService.queryAll();
        return new CommonResp<>(list);
    }
}
