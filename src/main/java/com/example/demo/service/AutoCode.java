package com.example.demo.service;

import com.example.demo.bean.ColumnEntity;
import com.example.demo.bean.TableEntity;
import com.example.demo.mapper.MysqlMapper;
import com.google.common.base.CaseFormat;
import freemarker.template.Template;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author lyc
 * @date 2019/10/29.
 */
@Component
public class AutoCode {

    @Resource
    private MysqlMapper mysqlMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Constant constant;

    public void autoCode(String tname) {
        List<String> tableNames = new ArrayList<>();
        HashMap tMap = new HashMap();
        if(StringUtils.isNotBlank(tname)){
            tMap.put("tableName",tname);
        }
        // 获取所有表信息
        List<Map<String, Object>> list = mysqlMapper.queryList(tMap);
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
                columnEntity.setAttrType(Constant.map.get(columnEntity.getDataType()));
            }
            // 赋值
            table.setColumns(columns);
            Map<String,CodeTemplate> templates = applicationContext.getBeansOfType(CodeTemplate.class);
            for (Map.Entry<String,CodeTemplate> entry:templates.entrySet()){
                CodeTemplate codeTemplate = entry.getValue();
                Map<String, Object> data = new HashMap<>();
                data.put("table",table);
                data.put("swaggerEnable",constant.getSwaggerEnable());
                data.put("author",constant.getAuthor());
                data.put("datetime", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                String pack = constant.getPackPathName().replace("/",".");
                String packageName = pack.substring(0,pack.lastIndexOf("."));
                String moduleName = pack.substring(pack.lastIndexOf(".")+1);
                data.put("package",packageName);
                data.put("moduleName",moduleName);
                data.put("bigDecimal",1);
                codeTemplate.setData(data);
                codeTemplate.outFile();
            }

        }
    }
}