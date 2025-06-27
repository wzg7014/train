package com.wzg.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.wzg.train.common.context.LoginMemberContext;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.member.domain.Passenger;
import com.wzg.train.member.domain.PassengerExample;
import com.wzg.train.member.mapper.PassengerMapper;
import com.wzg.train.member.req.PassengerQueryReq;
import com.wzg.train.member.req.PassengerSaveReq;
import com.wzg.train.member.resp.PassengerQuaryResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    @Resource
    private PassengerMapper passengerMapper;

    public void save(PassengerSaveReq req){
        DateTime now = DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowflakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

    public List<PassengerQuaryResp> queryList(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if (ObjectUtil.isNotEmpty(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        PageHelper.startPage(4, 3);
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(passengers, PassengerQuaryResp.class);
    }

}
