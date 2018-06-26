package com.atecher.devtools.code.generate;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/6/25 上午11:11
 */
public class CodeGenerationUtil {

    public static final String type_char = "char";

    public static final String type_varchar = "varchar";

    public static final String type_date = "date";

    public static final String type_datetime = "datetime";

    public static final String type_timestamp = "timestamp";

    public static final String type_int = "int";

    public static final String type_bigint = "bigint";

    public static final String type_text = "text";

    public static final String type_bit = "bit";

    public static final String type_decimal = "decimal";

    public static final String type_blob = "blob";

    public static final String type_float = "float";

    public static final String type_double = "double";

    public static final String type_enum = "enum";

    /**
     * 将数据库字段类型转换为xml中的jdbc类型（有引号，在resultMap中使用）
     * @param type
     * @return
     */
    public static String processXmlType(String type) {
        if ( type.indexOf(type_varchar) > -1 ) {
            return "jdbcType=\"VARCHAR\"";
        } if ( type.indexOf(type_char) > -1 ) {
            return "jdbcType=\"CHAR\"";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "jdbcType=\"BIGINT\"";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "jdbcType=\"INTEGER\"";
        }else if ( type.indexOf(type_datetime) > -1 ) {
            return "jdbcType=\"TIMESTAMP\"";
        }  else if ( type.indexOf(type_date) > -1 ) {
            return "jdbcType=\"DATE\"";
        }else if ( type.indexOf(type_text) > -1 ) {
            return "jdbcType=\"LONGVARCHAR\"";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "jdbcType=\"TIMESTAMP\"";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "jdbcType=\"BIT\"";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "jdbcType=\"DECIMAL\"";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "jdbcType=\"BLOB\"";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "jdbcType=\"FLOAT\"";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "jdbcType=\"DOUBLE\"";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "jdbcType=\"VARCHAR\"";
        }
        return null;
    }

    /**
     * 将数据库字段类型转换为xml中的jdbc类型(去掉引号，在sql语句中使用)
     * @param type
     * @return
     */
    public static String processXmlType2( String type ) {
        if ( type.indexOf(type_varchar) > -1 ) {
            return "jdbcType=VARCHAR";
        } if ( type.indexOf(type_char) > -1 ) {
            return "jdbcType=CHAR";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "jdbcType=BIGINT";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "jdbcType=INTEGER";
        } else if ( type.indexOf(type_datetime) > -1 ) {
            return "jdbcType=TIMESTAMP";
        } else if ( type.indexOf(type_date) > -1 ) {
            return "jdbcType=DATE";
        }else if ( type.indexOf(type_text) > -1 ) {
            return "jdbcType=LONGVARCHAR";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "jdbcType=TIMESTAMP";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "jdbcType=BIT";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "jdbcType=DECIMAL";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "jdbcType=BLOB";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "jdbcType=FLOAT";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "jdbcType=DOUBLE";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "jdbcType=VARCHAR";
        }
        return null;
    }

    /**
     * 将数据库字段类型转换为java类型
     * @param type
     * @return
     */
    public static String processType(String type) {
        if ( type.indexOf(type_char) > -1 ) {
            return "String";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "Long";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "Integer";
        } else if ( type.indexOf(type_date) > -1 ) {
            return "Date";
        } else if ( type.indexOf(type_text) > -1 ) {
            return "String";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "Date";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "Boolean";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "java.math.BigDecimal";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "byte[]";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "Float";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "Double";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "String";
        }
        return null;
    }
}
