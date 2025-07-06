package com.wzg.train.business.mapper;

import com.wzg.train.business.domain.dailyTrainSeat;
import com.wzg.train.business.domain.dailyTrainSeatExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface dailyTrainSeatMapper {
    long countByExample(dailyTrainSeatExample example);

    int deleteByExample(dailyTrainSeatExample example);

    int deleteByPrimaryKey(Long id);

    int insert(dailyTrainSeat record);

    int insertSelective(dailyTrainSeat record);

    List<dailyTrainSeat> selectByExample(dailyTrainSeatExample example);

    dailyTrainSeat selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") dailyTrainSeat record, @Param("example") dailyTrainSeatExample example);

    int updateByExample(@Param("record") dailyTrainSeat record, @Param("example") dailyTrainSeatExample example);

    int updateByPrimaryKeySelective(dailyTrainSeat record);

    int updateByPrimaryKey(dailyTrainSeat record);
}