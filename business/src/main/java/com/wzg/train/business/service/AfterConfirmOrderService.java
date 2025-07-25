package com.wzg.train.business.service;

import com.wzg.train.business.domain.*;
import com.wzg.train.business.enums.ConfirmOrderStatusEnum;
import com.wzg.train.business.feign.MemberFeign;
import com.wzg.train.business.mapper.ConfirmOrderMapper;
import com.wzg.train.business.mapper.DailyTrainSeatMapper;
import com.wzg.train.business.mapper.cust.DailyTrainTicketMapperCust;
import com.wzg.train.business.req.ConfirmOrderTicketReq;
import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.req.MemberTicketReq;
import com.wzg.train.common.resp.CommonResp;
import com.wzg.train.common.utils.SnowUtil;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private DailyTrainTicketMapperCust dailyTrainTicketMapperCust;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;


    /**
     *选中座位后事务处理
     * 座位表修改售卖情况sell
     * 余票详情表修改余票
     * 为会员增加购票记录
     * 更新确定订单为成功
     */
//    @Transactional
    @GlobalTransactional
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> finaSeatList, List<ConfirmOrderTicketReq> tickets,
                               ConfirmOrder confirmOrder) throws Exception {
        LOG.info("seata全局事务id:{}", RootContext.getXID());
        for (int j = 0; j < finaSeatList.size(); j++) {
            DailyTrainSeat dailyTrainSeat = finaSeatList.get(j);
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(dailyTrainSeat.getId());
            seatForUpdate.setSell(dailyTrainSeat.getSell());
            seatForUpdate.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatForUpdate);

//            计算这个站卖出去后，影响了哪些站的余票库存
//            参照2-3节如何保证不超卖、不少卖，还要能承受极高的并发10：30左右
//            影响的库存：本次选座之前没卖过票的，和本次购买的区间有交集的区间
//            假设10个站，本次买4~7站
//            原售：001000001
//            购买：000011100
//            新售：001011101
//            影响：XXX11111X
//            Integer startIndex = 4;
//            Integer endIndex = 7;
//            Integer minStartIndex = startIndex - 往前碰到的最后一个0;
//            Integer maxStartIndex = endIndex - 1;
//            Integer minEndIndex = startIndex + 1;
//            Integer maxEndIndex = endIndex + 往后碰到的最后一个0;
            Integer startIndex = dailyTrainTicket.getStartIndex();
            Integer endIndex = dailyTrainTicket.getEndIndex();
            char[] chars = seatForUpdate.getSell().toCharArray();
            Integer maxStartIndex = endIndex - 1;
            Integer minEndIndex = startIndex + 1;
            Integer minStartIndex = 0;
            for (int i = startIndex - 1; i >= 0; i--) {
                char aChar = chars[i];
                if (aChar == '1') {
                    minStartIndex = i + 1;
                    break;
                }
            }
            LOG.info("影响出发站区间：" + minStartIndex + "~" + maxStartIndex);

            Integer maxEndIndex = seatForUpdate.getSell().length();
            for (int i = endIndex; i < seatForUpdate.getSell().length(); i++) {
                char aChar = chars[i];
                if (aChar == '1') {
                    maxEndIndex = i;
                    break;
                }
            }

            LOG.info("影响到达站区间：" + minEndIndex + "~" + maxEndIndex);

            dailyTrainTicketMapperCust.updateCountBySell(
                    dailyTrainSeat.getDate(),
                    dailyTrainSeat.getTrainCode(),
                    dailyTrainSeat.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex
            );
            //调用会员服务接口，为会员增加一张车票
            MemberTicketReq memberTicketReq = new MemberTicketReq();
            memberTicketReq.setId(SnowUtil.getSnowflakeNextId());
            memberTicketReq.setMemberId(LoginMemberContext.getId());
            memberTicketReq.setPassengerId(tickets.get(j).getPassengerId());
            memberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
            memberTicketReq.setTrainDate(dailyTrainTicket.getDate());
            memberTicketReq.setTrainCode(dailyTrainSeat.getTrainCode());
            memberTicketReq.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
            memberTicketReq.setSeatRow(dailyTrainSeat.getRow());
            memberTicketReq.setSeatCol(dailyTrainSeat.getCol());
            memberTicketReq.setStartStation(dailyTrainTicket.getStart());
            memberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
            memberTicketReq.setEndStation(dailyTrainTicket.getEnd());
            memberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
            memberTicketReq.setSeatType(dailyTrainSeat.getSeatType());
            memberTicketReq.setCreateTime(dailyTrainTicket.getStartTime());
            memberTicketReq.setUpdateTime(dailyTrainTicket.getUpdateTime());
            CommonResp<Object> commonResp = memberFeign.save(memberTicketReq);
            LOG.info("调用member，返回：{}", commonResp);

            ConfirmOrder confirmOrderStatus = new ConfirmOrder();
            confirmOrderStatus.setId(confirmOrder.getId());
            confirmOrderStatus.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrderMapper.updateByPrimaryKeySelective(confirmOrderStatus);

            //模拟调用方出现异常
            if (1 == 1){
                throw new Exception("测试异常");
            }
        }
    }
}
