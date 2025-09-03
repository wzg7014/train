package com.wzg.train.business.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.wzg.train.business.domain.ConfirmOrder;
import com.wzg.train.business.enums.ConfirmOrderStatusEnum;
import com.wzg.train.business.enums.RedisKeyPreEnum;
import com.wzg.train.business.mapper.ConfirmOrderMapper;
import com.wzg.train.business.req.ConfirmOrderDoReq;
import com.wzg.train.business.req.ConfirmOrderTicketReq;
import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.exception.BusinessExceptionEnum;
import com.wzg.train.common.utils.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Resource
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SkTokenService skTokenService;

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public void beforeDoConfirm(ConfirmOrderDoReq req) {
        //校验令牌余量
        boolean validSkToken = skTokenService.validSkToken(req.getDate(),
                req.getTrainCode(),
                LoginMemberContext.getId());
        if(validSkToken) {
            LOG.info("令牌校验通过");
        }else {
            LOG.info("令牌校验不通过");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
        }

        // 获取车次锁
        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if (setIfAbsent) {
            LOG.info("恭喜，抢到锁了");
        } else {
            LOG.info("很遗憾，没有抢到锁");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
        }
        // 发送MQ排队购票
        LOG.info("准备发送MQ，等待出票");
    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}
