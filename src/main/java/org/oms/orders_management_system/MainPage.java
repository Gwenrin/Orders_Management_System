package org.oms.orders_management_system;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;

public class MainPage
{

    private final Map<String, Object> resultValues = new java.util.HashMap<>();

    @FXML
    private ComboBox<String> tableComboBox;

    @FXML
    private VBox columnsVBox;

    @FXML
    private TableView<Map<String, Object>> dataTable;

    @FXML
    private ComboBox<String> columnComboBox;

    @FXML
    private Button loadButton, avgButton, minButton, maxButton, sortAscButton, sortDescButton, clearResultsButton;

    @FXML
    public void initialize()
    {
        dataTable.setPlaceholder(new Label("Select table and columns, then LOAD DATA"));

        try
        {
            GenericTableDao dao = new GenericTableDao();
            tableComboBox.getItems().setAll(dao.getTables());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        tableComboBox.setOnAction(e -> loadColumnsForSelectedTable());
        loadButton.setOnAction(e -> handleLoadData());

        avgButton.setDisable(true);
        minButton.setDisable(true);
        maxButton.setDisable(true);
        sortAscButton.setDisable(true);
        sortDescButton.setDisable(true);

        avgButton.setOnAction(e -> handleAvg());
        minButton.setOnAction(e -> handleMin());
        maxButton.setOnAction(e -> handleMax());
        sortAscButton.setOnAction(e -> handleSortAsc());
        sortDescButton.setOnAction(e -> handleSortDesc());
        clearResultsButton.setOnAction(e -> removeResultColumns());
    }

    private void loadColumnsForSelectedTable()
    {
        columnsVBox.getChildren().clear();
        String table = tableComboBox.getValue();
        if (table == null)
        {
            return;
        }

        try
        {
            GenericTableDao dao = new GenericTableDao();
            List<String> cols = dao.getColumns(table);
            for (String c : cols)
            {
                columnsVBox.getChildren().add(new CheckBox(c));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List<String> getSelectedColumns()
    {
        return columnsVBox.getChildren().stream()
                .filter(n -> n instanceof CheckBox cb && cb.isSelected())
                .map(n -> ((CheckBox) n).getText())
                .toList();
    }

    private void handleLoadData()
    {
        String table = tableComboBox.getValue();
        List<String> columns = getSelectedColumns();

        if (table == null || columns.isEmpty())
        {
            return;
        }

        try
        {
            GenericTableDao dao = new GenericTableDao();
            List<Map<String, Object>> data = dao.fetchData(table, columns);

            fillTableView(columns, data);

            autoResizeColumns();

            columnComboBox.getItems().setAll(columns);
            columnComboBox.getSelectionModel().clearSelection();

            avgButton.setDisable(false);
            minButton.setDisable(false);
            maxButton.setDisable(false);
            sortAscButton.setDisable(false);
            sortDescButton.setDisable(false);
        } catch (Exception e)
        {
            showInfo(e.getMessage());
        }
    }

    private void fillTableView(List<String> columns, List<Map<String, Object>> data)
    {
        dataTable.getColumns().clear();

        for (String col : columns)
        {
            TableColumn<Map<String, Object>, Object> tc = new TableColumn<>(col);
            tc.setCellValueFactory(cell
                    -> new SimpleObjectProperty<>(cell.getValue().get(col))
            );
            dataTable.getColumns().add(tc);
        }

        dataTable.getItems().setAll(data);
    }

    private void autoResizeColumns()
    {
        dataTable.getColumns().forEach(column ->
        {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();

            for (int i = 0; i < dataTable.getItems().size(); i++)
            {
                Object cellData = column.getCellData(i);
                if (cellData != null)
                {
                    t = new Text(cellData.toString());
                    double width = t.getLayoutBounds().getWidth();
                    if (width > max)
                    {
                        max = width;
                    }
                }
            }

            column.setPrefWidth(max + 20);
        });
    }

    private void handleAvg()
    {
        calculateAvg();
    }

    private void handleMin()
    {
        calculateMin();
    }

    private void handleMax()
    {
        calculateMax();
    }

    private void handleSortAsc()
    {
        sortTable(true);
    }

    private void handleSortDesc()
    {
        sortTable(false);
    }

    private void showInfo(String msg)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String getSelectedColumnOrWarn()
    {
        String column = columnComboBox.getValue();
        if (column == null)
        {
            showInfo("Wybierz kolumnę w ComboBox po prawej");
            return null;
        }

        if (dataTable.getItems().isEmpty())
        {
            showInfo("Tabela nie zawiera danych");
            return null;
        }

        return column;
    }

    private Double toDouble(Object value)
    {
        if (value instanceof Number n)
        {
            return n.doubleValue();
        }

        try
        {
            return Double.parseDouble(
                    value.toString().trim().replace(",", ".")
            );
        } catch (Exception e)
        {
            return null;
        }
    }

    private void addResultColumn(String columnName, Object value)
    {
        // zapamiętaj wynik
        resultValues.put(columnName, value);

        // sprawdź czy kolumna już istnieje
        for (TableColumn<Map<String, Object>, ?> col : dataTable.getColumns())
        {
            if (col.getText().equals(columnName))
            {
                dataTable.refresh();
                return;
            }
        }

        TableColumn<Map<String, Object>, Object> resultCol
                = new TableColumn<>(columnName);

        resultCol.setCellValueFactory(cell
                -> new SimpleObjectProperty<>(resultValues.get(columnName))
        );

        dataTable.getColumns().add(resultCol);
        dataTable.refresh();
    }

    private void calculateAvg()
    {
        String column = getSelectedColumnOrWarn();
        if (column == null)
        {
            return;
        }

        double sum = 0;
        int count = 0;

        for (Map<String, Object> row : dataTable.getItems())
        {
            Object value = row.get(column);
            if (value == null)
            {
                continue;
            }

            Double num = toDouble(value);
            if (num == null)
            {
                continue;
            }

            sum += num;
            count++;
        }

        if (count == 0)
        {
            return;
        }

        double avg = sum / count;
        addResultColumn("AVG_" + column, avg);
    }

    private void calculateMin()
    {
        String column = getSelectedColumnOrWarn();
        if (column == null)
        {
            return;
        }

        Double min = null;

        for (Map<String, Object> row : dataTable.getItems())
        {
            Object value = row.get(column);
            if (value == null)
            {
                continue;
            }

            Double num = toDouble(value);
            if (num == null)
            {
                continue;
            }

            if (min == null || num < min)
            {
                min = num;
            }
        }

        if (min == null)
        {
            return;
        }

        addResultColumn("MIN_" + column, min);
    }

    private void calculateMax()
    {
        String column = getSelectedColumnOrWarn();
        if (column == null)
        {
            return;
        }

        Double max = null;

        for (Map<String, Object> row : dataTable.getItems())
        {
            Object value = row.get(column);
            if (value == null)
            {
                continue;
            }

            Double num = toDouble(value);
            if (num == null)
            {
                continue;
            }

            if (max == null || num > max)
            {
                max = num;
            }
        }

        if (max == null)
        {
            return;
        }

        addResultColumn("MAX_" + column, max);
    }

    private void sortTable(boolean ascending)
    {
        String columnName = columnComboBox.getValue();
        if (columnName == null)
        {
            showInfo("Wybierz kolumnę do sortowania");
            return;
        }

        TableColumn<Map<String, Object>, ?> targetColumn = null;

        for (TableColumn<Map<String, Object>, ?> col : dataTable.getColumns())
        {
            if (col.getText().equals(columnName))
            {
                targetColumn = col;
                break;
            }
        }

        if (targetColumn == null)
        {
            showInfo("Nie znaleziono kolumny: " + columnName);
            return;
        }

        targetColumn.setSortType(
                ascending
                        ? TableColumn.SortType.ASCENDING
                        : TableColumn.SortType.DESCENDING
        );

        dataTable.getSortOrder().clear();
        dataTable.getSortOrder().add(targetColumn);

        dataTable.sort();
    }

    private void removeResultColumns()
    {
        dataTable.getColumns().removeIf(col
                -> col.getText().startsWith("AVG_")
                || col.getText().startsWith("MIN_")
                || col.getText().startsWith("MAX_")
        );

        resultValues.clear();
        dataTable.refresh();
    }

}
