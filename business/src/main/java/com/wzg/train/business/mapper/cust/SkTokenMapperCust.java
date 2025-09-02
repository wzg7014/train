package com.wzg.train.business.mapper.cust;

import java.util.Date;

public interface SkTokenMapperCust {
    public int decrease(Date date, String trainCode, int decreaseCount);
}
