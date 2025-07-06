package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.business.domain.dailyTrainSeat;
import com.wzg.train.business.domain.dailyTrainSeatExample;
import com.wzg.train.business.mapper.dailyTrainSeatMapper;
import com.wzg.train.business.req.dailyTrainSeatQueryReq;
import com.wzg.train.business.req.dailyTrainSeatSaveReq;
import com.wzg.train.business.resp.dailyTrainSeatQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class dailyTrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(dailyTrainSeatService.class);

    @Resource
    private dailyTrainSeatMapper dailyTrainSeatMapper;

    public void save(dailyTrainSeatSaveReq req){
        DateTime now = DateTime.now();
        dailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, dailyTrainSeat.class);
        if (ObjectUtil.isNull(dailyTrainSeat.getId())){
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else {
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }

    }

    public PageResp<dailyTrainSeatQueryResp> queryList(dailyTrainSeatQueryReq req){
        dailyTrainSeatExample dailyTrainSeatExample = new dailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("id desc");
        dailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<dailyTrainSeat> dailyTrainSeats = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);

        PageInfo<dailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeats);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<dailyTrainSeatQueryResp> list = BeanUtil.copyToList(dailyTrainSeats, dailyTrainSeatQueryResp.class);

        PageResp<dailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }

}
