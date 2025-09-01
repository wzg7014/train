package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

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
        String lockKey = req.getDate() + "-" + req.getTrainCode();
//        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
//        if (setIfAbsent) {
//            LOG.info("恭喜，抢到锁了");
//        } else {
//            LOG.info("很遗憾，没有抢到锁");
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
//        }

        RLock lock = null;

        try {
            // 使用redisson，自带看门狗
            lock = redissonClient.getLock(lockKey);

            /**
             waitTime – the maximum time to acquire the lock 等待获取锁时间(最大尝试获得锁的时间)，超时返回false
             leaseTime – lease time 锁时长，即n秒后自动释放锁
             time unit – time unit 时间单位
             */
            // boolean tryLock = lock.tryLock(30, 10, TimeUnit.SECONDS); // 不带看门狗
            boolean tryLock = lock.tryLock(0, TimeUnit.SECONDS); // 带看门狗
            if (tryLock) {
                LOG.info("恭喜，抢到锁了！");
                // for (int i = 0; i < 30; i++) {
                //     Long expire = redisTemplate.opsForValue().getOperations().getExpire(lockKey);
                //     LOG.info("锁过期时间还有：{}", expire);
                //     Thread.sleep(1000);
                // }
            } else {
                // 只是没抢到锁，并不知道票抢完了没，所以提示稍候再试
                LOG.info("很遗憾，没抢到锁");
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
            }

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

            //最终的选座结果
            List<DailyTrainSeat> finaSeatList = new ArrayList<>();
            //计算相对第一个座位的偏移值
            //比如选择的是C1，D2，则偏移值为[0, 5]
            //比如选择的是A1,B1,C1，则偏移值为：[0,1,2]
            //判断是否有选座
            ConfirmOrderTicketReq ticket0 = tickets.get(0);
            if (StrUtil.isNotBlank(ticket0.getSeat())) {
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
                getSeat(finaSeatList,
                        date, trainCode,
                        ticket0.getSeatTypeCode(),
                        ticket0.getSeat().split("")[0],//从A1得到A
                        offsetList,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex());
            } else {
                LOG.info("本次购票没有选座");
                for (ConfirmOrderTicketReq ticketReq : tickets) {
                    getSeat(finaSeatList,
                            date, trainCode,
                            ticket0.getSeatTypeCode(),
                            null,//从A1得到A
                            null,
                            dailyTrainTicket.getStartIndex(),
                            dailyTrainTicket.getEndIndex());
                }
            }

            LOG.info("最终选座结果：{}", finaSeatList);

            // 选中座位后事务处理

            // 座位表修改售卖情况sell
            // 余票详情表修改余票
            // 为会员增加购票记录
            // 更新确定订单为成功
            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finaSeatList, tickets, confirmOrder);
            } catch (Exception e) {
                LOG.info("保存购票信息失败", e);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
            }
            // LOG.info("购票流程结束，释放锁！lockKey：{}", lockKey);
            // redisTemplate.delete(lockKey);
        } catch (InterruptedException e){
            LOG.info("购票异常", e);
        }finally {
            // 删除分布式锁
            LOG.info("购票流程结束，释放锁！");
            if (null != lock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 调座位，如果有选座，则一次性挑完，如果无选座，则一个一个挑
     * @param date
     * @param trainCode
     * @param seatType
     * @param column
     * @param offsetList
     */
    private void getSeat(List<DailyTrainSeat> finaSeatList,
                         Date date, String trainCode, String seatType,
                         String column, List<Integer> offsetList, Integer startIndex,
                         Integer endIndex){
        List<DailyTrainSeat> getSeatList = new ArrayList<>();
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(
                date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢：", carriageList.size());

        //一个车厢一个车厢的获取座位数据
        for (DailyTrainCarriage dailyTrainCarriage : carriageList) {
            LOG.info("开始从车厢{}选座", dailyTrainCarriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList = dailyTrainSeatService.
                    selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}", dailyTrainCarriage.getIndex(), seatList.size());

            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                String col = dailyTrainSeat.getCol();

                //判断当前座位不能被选中
                boolean alreadyChooseFlag = false;
                for (DailyTrainSeat finalSeat : finaSeatList) {
                    if (finalSeat.getId().equals(dailyTrainSeat.getId())){
                        alreadyChooseFlag = true;
                        break;
                    }
                }
                if (alreadyChooseFlag){
                    LOG.info("座位{}被选中过不能重复选中，继续判断下个座位", seatIndex);
                    continue;
                }

                // 判断column，有值的话，要比对列号

                if (StrUtil.isBlank(column)) {
                    LOG.info("无选座");
                } else {
                    if (!column.equals(col)) {
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}， 目标列值：{}",
                                seatIndex, col, column);
                        continue;
                    }
                }

                Boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if (isChoose) {
                    getSeatList.add(dailyTrainSeat);
                    LOG.info("选中座位");
                } else {
                    continue;
                }

                // 根据offset偏移值选剩下的座位
                boolean isGetAllOffsetSeat = true;
                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{}，校验便宜的座位是否可选", offsetList);
                    // 从索引1开始，因为索引0已经被选中
                    for (int j = 1; j < offsetList.size(); j++) {
                        Integer offset = offsetList.get(j);
                        // 座位在库里的索引是从1开始
//                        int nextIndex = seatIndex + offset - 1;
                        int nextIndex = i + offset;

                        // 有选座时，一定是在同一个车厢
                        if (nextIndex >= seatList.size()) {
                            LOG.info("座位{}不可选，偏移后的索引号超出了这个车厢的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        Boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isChooseNext) {
                            LOG.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
                            getSeatList.add(nextDailyTrainSeat);
                        } else {
                            LOG.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat) {
                    getSeatList = new ArrayList<>();
                    continue;
                }

                //保存选好的座位
                finaSeatList.addAll(getSeatList);
                return;
            }
        }
    }

    /**
     * 计算某座位在区间内是否可卖
     * 例：sell = 10001，本次购买区间站为1~4，则区间为000
     * 全部为0，代表这个区间可售，只要有一个为1，则代表这个区间不可售
     * <p>
     * 选中后，要计算购票后的sell，比如原来是10001，本次购买的区间为1~4
     * 方案：构造本次购票造成的售卖信息为01110，和原sell 10001进行按位或，得到新的sell 11111
     */
    private Boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex,
                         Integer endIndex){
        String sell = dailyTrainSeat.getSell();
        String sellPart = sell.substring(startIndex, endIndex);
        if (Integer.parseInt(sellPart) > 0){
            LOG.info("座位{}在本次车站区间{}~{}已售过票，不可选中该座位",
                    dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        }else {
            LOG.info("座位{}在本次车站区间{}~{}未售过票，可选中该座位",
                    dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            // 111  111
            String curSell = sellPart.replace('0', '1');
            // 0111 0111
            curSell = StrUtil.fillBefore(curSell, '0', endIndex);
            // 01110 01110
            curSell = StrUtil.fillAfter(curSell, '0', sell.length());

            //当前区间售票信息curSell与库里的已售信息sell进行按位或，即可得带到卖出此票之后的售卖信息
            // 15(01111) 14(01110 = 01110 | 00000)
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            // 1111  1110
            String newSell = NumberUtil.getBinaryStr(newSellInt);
            //补0 01111  01110
            newSell = StrUtil.fillBefore(newSell, '0', sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间{}~{}，即{}， 最终售票信息：{}",
                    dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
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
