package lab07;

import com.mysql.cj.jdbc.result.ResultSetMetaData;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class consultaSimple extends JFrame {

    private JComboBox<String> tablaSelector;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton salirButton;

    // Aquí asumimos que ya tienes una clase DatabaseConnection con la conexión establecida
    private Connection connection = DatabaseConnection.getConnection();

    public consultaSimple() throws SQLException {
        setTitle("Consulta Simple");
        setLayout(new BorderLayout());

        // Crear un panel superior para el JLabel y JComboBox
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        
        JLabel titleLabel = new JLabel("Seleccionar Tabla");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(titleLabel);

        // Crear y configurar el JComboBox con las tablas disponibles
        tablaSelector = new JComboBox<>(new String[]{"pais", "region", "ciudad", "articulo"}); // Aquí deben estar las tablas de tu base de datos
        tablaSelector.addActionListener(e -> {
            String selectedTable = (String) tablaSelector.getSelectedItem();
            if (selectedTable != null) {
                // Ejecutar la consulta y mostrar los resultados
                mostrarDatosTabla(selectedTable);
            }
        });
        topPanel.add(tablaSelector);

        add(topPanel, BorderLayout.NORTH);

        // Crear el JTable y el modelo de tabla
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);

        // Hacer el JTable no editable
        resultTable.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        // Crear el botón de salir
        salirButton = new JButton("Salir");
        salirButton.addActionListener(e -> dispose());

        // Crear un panel inferior para el botón de salir
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(salirButton);
        
        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600); // Ajustar el tamaño del JFrame
        setVisible(true);
    }

    private void mostrarDatosTabla(String tableName) {
        String query = "SELECT * FROM " + tableName + " WHERE ESTADO='A'";
        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            // Limpiar el modelo de tabla antes de mostrar nuevos datos
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // Obtener los metadatos del ResultSet (columnas)
            ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
            int numColumns = metaData.getColumnCount();

            // Agregar los nombres de las columnas al modelo de tabla
            for (int i = 1; i <= numColumns; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            // Iterar sobre cada fila del resultado y agregarla al modelo de tabla
            while (resultSet.next()) {
                Object[] rowData = new Object[numColumns];
                for (int i = 1; i <= numColumns; i++) {
                    rowData[i - 1] = resultSet.getObject(i);
                }
                tableModel.addRow(rowData);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al ejecutar la consulta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
