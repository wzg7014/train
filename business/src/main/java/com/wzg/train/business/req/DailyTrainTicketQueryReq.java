package com.wzg.train.business.req;

import com.wzg.train.common.req.PageReq;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

@Data
public class DailyTrainTicketQueryReq extends PageReq {

    private String trainCode;

    private String start;

    private String end;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
}