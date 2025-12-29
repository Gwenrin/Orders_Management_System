package org.oms.orders_management_system;

import java.sql.SQLException;

public class DatabaseTest
{
    public static void main(String[] args)
    {
        try(java.sql.Connection conn = DatabaseConnection.getConnection())
        {
            System.out.println("Connecting to database...");
        }catch ( SQLException ex)
        {
            System.out.println("Error connecting to database: " + ex.getMessage());
        }

    }
}
