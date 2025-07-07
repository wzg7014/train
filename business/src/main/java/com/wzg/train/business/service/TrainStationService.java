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
import com.wzg.train.business.domain.TrainStation;
import com.wzg.train.business.domain.TrainStationExample;
import com.wzg.train.business.mapper.TrainStationMapper;
import com.wzg.train.business.req.TrainStationQueryReq;
import com.wzg.train.business.req.TrainStationSaveReq;
import com.wzg.train.business.resp.TrainStationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wzg.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR;
import static com.wzg.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR;

@Service
public class TrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    public void save(TrainStationSaveReq req){
        DateTime now = DateTime.now();

        //保存数据之前，先判断唯一键是否存在
        TrainStation trainStationDb = selectByUnique(req.getTrainCode(),req.getIndex());

        if(ObjectUtil.isNotEmpty(trainStationDb)) {
            throw new BusinessException(BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
        }

        TrainStation trainStationDb1 = selectByUnique(req.getTrainCode(),req.getName());
        if(ObjectUtil.isNotEmpty(trainStationDb1)) {
            throw new BusinessException(BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
        }

        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if (ObjectUtil.isNull(trainStation.getId())){
            trainStation.setId(SnowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        }else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }

    }

    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req){
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code asc, `index` asc");
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation> trainStations = trainStationMapper.selectByExample(trainStationExample);

        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStations);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStations, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        trainStationMapper.deleteByPrimaryKey(id);
    }

    //唯一键是否存在抽取成一个方法
    private TrainStation selectByUnique(String code, Integer index) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(code).andIndexEqualTo(index);
        List<TrainStation> trainStation = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isEmpty(trainStation)) {
            return null;
        }else {
            return trainStation.get(0);
        }
    }

    private TrainStation selectByUnique(String code, String name) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(code).andNameEqualTo(name);
        List<TrainStation> trainStation = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isEmpty(trainStation)) {
            return null;
        }else {
            return trainStation.get(0);
        }
    }

    public List<TrainStation> selectByTrainCode(String code) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("`index` asc");
        trainStationExample.createCriteria().andTrainCodeEqualTo(code);
        return trainStationMapper.selectByExample(trainStationExample);
    }


}
