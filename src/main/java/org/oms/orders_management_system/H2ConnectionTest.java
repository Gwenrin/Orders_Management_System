package org.oms.orders_management_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class H2ConnectionTest
{
    public static void main(String[] args) {
        // Ścieżka do bazy danych
        String jdbcUrl = "jdbc:h2:file:./Database/northwind";
        String user = "sa";
        String password = "";

        try {
            // Ładowanie sterownika
            Class.forName("org.h2.Driver");

            // Nawiązywanie połączenia
            Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
            System.out.println("Połączono z bazą danych H2!");

            // Przykładowe zapytanie
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"customers\"");

            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getString(1) + " | Name: " + resultSet.getString(2));
            }

            // Zamknięcie połączenia
            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
