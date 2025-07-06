package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.business.domain.Train;
import com.wzg.train.business.domain.TrainExample;
import com.wzg.train.business.mapper.TrainMapper;
import com.wzg.train.business.req.TrainQueryReq;
import com.wzg.train.business.req.TrainSaveReq;
import com.wzg.train.business.resp.TrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wzg.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CODE_UNIQUE_ERROR;

@Service
public class TrainService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

    @Resource
    private TrainMapper trainMapper;

    public void save(TrainSaveReq req){
        DateTime now = DateTime.now();

        //保存数据之前，先判断唯一键是否存在
        Train trainDb = selectByUnique(req.getCode());
        if(ObjectUtil.isNotEmpty(trainDb)) {
            throw new BusinessException(BUSINESS_TRAIN_CODE_UNIQUE_ERROR);
        }

        Train train = BeanUtil.copyProperties(req, Train.class);
        if (ObjectUtil.isNull(train.getId())){
            train.setId(SnowUtil.getSnowflakeNextId());
            train.setCreateTime(now);
            train.setUpdateTime(now);
            trainMapper.insert(train);
        }else {
            train.setUpdateTime(now);
            trainMapper.updateByPrimaryKey(train);
        }

    }

    public PageResp<TrainQueryResp> queryList(TrainQueryReq req){
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("code asc");
        TrainExample.Criteria criteria = trainExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Train> trains = trainMapper.selectByExample(trainExample);

        PageInfo<Train> pageInfo = new PageInfo<>(trains);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainQueryResp> list = BeanUtil.copyToList(trains, TrainQueryResp.class);

        PageResp<TrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        trainMapper.deleteByPrimaryKey(id);
    }

    public List<TrainQueryResp> queryAll(){
        List<Train> trains = selectAll();
        return BeanUtil.copyToList(trains, TrainQueryResp.class);
    }

    public List<Train> selectAll() {
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("code asc");
        return trainMapper.selectByExample(trainExample);
    }


    //唯一键是否存在抽取成一个方法
    private Train selectByUnique(String name) {
        TrainExample trainExample = new TrainExample();
        trainExample.createCriteria().andCodeEqualTo(name);
        List<Train> train = trainMapper.selectByExample(trainExample);
        if(CollUtil.isEmpty(train)) {
            return null;
        }else {
            return train.get(0);
        }
    }
}
