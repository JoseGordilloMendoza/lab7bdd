package lab07;

import com.mysql.cj.jdbc.result.ResultSetMetaData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class vistasDB extends JFrame {

    private JComboBox<String> vistaSelector;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton salirButton;

    private Connection connection = DatabaseConnection.getConnection();

    public vistasDB() throws SQLException {
        setTitle("Vistas de Base de Datos");
        setLayout(new BorderLayout());

        // Crear un panel superior para el JComboBox y el botón de salir
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel vistaLabel = new JLabel("Seleccionar Vista:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(vistaLabel, gbc);

        vistaSelector = new JComboBox<>(new String[]{"vista_articulo", "vista_repartidor", "vista_costo_repartidor", "vista_almacenes"}); // Aquí deben estar las vistas de tu base de datos
        vistaSelector.addActionListener(e -> {
            String selectedVista = (String) vistaSelector.getSelectedItem();
            if (selectedVista != null) {
                // Ejecutar la consulta y mostrar los resultados
                mostrarDatosVista(selectedVista);
            }
        });
        gbc.gridx = 1;
        topPanel.add(vistaSelector, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Crear el JTable y el modelo de tabla
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);

        // Hacer el JTable no editable
        resultTable.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        // Crear un panel inferior para el botón de salir
        JPanel bottomPanel = new JPanel();
        salirButton = new JButton("Salir");
        salirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual
                new home(); // Crea una nueva instancia de la ventana Home
            }
        });
        bottomPanel.add(salirButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setVisible(true);
    }

    private void mostrarDatosVista(String vistaName) {
        String query = "SELECT * FROM " + vistaName;
        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
            int numColumns = metaData.getColumnCount();

            for (int i = 1; i <= numColumns; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }
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
