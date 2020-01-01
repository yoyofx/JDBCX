package com.bitauto.ep.fx.jdbcx;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class SQLReader
{
    private static Logger logger = LoggerFactory.getLogger(SQLReader.class);

    private Map<String, String> sqlContainer = null;


    public SQLReader() {

        initSqlContainer();
    }



    public String getSql(String key) {
        String sql = sqlContainer.get(key);
        if (sql == null || "".equals(sql))
            logger.warn("不存在该SQL语句");
        if (logger.isDebugEnabled()) {
            logger.debug("SQL:" + sql);
        }
        return sql;
    }

    public String getDynamicalSql(String key, Map<String, ?> param) {
        String sql = sqlContainer.get(key);

        return VelocityUtils.render(sql, param);
    }


    @SuppressWarnings("unchecked")
    private void initSqlContainer()
    {
        sqlContainer = new ConcurrentHashMap<String, String>();
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try
        {
            Resource[] resources = patternResolver.getResources("classpath*:sqlmapper/*.xml");
            for (Resource resource: resources)
            {
                readSQLFromFile(resource.getURL());
            }


        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void readSQLFromFile(URL url) throws IOException
    {
        InputStream ips = url.openStream();
        Document document = null;
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding("utf-8");
        try {
            document = saxReader.read(ips);
        } catch (DocumentException e) {
            logger.error("读取系统中用到的SQL 语句XML出错");
            throw new RuntimeException("读取sql语句XML文件出错:" + e.getMessage());
        }
        Element root = document.getRootElement();
        List<Element> sqlElements = root.selectNodes("//sqlElement");
        String key;
        for (Element sql : sqlElements) {
            key=sql.attribute("key").getValue();
            if(sqlContainer.containsKey(key)){
                logger.warn("key值:"+key+"重复");
            }
            sqlContainer.put(key, sql.getText());

        }

        if (ips != null) {
            try {
                ips.close();
            } catch (IOException e) {
                logger.error("关闭输入流出错:" + e.getMessage());
            }
        }
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (sqlContainer != null) {
            sqlContainer.clear();
            sqlContainer = null;
        }

    }


}
