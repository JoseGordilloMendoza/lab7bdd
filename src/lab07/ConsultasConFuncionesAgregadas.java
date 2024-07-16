package lab07;

import com.mysql.cj.jdbc.result.ResultSetMetaData;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultasConFuncionesAgregadas extends JFrame {

    private JComboBox<String> consultaSelector;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton salirButton;

    // Aquí asumimos que ya tienes una clase DatabaseConnection con la conexión establecida
    private Connection connection = DatabaseConnection.getConnection();

    public ConsultasConFuncionesAgregadas() throws SQLException {
        setTitle("Consultas con Funciones Agregadas");
        setLayout(new BorderLayout());

        // Crear un panel superior para el JComboBox y el botón de salir
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel consultaLabel = new JLabel("Seleccionar Consulta:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(consultaLabel, gbc);

        consultaSelector = new JComboBox<>(new String[]{"Consulta a tamaño_articulo", "Consulta a almacen"});
        consultaSelector.addActionListener(e -> {
            String selectedConsulta = (String) consultaSelector.getSelectedItem();
            if (selectedConsulta != null) {
                // Ejecutar la consulta correspondiente y mostrar los resultados
                if (selectedConsulta.equals("Consulta a tamaño_articulo")) {
                    ejecutarConsultaTamañoArticulo();
                } else if (selectedConsulta.equals("Consulta a almacen")) {
                    ejecutarConsultaAlmacen();
                }
            }
        });
        gbc.gridx = 1;
        topPanel.add(consultaSelector, gbc);

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
        salirButton.addActionListener(e -> dispose());
        bottomPanel.add(salirButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    private void ejecutarConsultaTamañoArticulo() {
    String query = "SELECT " +
                   "    ta.COD_TIP_ART, " +
                   "    ta.CAT AS Categoria, " +
                   "    COUNT(a.ART_COD) AS Total_Articulos, " +
                   "    AVG(a.PREC_ART) AS Precio_Promedio " +
                   "FROM " +
                   "    articulo a " +
                   "JOIN " +
                   "    tipo_de_articulo ta ON a.COD_TIP_ART = ta.COD_TIP_ART " +
                   "WHERE " +
                   "    a.ESTADO = 'A' AND ta.ESTADO = 'A' " +
                   "GROUP BY " +
                   "    ta.COD_TIP_ART, ta.CAT " +
                   "HAVING " +
                   "    COUNT(a.ART_COD) > 1 " +
                   "ORDER BY " +
                   "    Precio_Promedio DESC";

    ejecutarConsulta(query);
}


    private void ejecutarConsultaAlmacen() {
        String query = "SELECT \n" +
            "    a.COD_ALM,\n" +
            "    a.COD_FRAN AS Codigo_Franquicia,\n" +
            "    COUNT(ia.ING_ID) AS Total_Ingredientes,\n" +
            "    SUM(ia.STO_ACT) AS Stock_Total_Actual,\n" +
            "    MAX(ia.STO_ACT) AS Stock_Maximo_Ingrediente,\n" +
            "    MIN(ia.STO_ACT) AS Stock_Minimo_Ingrediente,\n" +
            "    AVG(ia.STO_ACT) AS Promedio_Stock_Actual,\n" +
            "    SUM(CASE WHEN ia.STO_ACT < ia.STO_MIN THEN 1 ELSE 0 END) AS Ingredientes_Bajo_Minimo\n" +
            "FROM \n" +
            "    almacen a\n" +
            "LEFT JOIN \n" +
            "    ingrediente_almacen ia ON a.COD_ALM = ia.COD_ALM\n" +
            "WHERE \n" +
            "    a.ESTADO = 'A' AND (ia.ESTADO = 'A' OR ia.ESTADO IS NULL)\n" +
            "GROUP BY \n" +
            "    a.COD_ALM, a.COD_FRAN\n" +
            "HAVING \n" +
            "    COUNT(ia.ING_ID) > 0\n" +
            "ORDER BY \n" +
            "    Stock_Total_Actual DESC, Total_Ingredientes DESC;";

        ejecutarConsulta(query);
    }

    private void ejecutarConsulta(String query) {
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

    public static void main(String[] args) {
        try {
            new ConsultasConFuncionesAgregadas();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
