package com.example.demo.service;

import com.example.demo.bean.ColumnEntity;
import com.example.demo.bean.TableEntity;
import com.example.demo.mapper.MysqlMapper;
import com.google.common.base.CaseFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author lyc
 * @date 2019/8/12.
 */
@Service
public class MysqlAutoService {
    @Resource private MysqlMapper mysqlMapper;

    @Autowired
    Configuration configuration;

    @Value("${parentPath:./src/main/java/com/example/demo/test}")
    private String parentPath;

    @Value("${entityPath:/entity/}")
    private String entityPath;

    @Value("${mapperPath:/mapper/}")
    private String mapperPath;

    @Value("${servicePath:/service/}")
    private String servicePath;

    @Value("${servicePath:/service/impl/}")
    private String serviceImplPath;

    @Value("${controllerPath:/controller/}")
    private String controllerPath;

    public void autoCode() throws IOException, TemplateException {
       List<String> tableNames = new ArrayList<>();
       // 获取所有表信息
       List<Map<String, Object>> list = mysqlMapper.queryList(null);
       for (int i = 0; i < list.size(); i++) {
           String tableName = (String) list.get(i).get("tableName");
           tableNames.add(tableName);
       }
       //获取 表信息
       for(String tableName : tableNames){
           //查询表信息
           TableEntity table =  mysqlMapper.queryTable(tableName);
           // 赋值 类名
           table.setClassName(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getTableName()));
           table.setAttrName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.getTableName()));
           //查询列信息
           List<ColumnEntity> columns = mysqlMapper.queryColumns(tableName);
           for (int i = 0; i < columns.size(); i++) {
               // 赋值 属性 下划线-》驼峰 和 数据库类型-》java 类型
               ColumnEntity columnEntity = columns.get(i);
               columnEntity.setAttrName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnEntity.getColumnName()));
               columnEntity.setAttrType(map.get(columnEntity.getDataType()));
           }
           // 赋值

           table.setColumns(columns);

          // System.out.println(columns);
           //渲染模板
           Map<String,Template> hashMap = new HashMap();
           hashMap.put("Entity",configuration.getTemplate("Entity.java.ftl"));
           hashMap.put("Mapper",configuration.getTemplate("Mapper.java.ftl"));
           hashMap.put("Service",configuration.getTemplate("Service.java.ftl"));
           hashMap.put("ServiceImpl",configuration.getTemplate("ServiceImpl.java.ftl"));
           hashMap.put("Controller",configuration.getTemplate("Controller.java.ftl"));

           for (Map.Entry entry:hashMap.entrySet()) {
               Template template = (Template) entry.getValue();
               String key = (String) entry.getKey();
               //数据
               Map<String, Object> data = new HashMap<>();
               data.put("table",table);
               data.put("author","lyc");
               data.put("datetime", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));

               data.put("package","com.example.demo");
               data.put("moduleName","test");
               data.put("bigDecimal",1);
               //从设置的目录中获得模板
               String result = FreeMarkerTemplateUtils.processTemplateIntoString(template,data);
               if(key.equals("Entity")){
                   FileUtils.write(new File(parentPath+entityPath+table.getClassName()+".java"),result,"utf-8");
               }

               if(key.equals("Mapper")){
                   FileUtils.write(new File(parentPath+mapperPath+table.getClassName()+"Mapper.java"),result,"utf-8");
               }

               if(key.equals("Service")){
                   FileUtils.write(new File(parentPath+servicePath+table.getClassName()+"Service.java"),result,"utf-8");
               }

               if(key.equals("ServiceImpl")){
                   FileUtils.write(new File(parentPath+serviceImplPath+table.getClassName()+"ServiceImpl.java"),result,"utf-8");
               }

               if(key.equals("Controller")){
                   FileUtils.write(new File(parentPath+controllerPath+table.getClassName()+"Controller.java"),result,"utf-8");
               }
           }

       }
   }

   //类型转换
   public static Map<String,String> map = new HashMap();

    static {
        map.put("tinyint","Integer");
        map.put("smallint","Integer");
        map.put("mediumint","Integer");
        map.put("int","Integer");
        map.put("integer","Integer");
        map.put("bigint","Long");
        map.put("float","Float");
        map.put("double","Double");
        map.put("decimal","BigDecimal");
        map.put("bit","Boolean");
        map.put("char","String");
        map.put("varchar","String");
        map.put("tinytext","String");
        map.put("text","String");
        map.put("mediumtext","String");
        map.put("longtext","String");
        map.put("date","Date");
        map.put("datetime","Date");
        map.put("timestamp","Date");

    }

}