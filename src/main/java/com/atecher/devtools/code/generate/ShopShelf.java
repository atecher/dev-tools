package com.atecher.devtools.code.generate;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/8/28 下午8:28
 */
@Data
@Builder
public class ShopShelf {
    private String shelfCode;
    private String shopCode;
    private String qrCode;
    private Set<String> goods;
}
