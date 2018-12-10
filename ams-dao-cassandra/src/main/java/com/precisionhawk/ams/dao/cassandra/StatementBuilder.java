package com.precisionhawk.ams.dao.cassandra;

import com.datastax.driver.core.SimpleStatement;
import com.precisionhawk.ams.domain.WorkOrderStatus;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class StatementBuilder {
    
    static final String IN_CLAUSE_PLACEHOLDER = "{in_clause}";
    static final String WHERE_CLAUSE_PLACEHOLDER = "{where_clause}";
    
    private String inClause = null;
    private final List<Object> parameters = new LinkedList<>();
    private String template;
    private final StringBuilder whereClause = new StringBuilder();
    
    /** Creates a new builder. */
    public StatementBuilder() {}

    /** Creates a new builder starting with the provided template. */
    public StatementBuilder(String sqlTemplate) {
        this.template = sqlTemplate;
    }
    
    /** Builds and returns the statement. */
    public SimpleStatement build() {
        if (template == null) {
            throw new IllegalArgumentException("No template provided");
        }
        String sql = template.replace(WHERE_CLAUSE_PLACEHOLDER, whereClause.toString());
        if (inClause != null) {
            sql = sql.replace(IN_CLAUSE_PLACEHOLDER, inClause);
        }
        return new SimpleStatement(sql, parameters.toArray());
    }
    
    /**
     * Adds the field to the where clause and stores the value to populate the
     * parameter in the where clause.
     */
    public StatementBuilder addEquals(String colName, Object value) {
        if (whereClause.length() == 0) {
            whereClause.append(" AND ");
        } else {
            whereClause.append(" WHERE ");
        }
        whereClause.append(colName);
        whereClause.append(" = ?");
        parameters.add(value);
        return this;
    }
    
    /**
     * Adds the field to the where clause and stores the value to populate the
     * parameter in the where clause, if the value is not null.
     */
    public StatementBuilder addEqualsConditionally(String colName, Object value) {
        if (whereClause.length() == 0) {
            whereClause.append(" AND ");
        } else {
            whereClause.append(" WHERE ");
        }
        whereClause.append(colName);
        whereClause.append(" = ?");
        parameters.add(value);
        return this;
    }

    /**
     * If the collection holds values, adds an IN clause to the WHERE clause.
     * @param <T> The type of the values.
     * @param colName The name of the field to search on.
     * @param values The values to put into the in clause.
     * @return The resultant builder.
     */
    <T> StatementBuilder addInClauseConditionally(String colName, Collection<T> values) {
        return addInClauseConditionally(colName, values, false);
    }
    
    /**
     * If the collection holds values, adds an IN or NOT IN clause to the WHERE clause.
     * @param <T> The type of the values.
     * @param colName The name of the field to search on.
     * @param values The values to put into the in clause.
     * @param notIn If true, a NOT IN clause will be added rather than an IN clause.
     * @return The resultant builder.
     */
    <T> StatementBuilder addInClauseConditionally(String colName, Collection<T> values, boolean notIn) {
        String str = buildInClause(values);
        if (str != null) {
            inClause = str;
            if (whereClause.length() == 0) {
                whereClause.append(" AND ");
            } else {
                whereClause.append(" WHERE ");
            }
            whereClause.append(colName);
            if (notIn) {
                whereClause.append(" NOT");
            }
            whereClause.append(" IN ");
            whereClause.append(IN_CLAUSE_PLACEHOLDER);
        }
        return this;
    }
    
    /**
     * Generates a value series to be used to replace an IN clause placeholder in the template.
     * @param <T> The type of the values.
     * @param values The values to put into the in clause.
     * @return The resultant builder.
     */
    public <T> StatementBuilder setInClause(Collection<T> values) {
        inClause = buildInClause(values);
        return this;
    }
    
    private <T> String buildInClause(Collection<T> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder("(");
            String delim = "";
            boolean isString;
            for (T value : values) {
                if (value != null) {
                    sb.append(delim);
                    delim = ", ";
                    isString = !(value instanceof Number);
                    if (isString) {
                        sb.append('"');
                    }
                    sb.append(value.toString());
                    if (isString) {
                        sb.append('"');
                    }
                }
            }
            sb.append(")");
            return sb.toString();
        }
        return null;
    }
    
    /**
     * Sets the value of a parameter in the SQL.  This is used when the where clause
     * already exists in the SQL or for setting values in an insert or update.
     */
    public StatementBuilder setParameter(int pos, Object parameter) {
        // Expand the size of the array, if necessary, by adding nulls.
        while (parameters.size() < pos + 1) {
            parameters.add(null);
        }
        parameters.set(pos, parameter);
        return this;
    }
    
    public boolean hasWhereClause() {
        return !parameters.isEmpty();
    }
    
    /** Sets the SQL template to use. */
    public StatementBuilder withSqlTemplate(String sqlTemplate) {
        this.template = sqlTemplate;
        return this;
    }
}
