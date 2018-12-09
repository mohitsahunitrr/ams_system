package com.precisionhawk.ams.dao.cassandra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps of statements for interacting with data in Cassandra.
 *
 * @author pchapman
 */
public class StatementsMaps {
    
    private String deleteStmt;
    public String getDeleteStmt() {
        return deleteStmt;
    }
    public void setDeleteStmt(String deleteStmt) {
        this.deleteStmt = deleteStmt;
    }
    
    private List<String> initStmts;
    public List<String> getInitStmts() {
        return initStmts;
    }
    public void setInitStmts(List<String> initStmts) {
        this.initStmts = initStmts;
    }

    private String insertStmt;
    public String getInsertStmt() {
        return insertStmt;
    }
    public void setInsertStmt(String insertStmt) {
        this.insertStmt = insertStmt;
    }
    
    private Map<String, String> namedTemplates = new HashMap<>();
    public Map<String, String> getNamedTemplates() {
        return namedTemplates;
    }
    public void setNamedTemplates(Map<String, String> namedTemplates) {
        this.namedTemplates = namedTemplates;
    }

    private String selectTemplate;
    public String getSelectTemplate() {
        return selectTemplate;
    }
    public void setSelectTemplate(String selectTemplate) {
        this.selectTemplate = selectTemplate;
    }

    private String table;
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }

    private String updateStmt;
    public String getUpdateStmt() {
        return updateStmt;
    }
    public void setUpdateStmt(String updateStmt) {
        this.updateStmt = updateStmt;
    }    
}
