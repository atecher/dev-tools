package com.atecher.devtools.code.generate;

import lombok.*;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static com.atecher.devtools.code.generate.CodeGenerationUtil.processType;
import static com.atecher.devtools.code.generate.CodeGenerationUtil.processXmlType;
import static com.atecher.devtools.code.generate.CodeGenerationUtil.processXmlType2;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/6/25 上午11:03
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGeneration {


    /**
     **********************************使用前必读*******************
     **
     ** 使用需要修改的值：{
     *					1-dbName：数据库名；
     *					2-user：数据库名；
     *					3-password：数据库密码；
     *					4-url:数据库连接；
     *	 }
     **
     ***********************************************************
     */



    private String moduleName = "";
    // 模块名

    private String tableName;

    private String beanName;

    private String mapperName;

    private List<String> tables;



    private StoreConfig storeConfig;
    private DBDriver driver;

    /**
     *  获取所有的表 or 配置要生成的表
     * @return
     * @throws SQLException
     */
    private List<String> getTables(){
        return tables;
    }

    /**
     * 将表名转换为bean名称
     * @param table
     */
    private void processTable(String table) {
        StringBuffer sb = new StringBuffer(table.length());
        String[] tables = table.split("_");
        String temp = null;
        for ( int i = 0 ; i < tables.length ; i++ ) {
            temp = tables[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        beanName = sb.toString();
        mapperName = storeConfig.getMapper_package()+"."+beanName + "Mapper";
    }



    /**
     * 将数据库字段名转化为bean字段名
     * @param field
     * @return
     */
    private String processField( String field ) {
        StringBuffer sb = new StringBuffer(field.length());
        String[] fields = field.split("_");
        String temp = null;
        sb.append(fields[0]);
        for ( int i = 1 ; i < fields.length ; i++ ) {
            temp = fields[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        return sb.toString();
    }


    /**
     * 实体用全名
     *
     * @param beanName
     * @return
     */
    private String processResultMapId(String beanName) {
//        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);//用别名
        return storeConfig.getBean_package()+"."+beanName;//用全名
    }

    /**
     *  将实体类名首字母改为小写
     * @param beanName
     * @return
     */
    private String processResultMapId2( String beanName ) {
        //用别名
        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
    }

    /**
     *  构建类上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException
     */
    private BufferedWriter buildClassComment(BufferedWriter bw, String text) throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("/**");
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" * " + text);
        bw.newLine();
        bw.write(" * @author hailin.su");
        bw.newLine();
        bw.write(" * @date " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        bw.newLine();
        bw.write(" **/");
        return bw;
    }

    /**
     *  生成实体对象
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildEntityBean(List<String> columns, List<String> types, List<String> comments, String tableComment) throws IOException {
        File folder = new File(storeConfig.getBean_path());
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File beanFile = new File(storeConfig.getBean_path(), beanName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + storeConfig.getBean_package() + ";");
        bw.newLine();
        bw.write("import java.io.Serializable;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();

        bw.write("public class " + beanName + " implements Serializable {");
        bw.newLine();
        bw.newLine();

        bw.write("\tprivate static final long serialVersionUID = "+getRandomNum(19)+"L;");
        bw.newLine();
        bw.newLine();

        int size = columns.size();
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("\t/**" + comments.get(i) + "**/");
            bw.newLine();
            bw.write("\tprivate " + processType(types.get(i)) + " " + processField(columns.get(i)) + ";");
            bw.newLine();
            bw.newLine();
        }
        bw.newLine();
        // 生成get 和 set方法
        String tempField = null;
        String _tempField = null;
        String tempType = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempType = processType(types.get(i));
            _tempField = processField(columns.get(i));
            tempField = _tempField.substring(0, 1).toUpperCase() + _tempField.substring(1);
            bw.newLine();
            bw.write("\tpublic void set" + tempField + "(" + tempType + " " + _tempField + "){");
            bw.newLine();
            bw.write("\t\tthis." + _tempField + " = " + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
            bw.write("\tpublic " + tempType + " get" + tempField + "(){");
            bw.newLine();
            bw.write("\t\treturn this." + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
        }
        bw.newLine();

        //重写toString
        bw.write("\t@Override");
        bw.newLine();
        bw.write("\tpublic String toString() {");
        bw.newLine();
        String _field = null;
        bw.write("\t\treturn \""+beanName+" [ ");
        for ( int i = 0 ; i < size ; i++ ) {
            _field = processField(columns.get(i));
            bw.write(_field+"= \"+"+_field);
            if((i+1)<size){
                bw.write("+");
                bw.newLine();
                bw.write("\t\t\t\",");
            }
        }
        bw.write("+\"]\";");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();


        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }


    /**
     *  构建实体类映射XML文件
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildMapperXml(List<String> columns, List<String> types, List<String> comments ,String tableComment) throws IOException {
        File folder = new File(storeConfig.getXml_path());
        System.out.println(folder.getAbsolutePath());
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File mapperXmlFile = new File(storeConfig.getXml_path(), beanName + "Mapper.xml");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
        bw.write("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
        bw.write("<mapper namespace=\"" + mapperName + "\">");
        bw.newLine();
        bw.newLine();

        //通用结果集BaseResultMap的生成
        bw.write("\t<!--cg generate by hailin.su at "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())+"-->");
        bw.newLine();
        bw.newLine();

        bw.write("\t<!--通用"+tableComment+"表映射-->");
        bw.newLine();
        bw.write("\t<resultMap id=\"BaseResultMap\" type=\""+processResultMapId(beanName)+"\" >");
        bw.newLine();
        int size = columns.size();
        bw.write("\t\t<id property=\"" + this.processField(columns.get(0)) + "\" column=\"" + columns.get(0) +"\" "
                +processXmlType(types.get(0))+ " />");
        bw.newLine();
        for ( int i = 1 ; i < size ; i++ ) {
            bw.write("\t\t<result property=\"" + this.processField(columns.get(i)) + "\" column=\"" + columns.get(i) +"\" "
                    +processXmlType(types.get(i))+ " />");
            bw.newLine();
        }
        bw.write("\t</resultMap>");

        bw.newLine();
        bw.newLine();
        bw.newLine();


        // 下面开始写SqlMapper中的方法
        buildSQL(bw, columns, types);

        bw.write("</mapper>");
        bw.flush();
        bw.close();
    }


    /**
     *  生成mapper类
     *
     * @param types
     * @throws IOException
     */
    private void buildMapper(List<String> types,String tableComment)throws IOException {
        File folder = new File(storeConfig.getMapper_path());
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File beanFile = new File(storeConfig.getMapper_path(), beanName + "Mapper.java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + storeConfig.getMapper_package() + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.newLine();

        bw.write("import "+storeConfig.getBean_package()+"."+beanName+";");
        bw.newLine();
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("public interface " + beanName + "Mapper {");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic " + beanName + " selectByPrimaryKey("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int deleteByPrimaryKey("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();


        bw.write("\tpublic int insertSelective("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int updateByPrimaryKeySelective("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int updateByPrimaryKey("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic Long selectObjectListPageTotal("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic List<" + beanName + "> selectObjectListPage("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic List<" + beanName + "> selectByObjectList("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     *  生成service接口
     *
     * @param types
     * @throws IOException
     */
    private void buildInterService(List<String> types,String tableComment)throws IOException {
        File folder = new File(storeConfig.getI_service_path());
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
        String className = "I"+beanName + "Service";

        File beanFile = new File(storeConfig.getI_service_path(),className+".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + storeConfig.getI_service_package() + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.newLine();

        bw.write("import "+storeConfig.getBean_package()+"."+beanName+";");
        bw.newLine();
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("public interface " + className + " {");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic " + beanName + " selectByPrimaryKey("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int deleteByPrimaryKey("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();


        bw.write("\tpublic int insertSelective("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int updateByPrimaryKeySelective("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic int updateByPrimaryKey("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic Long selectObjectListPageTotal("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic List<" + beanName + "> selectObjectListPage("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tpublic List<" + beanName + "> selectByObjectList("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     *  生成service 实现类
     *
     * @param types
     * @throws IOException
     */
    private void buildImplService(List<String> types,String tableComment)throws IOException {
        File folder = new File(storeConfig.getService_path());
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
        String className = beanName + "Service";

        String i_className = "I" + beanName + "Service";

        File beanFile = new File(storeConfig.getService_path(),className+".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + storeConfig.getService_package() + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.write("import org.slf4j.Logger;");
        bw.newLine();
        bw.write("import org.slf4j.LoggerFactory;");
        bw.newLine();
        bw.write("import org.springframework.stereotype.Service;");
        bw.newLine();
        bw.write("import javax.annotation.Resource;");
        bw.newLine();
        bw.write("import " + storeConfig.getMapper_package() + "." + beanName + "Mapper;");
        bw.newLine();
        bw.write("import " + storeConfig.getI_service_package() + "." + i_className+";");
        bw.newLine();
        bw.newLine();

        bw.write("import "+storeConfig.getBean_package()+"."+beanName+";");
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("@Service");
        bw.newLine();
        bw.write("public class " + className + " implements " + i_className + " {");
        bw.newLine();
        bw.newLine();
        bw.write("\tprivate static Logger logger = LoggerFactory.getLogger("+className+".class);");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Resource");bw.newLine();
        String daoName = processResultMapId2(beanName)+"Mapper";
        bw.write("\tprivate "+beanName+"Mapper "+daoName+";");bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic " + beanName + " selectByPrimaryKey("+processType(types.get(0))+" id) {");
        bw.newLine();

        bw.write("\t\treturn "+daoName+".selectByPrimaryKey(id);");bw.newLine();

        bw.write("\t}");
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic int deleteByPrimaryKey(" + processType(types.get(0)) + " id) {");
        bw.newLine();
        bw.newLine();
        bw.write("\t\treturn "+daoName+".deleteByPrimaryKey(id);");bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic int insertSelective(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".insertSelective("+processResultMapId2(beanName)+");");bw.newLine();


        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic int updateByPrimaryKeySelective(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".updateByPrimaryKeySelective("+processResultMapId2(beanName)+");");bw.newLine();

        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic int updateByPrimaryKey(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".updateByPrimaryKey("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic Long selectObjectListPageTotal(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".selectObjectListPageTotal("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic List<" + beanName + "> selectObjectListPage(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".selectObjectListPage("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic List<" + beanName + "> selectByObjectList("+beanName+" "+processResultMapId2(beanName)+"){");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".selectByObjectList("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");
        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }



    private void buildSQL( BufferedWriter bw, List<String> columns, List<String> types ) throws IOException {
        String tempField = null;

        int size = columns.size();

        // 通用结果列
        bw.write("\t<!-- 通用查询结果集合-->");
        bw.newLine();
        bw.write("\t<sql id=\"Base_Column_List\">");
        bw.newLine();

        bw.write("\t"+columns.get(0)+",");
        for ( int i = 1 ; i < size ; i++ ) {
            bw.write("\t" + columns.get(i));
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }

        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();

        //通用查询条件拼接
        bw.write("\t<!-- 公共查询条件-->");
        bw.newLine();
        bw.write("\t<!-- collection foreach DATE_FORMAT(create_time,'%Y-%m-%d') like CONCAT('%',#{goodsNo,jdbcType=VARCHAR},'%') -->");
        bw.newLine();
        bw.write("\t<!-- <![CDATA[<=]]> date_format(FROM_UNIXTIME(expire_time),'%Y-%c-%d %h:%i:%s') as showExpireTime-->");
        bw.newLine();
        bw.newLine();
        bw.write("\t<sql id=\"conditions\">");
        bw.newLine();

        for (int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            //todo 数字类型=0的要特殊处理 delFlag != '' 若是0   false
            String type = processXmlType2(types.get(i));
            if ("jdbcType=INTEGER".equals(type)){
                bw.write("\t\t<if test=\"" + tempField + " != null \"> and "+ columns.get(i) + " = #{" + tempField + ","+type+"} </if>");
            }else {
                bw.write("\t\t<if test=\"" + tempField + " != null and "+tempField+" != '' \"> and "+ columns.get(i) + " = #{" + tempField + ","+type+"} </if>");
            }
            bw.newLine();
        }

        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();

        //通用排序语句

        // 查询（根据主键ID查询）
        bw.write("\t<!-- 查询（根据主键ID查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"selectByPrimaryKey\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+processXmlType2(types.get(0))+"}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 查询完


        // 删除（根据主键ID删除）
        bw.write("\t<!--删除：根据主键ID删除-->");
        bw.newLine();
        bw.write("\t<delete id=\"deleteByPrimaryKey\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
        bw.newLine();
        bw.write("\t\t DELETE FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+processXmlType2(types.get(0))+"}");
        bw.newLine();
        bw.write("\t</delete>");
        bw.newLine();
        bw.newLine();
        // 删除完


        //按条件删除
       /* bw.write("\t<!-- 删除：根据输入条件删除 -->");
        bw.newLine();
        bw.write("\t<delete id=\"deleteByObject\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t DELETE FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t</delete>");
        bw.newLine();
        bw.newLine();*/




        //---------------  insert方法（匹配有值的字段）
        bw.write("\t<!-- 添加 （匹配有值的字段,不建议使用）-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertSelective\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();

        tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\"> "+ columns.get(i) + ",</if>");
            bw.newLine();
        }

        bw.write("\t\t </trim>");
        bw.newLine();

        bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();

        tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + "!=null\">#{"+ tempField + ","+processXmlType2(types.get(i))+"},</if>");
            bw.newLine();
        }

        bw.write("\t\t </trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
        //---------------  完毕


        // 修改update方法（匹配有值的字段）
        bw.write("\t<!-- 根据主键修改输入的值-->");
        bw.newLine();
        bw.write("\t<update id=\"updateByPrimaryKeySelective\" parameterType=\"" +processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write(" \t\t <set> ");
        bw.newLine();

        tempField = null;
        for ( int i = 1 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">"+ columns.get(i) + " = #{" + tempField + ","+processXmlType2(types.get(i))+"},</if>");
            bw.newLine();
        }

        bw.newLine();
        bw.write(" \t\t </set>");
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+processXmlType2(types.get(0))+"}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();
        // update方法完毕

        // ----- 修改（全量修改）
        bw.write("\t<!-- 根据主键全量修改,不建议使用-->");
        bw.newLine();
        bw.write("\t<update id=\"updateByPrimaryKey\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write("\t\t SET ");

        bw.newLine();
        tempField = null;
        for ( int i = 1 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t " + columns.get(i) + " = #{" + tempField + "}");
            if ( i != size - 1 ) {
                bw.write(",");
            }
            bw.newLine();
        }

        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();

        //分页查询
        bw.write("\t<!-- 分页查询 -->");
        bw.newLine();
        bw.write("\t<select id=\"selectObjectListPage\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.util.HashMap\" useCache=\"false\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t\t ORDER BY id DESC");
        bw.newLine();
        bw.write("\t\t limit #{startOfPage},#{pageSize}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

        //分页查询总数
        bw.write("\t<!-- 分页查询总数 -->");
        bw.newLine();
        bw.write("\t<select id=\"selectObjectListPageTotal\" resultType=\"java.lang.Long"
                + "\" parameterType=\"java.util.HashMap\" useCache=\"false\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t count(*) ");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

        //按条件查询列表
        bw.write("\t<!-- 按条件查询列表 -->");
        bw.newLine();
        bw.write("\t<select id=\"selectByObjectList\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.util.HashMap\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

    }


    /**
     *  获取所有的数据库表注释
     *
     * @return
     * @throws SQLException
     */
    private Map<String, String> getTableComment() throws SQLException {
        Map<String, String> maps = new HashMap<>();
        PreparedStatement pstate = driver.getConncetion().prepareStatement("show table status");
        ResultSet results = pstate.executeQuery();
        while ( results.next() ) {
            String tableName = results.getString("NAME");
            String comment = results.getString("COMMENT");
            maps.put(tableName, comment);
        }
        return maps;
    }

    private String getRandomNum(int length){
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        code.append(String.valueOf(random.nextInt(90)+1));
        for (int i = 2; i < length; i++) {
            code.append(String.valueOf(random.nextInt(10)));
        }
        return code.toString();
    }

    public void generate() throws ClassNotFoundException, SQLException, IOException {
        String prefix = "show full fields from ";
        List<String> columns;
        List<String> types;
        List<String> comments;
        PreparedStatement pstate;
        List<String> tables = getTables();
        Map<String, String> tableComments = getTableComment();
        for ( String table : tables ) {
            columns = new ArrayList<>();
            types = new ArrayList<>();
            comments = new ArrayList<>();
            pstate = driver.getConncetion().prepareStatement(prefix + table);
            ResultSet results = pstate.executeQuery();
            while ( results.next() ) {
                columns.add(results.getString("FIELD"));
                types.add(results.getString("TYPE"));
                comments.add(results.getString("COMMENT"));
            }
            tableName = table;
            processTable(table);
            //表名注释
            String tableComment = tableComments.get(tableName);
            buildEntityBean(columns, types, comments, tableComment);
            buildMapperXml(columns, types, comments,tableComment);
            buildMapper(types,tableComment);
            buildInterService(types, tableComment);
            buildImplService(types, tableComment);
        }
        driver.getConncetion().close();
    }


    public static void main( String[] args ) {
        try {
            DBDriver build = DBDriver.builder().datebase("grampus_user").ip("10.7.8.22").user("grampus").password("123456").build();
            StoreConfig storeConfig = StoreConfig.builder().base_package("com.mryx.grampus.user").storePath("/Users/mark/cg").database(build.getDatebase()).build();

            CodeGeneration codeGeneration = CodeGeneration.builder().driver(build).storeConfig(storeConfig).tables(Arrays.asList("gms_user_delivery_address")).build();
            codeGeneration.generate();
            System.out.println("===============success======================");
        } catch (ClassNotFoundException e ) {
            e.printStackTrace();
        } catch (SQLException e ) {
            e.printStackTrace();
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }
}
