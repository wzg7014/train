package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.business.domain.SkToken;
import com.wzg.train.business.domain.SkTokenExample;
import com.wzg.train.business.mapper.SkTokenMapper;
import com.wzg.train.business.req.SkTokenQueryReq;
import com.wzg.train.business.req.SkTokenSaveReq;
import com.wzg.train.business.resp.SkTokenQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

    @Autowired
    private DailyTrainStationService dailyTrainStationService;



    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的令牌信息开始", DateUtil.formatDate(date), trainCode);

        //删除已有当天数据
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);

        // 生成该车次的数据
        DateTime now = new DateTime();
        SkToken skToken = new SkToken();
        skToken.setId(SnowUtil.getSnowflakeNextId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);
        skToken.setDate(date);

        // 座位数
        int seatCount = dailyTrainSeatService.countSeat(date, trainCode);
        LOG.info("车次{}座位数：{}",trainCode,seatCount);

        long stationCount = dailyTrainStationService.countByTrain(trainCode,date);
        LOG.info("车次{}站数：{}",trainCode,stationCount);

        //  3/4需要根据实际卖票比例来定，一趟火车最多可以卖(seatCount * stationCount) 张火车票，在一票一站情况下
        int count = (int)(seatCount * stationCount * 3 / 4);
        skToken.setCount(count);

        skTokenMapper.insert(skToken);
    }

    @Resource
    private SkTokenMapper skTokenMapper;

    public void save(SkTokenSaveReq req){
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())){
            skToken.setId(SnowUtil.getSnowflakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else {
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }

    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req){
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<SkToken> skTokens = skTokenMapper.selectByExample(skTokenExample);

        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokens);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokens, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        skTokenMapper.deleteByPrimaryKey(id);
    }

}
