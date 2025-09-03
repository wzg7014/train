package com.wzg.train.business.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.wzg.train.business.domain.ConfirmOrder;
import com.wzg.train.business.dto.ConfirmOrderDto;
import com.wzg.train.business.enums.ConfirmOrderStatusEnum;
import com.wzg.train.business.enums.RocketMQTopicEnum;
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

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Autowired
    private SkTokenService skTokenService;

    @Resource
    public RocketMQTemplate rocketMQTemplate;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public long beforeDoConfirm(ConfirmOrderDoReq req) {
        req.setMemberId(LoginMemberContext.getId());
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

        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        // 保存确定订单表，状态初始
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setMemberId(req.getMemberId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);

//        // 获取车次锁
//        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
//        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
//        if (setIfAbsent) {
//            LOG.info("恭喜，抢到锁了");
//        } else {
//            LOG.info("很遗憾，没有抢到锁");
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
//        }

        // 发送MQ排队购票
        ConfirmOrderDto confirmOrderDto = new ConfirmOrderDto();
        confirmOrderDto.setDate(req.getDate());
        confirmOrderDto.setTrainCode(req.getTrainCode());
        confirmOrderDto.setLogId(MDC.get("LOG_ID"));
        String reqJson = JSON.toJSONString(confirmOrderDto);
        LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
        rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
        LOG.info("排队购票，发送mq结束");
        return confirmOrder.getId();
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
