package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzg.train.business.domain.*;
import com.wzg.train.business.enums.ConfirmOrderStatusEnum;
import com.wzg.train.business.enums.SeatColEnum;
import com.wzg.train.business.enums.SeatTypeEnum;
import com.wzg.train.business.req.ConfirmOrderTicketReq;
import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.exception.BusinessExceptionEnum;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.business.mapper.ConfirmOrderMapper;
import com.wzg.train.business.req.ConfirmOrderQueryReq;
import com.wzg.train.business.req.ConfirmOrderDoReq;
import com.wzg.train.business.resp.ConfirmOrderQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.wzg.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR;
import static com.wzg.train.common.exception.BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

   @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req){
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrders = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrders);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrders, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    public void doConfirm(ConfirmOrderDoReq req){
        // 省略业务数据校验，如：车次是否存在，余票是否存在，车次是否在有效期内，tickets条数 > 0
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
        confirmOrder.setMemberId(LoginMemberContext.getId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);

        // 查出余票记录，需要得到真实的库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
        LOG.info("查出余票记录:{}", dailyTrainTicket);
        // 扣减余票数量，并判断余票是否足够
        reduceTickets(req, dailyTrainTicket);

        //计算相对第一个座位的偏移值
        //比如选择的是C1，D2，则偏移值为[0, 5]
        //比如选择的是A1,B1,C1，则偏移值为：[0,1,2]
        //判断是否有选座
        ConfirmOrderTicketReq ticket0 = tickets.get(0);
        if (StrUtil.isNotBlank(ticket0.getSeat())){
            LOG.info("本次购票有选座");
            //查处本次选座的座位类型有哪些列，用于计算所选座位与第一个座位的偏移值
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticket0.getSeatTypeCode());
            LOG.info("本次选座的座位类型有：{}", colEnumList);

            //组成和前端两排选座一样的列表，用于作参照的座位列表，例：referSeatList = {A1,C1,D1,F1,A2,C2,D2,F2}
            ArrayList<String> referSeatList = new ArrayList<>();
            for (int i = 1; i < 3; i++) {
                for (SeatColEnum seatColEnum : colEnumList) {
                    referSeatList.add(seatColEnum.getCode() + i);
                }
            }
            LOG.info("用于做参照的两排座位：{}", referSeatList);

            List<Integer> offsetList = new ArrayList<>();
            //绝对偏移值，即：在参照座位列表的位置
            List<Integer> aboluteOffsetList = new ArrayList<>();
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                int index = referSeatList.indexOf(ticketReq.getSeat());
                aboluteOffsetList.add(index);
            }
            LOG.info("计算得到所有座位的绝对偏移值：{}", aboluteOffsetList);
            for (Integer index : aboluteOffsetList) {
                int offset = index - aboluteOffsetList.get(0);
                offsetList.add(offset);
            }
            LOG.info("计算得到所有座位的相对第一个座位的相对偏移值：{}", offsetList);
            getSeat(date, trainCode,
                    ticket0.getSeatTypeCode(),
                    ticket0.getSeat().split("")[0],//从A1得到A
                    offsetList);
        }else {
            LOG.info("本次购票没有选座");
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                getSeat(date, trainCode,
                        ticket0.getSeatTypeCode(),
                        null,//从A1得到A
                        null);
            }
        }

        // 选座

            //一个车厢一个车厢的获取座位数据

            //挑选符合条件的座位，如果这个车厢不满足，则进入下一个车厢（多个选座应该在同一个车厢）

        // 选中座位后事务处理

            // 座位表修改售卖情况sell
            // 余票详情表修改余票
            // 为会员增加购票记录
            //更新确定订单为成功
    }

    private void getSeat(Date date, String trainCode, String seatType,
                         String colum, List<Integer> offsetList){
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(
                date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢：", carriageList.size());

        //一个车厢一个车厢的获取座位数据
        for (DailyTrainCarriage dailyTrainCarriage : carriageList) {
            LOG.info("开始从车厢{}选座", dailyTrainCarriage.getIndex());
            List<DailyTrainSeat> seatList = dailyTrainSeatService.
                    selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}", dailyTrainCarriage.getIndex(), seatList.size());

        }
    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticketReq : req.getTickets()) {
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum){
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }
}
