package lab07;

import com.mysql.cj.jdbc.result.ResultSetMetaData;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class ConsultaCombinada extends JFrame {

    private JComboBox<String> tabla1Selector;
    private JComboBox<String> tabla2Selector;
    private JComboBox<String> campo1Selector;
    private JComboBox<String> campo2Selector;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton consultarButton;
    private JButton salirButton;

    // Aquí asumimos que ya tienes una clase DatabaseConnection con la conexión establecida
    private Connection connection = DatabaseConnection.getConnection();

    public ConsultaCombinada() throws SQLException {
        setTitle("Consulta Combinada");
        setLayout(new BorderLayout());

        // Crear un panel superior para los JComboBox y el botón de consultar
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel tabla1Label = new JLabel("Probando con Tabla 1:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(tabla1Label, gbc);

        tabla1Selector = new JComboBox<>(new String[]{"pais"}); // Aquí deben estar las tablas de tu base de datos
        gbc.gridx = 1;
        topPanel.add(tabla1Selector, gbc);

        JLabel campo1Label = new JLabel("Probando con campo Tabla 1:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(campo1Label, gbc);

        campo1Selector = new JComboBox<>(new String[]{"COD_PAI"}); // Aquí deben estar los campos de tu base de datos
        gbc.gridx = 1;
        topPanel.add(campo1Selector, gbc);

        JLabel tabla2Label = new JLabel("Probando con Tabla 2:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(tabla2Label, gbc);

        tabla2Selector = new JComboBox<>(new String[]{"region"}); // Aquí deben estar las tablas de tu base de datos
        gbc.gridx = 1;
        topPanel.add(tabla2Selector, gbc);

        JLabel campo2Label = new JLabel("Probando con campo Tabla 2:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(campo2Label, gbc);

        campo2Selector = new JComboBox<>(new String[]{"COD_PAI"}); // Aquí deben estar los campos de tu base de datos
        gbc.gridx = 1;
        topPanel.add(campo2Selector, gbc);

        consultarButton = new JButton("Consultar");
        consultarButton.addActionListener(e -> {
            String tabla1 = (String) tabla1Selector.getSelectedItem();
            String tabla2 = (String) tabla2Selector.getSelectedItem();
            String campo1 = (String) campo1Selector.getSelectedItem();
            String campo2 = (String) campo2Selector.getSelectedItem();
            if (tabla1 != null && tabla2 != null && campo1 != null && campo2 != null) {
                // Ejecutar la consulta combinada y mostrar los resultados
                mostrarDatosCombinados(tabla1, tabla2, campo1, campo2);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        topPanel.add(consultarButton, gbc);

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

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(salirButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600); // Ajustar el tamaño del JFrame
        setVisible(true);
    }

    private void mostrarDatosCombinados(String tabla1, String tabla2, String campo1, String campo2) {
        String query = "SELECT "
                + "CONCAT(t1." + campo1 + ", ' - ', t2." + campo2 + ") AS ID_Combinado, "
                + "t1.*, "
                + "t2.*, "
                + "CASE WHEN t1.ESTADO = 'A' AND t2.ESTADO = 'A' THEN 'Ambos Activos' ELSE 'Revisar Estados' END AS Estado_Combinado "
                + "FROM " + tabla1 + " t1 "
                + "JOIN " + tabla2 + " t2 ON t1." + campo1 + " = t2." + campo2 + " "
                + "WHERE t1.ESTADO='A' AND t2.ESTADO='A'";

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

    public static void main(String[] args) {
        try {
            new ConsultaCombinada();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
