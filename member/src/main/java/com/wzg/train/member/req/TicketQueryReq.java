package com.wzg.train.member.req;

import com.wzg.train.common.req.PageReq;
import lombok.Data;

@Data
public class TicketQueryReq extends PageReq {

    private Long memberId;

}