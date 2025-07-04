package com.wzg.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzg.train.business.domain.Train;
import com.wzg.train.business.domain.TrainExample;
import com.wzg.train.business.resp.TrainQueryResp;
import com.wzg.train.common.exception.BusinessException;
import com.wzg.train.common.resp.PageResp;
import com.wzg.train.common.utils.SnowUtil;
import com.wzg.train.business.domain.Station;
import com.wzg.train.business.domain.StationExample;
import com.wzg.train.business.mapper.StationMapper;
import com.wzg.train.business.req.StationQueryReq;
import com.wzg.train.business.req.StationSaveReq;
import com.wzg.train.business.resp.StationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wzg.train.common.exception.BusinessExceptionEnum.BUSINESS_STATION_NAME_UNIQUE_ERROR;

@Service
public class StationService {

    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;

    public void save(StationSaveReq req){
        DateTime now = DateTime.now();
        Station station = BeanUtil.copyProperties(req, Station.class);


        //保存数据之前，先判断唯一键是否存在
        StationExample stationExample = new StationExample();
        stationExample.createCriteria().andNameEqualTo(req.getName());
        List<Station> list = stationMapper.selectByExample(stationExample);
        if(ObjectUtil.isNotEmpty(list)) {
            throw new BusinessException(BUSINESS_STATION_NAME_UNIQUE_ERROR);
        }

        if (ObjectUtil.isNull(station.getId())){
            station.setId(SnowUtil.getSnowflakeNextId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        }else {
            station.setUpdateTime(now);
            stationMapper.updateByPrimaryKey(station);
        }

    }

    public PageResp<StationQueryResp> queryList(StationQueryReq req){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("id desc");
        StationExample.Criteria criteria = stationExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Station> stations = stationMapper.selectByExample(stationExample);

        PageInfo<Station> pageInfo = new PageInfo<>(stations);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<StationQueryResp> list = BeanUtil.copyToList(stations, StationQueryResp.class);

        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id){
        stationMapper.deleteByPrimaryKey(id);
    }

    public List<StationQueryResp> queryAll(){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("name_pinyin asc");
        List<Station> stations = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stations, StationQueryResp.class);
    }

}
