package com.wzg.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.exception.BusinessExceptionEnum;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.member.domain.Member;
import com.wzg.train.member.domain.MemberExample;
import com.wzg.train.member.mapper.MemberMapper;
import com.wzg.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req){
        String mobile = req.getMobile();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);

        if (CollUtil.isNotEmpty(list)){
            //return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        Member member = new Member();
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);

        memberMapper.insert(member);
        return member.getId();
    }
}

