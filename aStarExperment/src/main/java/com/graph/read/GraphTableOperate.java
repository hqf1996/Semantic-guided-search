package com.graph.read;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yhj on 2018/5/30.
 */
public class GraphTableOperate extends BaseOperate {
    private String[] columns;
    public GraphTableOperate(String tableName){
        super(tableName);
//        columns = new String[]{"id", "entity_id1", "entity_id2", "entity_name1", "predicate_name", "entity_name2"};
        columns = new String[]{"entity_id1", "entity_id2", "entity_name1", "predicate_name", "entity_name2"};
    }

    @Override
    public List<EdgeSource> selectLimit(String limit) {
        ResultSet result = null ;
        String sql = null;
        List<EdgeSource> list = new ArrayList<EdgeSource>();
        int retry = 3;
        while (retry-- > 0){
            try {
                sql = getSelectSql(limit);
//            System.out.println(sql);
                result = stmt.executeQuery(sql);
                while (result.next()){
                    EdgeSource edgeSource = new EdgeSource();
//                    edgeSource.setId(result.getInt("id"));
                    edgeSource.setEntity_id1(result.getString("entity_id1"));
                    edgeSource.setEntity_id2(result.getString("entity_id2"));
                    edgeSource.setEntity_name1(result.getString("entity_name1"));
                    edgeSource.setPredicate_name(result.getString("predicate_name"));
                    edgeSource.setEntity_name2(result.getString("entity_name2"));
                    list.add(edgeSource);
                }
                break;
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(sql);
                init();
            }
        }
        SqlUtil.close(result);
        return list;


//        try {
//            sql = getSelectSql(limit, columns);
////            System.out.println(sql);
//            result = stmt.executeQuery(sql);
//            while (result.next()){
//                EdgeSource edgeSource = new EdgeSource();
//                edgeSource.setId(result.getInt("id"));
//                edgeSource.setEntity_id1(result.getString("entity_id1"));
//                edgeSource.setEntity_id2(result.getString("entity_id2"));
//                edgeSource.setEntity_name1(result.getString("entity_name1"));
//                edgeSource.setPredicate_name(result.getString("predicate_name"));
//                edgeSource.setEntity_name2(result.getString("entity_name2"));
//                list.add(edgeSource);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println(sql);
//        }finally {
//            SqlUtil.close(result);
//            return list;
//        }
    }
}
