package com.atecher.devtools.code.generate.v2;

import lombok.Data;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/7/28 下午8:46
 */
@Data
public class Column {
    /**
     * 列名
     */
    private String columnName;
    /**
     * 列名类型
     */
    private String dataType;
    /**
     * 列名备注
     */
    private String comments;

    /**
     * 属性名称(第一个字母大写)，如：user_name => UserName
     */
    private String attrName;

    /**
     * 属性类型
     */
    private String attrType;
    /**
     * auto_increment
     */
    private String extra;
}
