package com.wzg.train.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wzg.train.business.req.ConfirmOrderDoReq;
import com.wzg.train.business.req.ConfirmOrderQueryReq;
import com.wzg.train.business.resp.ConfirmOrderQueryResp;
import com.wzg.train.business.service.ConfirmOrderService;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.exception.BusinessExceptionEnum;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderController.class);

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @SentinelResource(value = "confirmOrderDo", blockHandler = "doConfirmBlock")
    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        //图形验证码
        String imageCodeToken = req.getImageCodeToken();
        String imageCode = req.getImageCode();
        String imageCodeRedis = redisTemplate.opsForValue().get(imageCodeToken).toString();
        LOG.info("从redis中获取到的验证码:{}",imageCodeRedis);
        if(ObjectUtils.isEmpty(imageCodeRedis)) {
            return  new CommonResp<>(false,"验证码已过期",null);
        }

        // 验证码校验，大小写忽略，提升体验，比如Oo，Ww容易混淆
        if (!imageCodeRedis.equalsIgnoreCase(imageCode)) {
            return new CommonResp<>(false,"验证码错误",null);
        }else{
            //y验证码通过后，移除验证码
            redisTemplate.delete(imageCodeToken);
        }

        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

    public CommonResp<Object> doConfirmBlock(ConfirmOrderDoReq confirmOrderDoReq, BlockException e) {
        LOG.info("当前购票被限流:{}",e);
        CommonResp<Object> commonResp = new CommonResp<>();
        commonResp.setSuccess(false);
        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return commonResp;
    }
}
