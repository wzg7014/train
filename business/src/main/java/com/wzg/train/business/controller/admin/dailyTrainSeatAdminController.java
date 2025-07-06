package com.wzg.train.business.controller.admin;

import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.business.req.dailyTrainSeatQueryReq;
import com.wzg.train.business.req.dailyTrainSeatSaveReq;
import com.wzg.train.business.resp.dailyTrainSeatQueryResp;
import com.wzg.train.business.service.dailyTrainSeatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-seat")
public class dailyTrainSeatAdminController {

    @Resource
    private dailyTrainSeatService dailyTrainSeatService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody dailyTrainSeatSaveReq req) {
        dailyTrainSeatService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<dailyTrainSeatQueryResp>> queryList(@Valid dailyTrainSeatQueryReq req) {
        PageResp<dailyTrainSeatQueryResp> list = dailyTrainSeatService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        dailyTrainSeatService.delete(id);
        return new CommonResp<>();
    }
}
