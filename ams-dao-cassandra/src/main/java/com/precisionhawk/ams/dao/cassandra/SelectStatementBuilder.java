package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.SimpleStatement;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class SelectStatementBuilder {
    
    private static final String WHERE_CLAUSE_PLACEHOLDER = "{where_clause}";
    
    private String template;
    private final StringBuilder whereClause = new StringBuilder();
    private final List<Object> values = new LinkedList<>();
    
    /** Creates a new builder. */
    public SelectStatementBuilder() {}

    /** Creates a new builder starting with the provided template. */
    public SelectStatementBuilder(String sqlTemplate) {
        this.template = sqlTemplate;
    }
    
    /** Builds and returns the statement. */
    public SimpleStatement build() {
        if (template == null) {
            throw new IllegalArgumentException("No template provided");
        }
        String sql = template.replace(WHERE_CLAUSE_PLACEHOLDER, whereClause.toString());
        return new SimpleStatement(sql, values.toArray());
    }
    
    /** Adds the field to the where clause. */
    public SelectStatementBuilder addEquals(String colName, Object value) {
        if (whereClause.length() == 0) {
            whereClause.append(" AND ");
        } else {
            whereClause.append(" WHERE ");
        }
        whereClause.append(colName);
        whereClause.append(" = ?");
        values.add(value);
        return this;
    }
    
    /** Adds the field to the where clause if the value is not null. */
    public SelectStatementBuilder addEqualsConditionally(String colName, Object value) {
        if (whereClause.length() == 0) {
            whereClause.append(" AND ");
        } else {
            whereClause.append(" WHERE ");
        }
        whereClause.append(colName);
        whereClause.append(" = ?");
        values.add(value);
        return this;
    }
    
    public boolean hasWhereClause() {
        return !values.isEmpty();
    }
    
    /** Sets the SQL template to use. */
    public SelectStatementBuilder withSqlTemplate(String sqlTemplate) {
        this.template = sqlTemplate;
        return this;
    }
}
