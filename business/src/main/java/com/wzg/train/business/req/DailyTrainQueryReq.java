package com.wzg.train.business.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wzg.train.common.req.PageReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@Data
public class DailyTrainQueryReq extends PageReq {

    private String code;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

}