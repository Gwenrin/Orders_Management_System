package org.oms.orders_management_system;

import java.sql.*;
import java.util.*;

public class GenericTableDao
{

    // cytowanie identyfikatorów (rozwiązuje ORDERS i orders)
    private String q(String id)
    {
        return "\"" + id.replace("\"", "\"\"") + "\"";
    }

    // Pobieranie tabel
    public List<String> getTables() throws SQLException
    {
        List<String> tables = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection())
        {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[]
            {
                "TABLE"
            });
            while (rs.next())
            {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    // Pobieranie kolumn
    public List<String> getColumns(String table) throws SQLException
    {
        List<String> cols = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection())
        {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, null);
            while (rs.next())
            {
                cols.add(rs.getString("COLUMN_NAME"));
            }
        }
        return cols;
    }

    // Pobieranie danych
    public List<Map<String, Object>> fetchData(
            String table,
            List<String> columns
    ) throws SQLException
    {

        String cols = columns.stream()
                .map(this::q)
                .reduce((a, b) -> a + ", " + b)
                .orElse("*");

        String sql = "SELECT " + cols + " FROM " + q(table);

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columns)
                {
                    Object value = rs.getObject(col);

                    if (value instanceof Clob clob)
                    {
                        value = clob.getSubString(1, (int) clob.length());
                    }

                    row.put(col, value);

                }
                result.add(row);
            }
        }

        return result;
    }
}
