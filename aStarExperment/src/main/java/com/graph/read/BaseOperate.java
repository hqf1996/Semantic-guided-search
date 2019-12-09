package com.graph.read;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by yhj on 2018/5/29.
 */
public abstract class BaseOperate {
    protected String tableName;
    protected Connection conn;
    protected Statement stmt;
    public String[] columns;
    public BaseOperate(String tableName){
        this.tableName = tableName;
        init();
    }

    protected void init(){
        conn = SqlUtil.getConn();
        stmt = SqlUtil.getStatement(conn);
    }

    public Object select(){
        return selectLimit("");
    }

    public abstract Object selectLimit(String limit);

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    protected String getSelectSql(String limit){
        if(columns == null || columns.length <= 0){
            return "select * from " + this.tableName + " "+  limit;
        }else {
            String s = "select ";
            for(int i = 0;i < columns.length-1; i++){
                s = s + columns[i] + ",";
            }
            s = s + columns[columns.length-1] + " from " + this.tableName + " " + limit;
            return s;
        }
    }

    public void close(){
        SqlUtil.close(stmt);
        SqlUtil.close(conn);
    }
}
