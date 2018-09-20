package com.atecher.devtools.code.generate.v2;

import com.atecher.devtools.code.generate.DBDriver;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/7/28 下午8:43
 */
public class CodeGenration {

    //自动去除表前缀
    public static String AUTO_REOMVE_PRE = "true";

    private static String getTableInfo = "select table_name tableName, engine, table_comment tableComment from information_schema.tables \r\n"
            + "	where table_schema = '%s' and table_name = '%s'";

    private static String getColumnInfo = "select column_name columnName, data_type dataType, column_comment columnComment, column_key columnKey, extra from information_schema.columns\r\n"
            + " where table_schema = '%s' and table_name = '%s' order by ordinal_position";


    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
//        templates.add("templates/common/generator/domain.java.vm");
//        templates.add("templates/common/generator/Dao.java.vm");
        //templates.add("templates/common/generator/Mapper.java.vm");
        templates.add("templates/common/generator/Mapper.xml.vm");
//        templates.add("templates/common/generator/Service.java.vm");
//        templates.add("templates/common/generator/ServiceImpl.java.vm");
//        templates.add("templates/common/generator/Controller.java.vm");
//        templates.add("templates/common/generator/list.html.vm");
//        templates.add("templates/common/generator/add.html.vm");
//        templates.add("templates/common/generator/edit.html.vm");
//        templates.add("templates/common/generator/list.js.vm");
//        templates.add("templates/common/generator/add.js.vm");
//        templates.add("templates/common/generator/edit.js.vm");
        //templates.add("templates/common/generator/menu.sql.vm");
        return templates;
    }


    public static Map<String,Map<String, String>> getTables(List<String> tableNames, DBDriver driver) {
        Map<String,Map<String, String>> result=new HashMap<>();
        for (String tableName : tableNames) {
            String sql = String.format(getTableInfo, driver.getDatebase(), tableName);
            System.out.println(sql);
            Map<String, String> hm = new HashMap<>();
            ResultSet results = null;
            try {
                results = driver.getConncetion().prepareStatement(sql).executeQuery();

                ResultSetMetaData rsmd = results.getMetaData();
                int count = rsmd.getColumnCount();
                while (results.next()){
                    for (int i = 1; i <= count; i++) {
                        String key = rsmd.getColumnLabel(i);
                        String value = (String)results.getObject(i);
                        hm.put(key, value);
                    }
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
            result.put(tableName,hm);

        }
        return  result;
    }

    public static Map<String,List<Map<String, String>>> getColumns(List<String> tableNames, DBDriver driver) {
        Map<String,List<Map<String, String>>> result=new HashMap<>(tableNames.size());
        for (String tableName : tableNames) {
            String sql = String.format(getColumnInfo, driver.getDatebase(), tableName);
            System.out.println(sql);
            List<Map<String, String>> list = new ArrayList<>();
            ResultSet rs = null;
            try {
                rs = driver.getConncetion().prepareStatement(sql).executeQuery();
                if (rs != null) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnCount = md.getColumnCount();

                    Map<String, String> rowData;
                    while (rs.next()) {
                        rowData = new HashMap<>(columnCount);
                        for (int i = 1; i <= columnCount; i++) {
                            rowData.put(md.getColumnLabel(i), rs.getString(i));
                        }
                        list.add(rowData);
                        System.out.println("list:" + list.toString());
                    }

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            result.put(tableName,list);
        }

        return result;

    }


    public static void generatorCode(Map<String, String> table, List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        //表信息
        Table tableInfo = new Table();
        tableInfo.setTableName(table.get("tableName"));
        tableInfo.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableInfo.getTableName(), config.getString("tablePrefix"), config.getString("autoRemovePre"));
        tableInfo.setClassName(className);

        //列信息
        List<Column> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            Column columnDO = new Column();
            columnDO.setColumnName(column.get("columnName"));
            columnDO.setDataType(column.get("dataType"));
            columnDO.setComments(column.get("columnComment"));
            columnDO.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJavaAttr(columnDO.getColumnName());
            columnDO.setAttrName(attrName);

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnDO.getDataType(), "unknowType");
            columnDO.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableInfo.getPk() == null) {
                tableInfo.setPk(columnDO);
            }

            columsList.add(columnDO);
        }
        tableInfo.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableInfo.getPk() == null) {
            tableInfo.setPk(tableInfo.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>(16);
        map.put("tableName", tableInfo.getTableName());
        map.put("comments", tableInfo.getComments());
        map.put("pk", tableInfo.getPk());
        map.put("className", tableInfo.getClassName());
        map.put("classname", StringUtils.uncapitalize(tableInfo.getClassName()));
        map.put("pathName", config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1));
        map.put("columns", tableInfo.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, StringUtils.uncapitalize(tableInfo.getClassName()), tableInfo.getClassName(), config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new GeneratorException("渲染模板失败，表名：" + tableInfo.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    public static String columnToJavaAttr(String columnName) {
        return StringUtils.uncapitalize(columnToJava(columnName));
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix, String autoRemovePre) {
        if (AUTO_REOMVE_PRE.equals(autoRemovePre)) {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }

        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new GeneratorException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String classname, String className, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        //String modulesname=config.getString("packageName");
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains("domain.java.vm")) {
            return packagePath + "domain" + File.separator + className + "DO.java";
        }

        if (template.contains("Dao.java.vm")) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

//		if(template.contains("Mapper.java.vm")){
//			return packagePath + "dao" + File.separator + className + "Mapper.java";
//		}

        if (template.contains("Service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + packageName + File.separator + className + "Mapper.xml";
        }

        if (template.contains("list.html.vm")) {
            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
                    + packageName + File.separator + classname + File.separator + classname + ".html";
            //				+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".html";
        }
        if (template.contains("add.html.vm")) {
            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
                    + packageName + File.separator + classname + File.separator + "add.html";
        }
        if (template.contains("edit.html.vm")) {
            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
                    + packageName + File.separator + classname + File.separator + "edit.html";
        }

        if (template.contains("list.js.vm")) {
            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + classname + ".js";
            //		+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".js";
        }
        if (template.contains("add.js.vm")) {
            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + "add.js";
        }
        if (template.contains("edit.js.vm")) {
            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + "edit.js";
        }

//		if(template.contains("menu.sql.vm")){
//			return className.toLowerCase() + "_menu.sql";
//		}

        return null;
    }

    public static void main(String[] args) throws IOException {
        DBDriver driver = DBDriver.builder().datebase("grampus_user").ip("10.7.8.22").user("grampus").password("123456").build();
        List<String> tableNames = Arrays.asList("gms_user_delivery_address");
        Map<String, Map<String, String>> tables = getTables(tableNames, driver);
        Map<String, List<Map<String, String>>> columns = getColumns(tableNames, driver);
        File file = new File("/Users/mark/1.zip");
        if (!file.exists()){
            file.createNewFile();
        }
        ZipOutputStream zos = new ZipOutputStream (
                new FileOutputStream(file) ) ;
        for(String tableName:tableNames){


            generatorCode(tables.get(tableName), columns.get(tableName),zos);
        }

        zos.close();


    }
}
