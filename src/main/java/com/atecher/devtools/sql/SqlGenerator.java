package com.atecher.devtools.sql;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/9/20 下午4:14
 */
public class SqlGenerator {
    public static void main(String[] args) {
        String sqlTemplate="ALTER TABLE gms_order ADD COLUMN order_source tinyint(2) NOT NULL DEFAULT 1 COMMENT '订单来源：1 便利购（微信小程序与支付宝）；2 每日优鲜H5';";
        List<String> sqlList=new ArrayList<>();
        for(int i=0;i<10;i++){
            sqlList.add("\r\n\nuse gms_order_"+i+";");
            for(int j=0;j<256;j++){
                sqlList.add(sqlTemplate.replaceFirst("gms_order","gms_order_"+j));
            }
        }

        try {
            FileUtils.writeLines(new File("/Users/mark/Work/update.sql"),sqlList);
        }catch (Exception e){

        }

    }
}
