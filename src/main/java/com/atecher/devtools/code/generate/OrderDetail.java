package com.atecher.devtools.code.generate;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/8/28 下午8:28
 */
@Data
@Builder
public class OrderDetail{
    private String orderId;
    private String shopCode;
    private String goodsCode;
    private String shelfCode;
}
