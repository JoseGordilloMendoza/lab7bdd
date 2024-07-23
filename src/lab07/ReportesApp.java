package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.toedter.calendar.JDateChooser;
import java.io.FileNotFoundException;
import java.math.BigDecimal;


public class ReportesApp extends JFrame {
   
    
    private JComboBox<String> comboAlmacenes;
    private JLabel labelAnio;
        private JLabel labelMes;
        private JLabel labelAlmacen;
    private JComboBox<String> comboReportes;
    private JComboBox<Integer> comboAnio;
    private JComboBox<Integer> comboMes;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnSalir;
    private JButton btnGenerarCSV;
    private JButton btnGenerarPDF;
    private JButton btnEnviar;
    private JButton btnGenerarBoleta;

    public ReportesApp() {
        setTitle("Generador de Reportes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setVisible(true);

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        comboReportes = new JComboBox<>(new String[]{
                "Reporte de Inventario Almacen",
                "Reporte Ventas Mensuales",
                "Reporte de Pedidos por Procedencia",
                "Generar Boleta" // Nueva opción
        });
        
        btnGenerarBoleta = new JButton("Generar Boleta");
        comboAnio = new JComboBox<>();
        for (int year = 2020; year <= 2025; year++) {
            comboAnio.addItem(year);
        }

        comboMes = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            comboMes.addItem(month);
        }

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setShowGrid(true); // Mostrar líneas de la tabla para mejor visibilidad
        btnSalir = new JButton("Salir");
        btnGenerarCSV = new JButton("Generar CSV");
        btnGenerarPDF = new JButton("Generar PDF");
        btnEnviar = new JButton("Enviar");

        // Inicializar los JLabels
        labelAnio = new JLabel("Año:");
        labelMes = new JLabel("Mes:");
        labelAlmacen = new JLabel("Seleccione el Almacén:");

        comboAlmacenes = new JComboBox<>();
        cargarAlmacenes();

        // Inicialmente ocultamos los selectores de fecha y el botón de enviar
        comboAnio.setVisible(false);
        comboMes.setVisible(false);
        btnEnviar.setVisible(false);
        comboAlmacenes.setVisible(false);
        labelAnio.setVisible(false);
        labelMes.setVisible(false);
        labelAlmacen.setVisible(false);
        
        
    }


    
    private void cargarAlmacenes() {
        List<String> almacenes = obtenerAlmacenes();
        for (String almacen : almacenes) {
            comboAlmacenes.addItem(almacen);
        }
    }

    
    private List<String> obtenerAlmacenes() {
        List<String> almacenes = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eatngo", "root", "admin")) {
            String query = "SELECT codigo_almacen, nombre_localidad FROM vista_almacenes";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String almacen = rs.getString("codigo_almacen") + " / " + rs.getString("nombre_localidad");
                    almacenes.add(almacen);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return almacenes;
    }

    
    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new GridLayout(5, 2, 10, 10)); // Añadir GridLayout para disposición en rejilla

        panelNorth.add(new JLabel("Seleccione el Reporte:"));
        panelNorth.add(comboReportes);

        // Añadir los componentes condicionales en el orden correcto
        panelNorth.add(labelAlmacen);
        panelNorth.add(comboAlmacenes);

        panelNorth.add(labelAnio);
        panelNorth.add(comboAnio);

        panelNorth.add(labelMes);
        panelNorth.add(comboMes);

        // Añadir el botón de enviar
        panelNorth.add(new JLabel()); // Espacio vacío para alinear el botón
        panelNorth.add(btnEnviar);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelSouth = new JPanel();
        panelSouth.add(btnGenerarCSV);
        panelSouth.add(btnSalir);
        panelSouth.add(btnGenerarPDF);

        add(panelNorth, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelSouth, BorderLayout.SOUTH);
        
        panelNorth.add(btnGenerarBoleta); 
        btnGenerarBoleta.setVisible(false);
    }


    private void addListeners() {
        comboReportes.addActionListener(e -> toggleDateSelectors());
        btnSalir.addActionListener(e -> System.exit(0));
        btnGenerarCSV.addActionListener(e -> generarCSV());
        btnGenerarPDF.addActionListener(e -> generarPDF());
        btnEnviar.addActionListener(e -> cargarReporte());

        comboAlmacenes.addActionListener(e -> {
            if ("Reporte de Inventario Almacen".equals(comboReportes.getSelectedItem())) {
                cargarReporte();
            }
        });
        
        btnGenerarBoleta.addActionListener(e -> generarBoleta()); // Listener para el botón Generar Boleta

    }



    private void toggleDateSelectors() {
        String selectedReport = (String) comboReportes.getSelectedItem();
        boolean isVentasMensuales = "Reporte Ventas Mensuales".equals(selectedReport);
        boolean isInventarioAlmacen = "Reporte de Inventario Almacen".equals(selectedReport);
        boolean isPedidosPorProcedencia = "Reporte de Pedidos por Procedencia".equals(selectedReport);
        
        boolean isGenerarBoleta = "Generar Boleta".equals(selectedReport);

// Añadir esta línea al final del método
         // Mostrar botón Generar Boleta solo si se selecciona esa opción

        
        
        comboAnio.setVisible(isVentasMensuales);
        labelAnio.setVisible(isVentasMensuales);
        comboMes.setVisible(isVentasMensuales);
        labelMes.setVisible(isVentasMensuales);

        comboAlmacenes.setVisible(isInventarioAlmacen);
        labelAlmacen.setVisible(isInventarioAlmacen);

        btnEnviar.setVisible(isVentasMensuales || isInventarioAlmacen || isPedidosPorProcedencia);
        btnGenerarBoleta.setVisible(isGenerarBoleta);
    }

    private void generarBoleta() {
        String input = JOptionPane.showInputDialog(this, "Ingrese el código de la boleta:", "Generar Boleta", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                BigDecimal codBol = new BigDecimal(input.trim());
                String query = "{CALL generar_boleta(?)}";
                try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {
                    stmt.setBigDecimal(1, codBol);

                    boolean hasResults = stmt.execute();
                    int resultSetCount = 0;

                    while (hasResults) {
                        resultSetCount++;
                        try (ResultSet rs = stmt.getResultSet()) {
                            cargarResultadosEnTabla(rs, resultSetCount);
                        }

                        hasResults = stmt.getMoreResults();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al generar la boleta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de boleta inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarResultadosEnTabla(ResultSet rs, int resultSetCount) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Limpiar modelo de tabla solo para el primer ResultSet
        if (resultSetCount == 1) {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
        }

        // Agregar nombres de columnas solo para el primer ResultSet
        if (resultSetCount == 1) {
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnLabel(i));
            }
        }

        // Agregar filas a la tabla
        while (rs.next()) {
            int columnCount = metaData.getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }
    }




    private void cargarReportePedidosPorProcedencia() {
        String query = "{CALL obtener_reporte_pedidos_por_procedencia()}";
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {
            cargarResultadosEnTabla(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void cargarReporte() {
        String reporteSeleccionado = (String) comboReportes.getSelectedItem();
        if ("Reporte de Inventario Almacen".equals(reporteSeleccionado)) {
            String seleccionado = (String) comboAlmacenes.getSelectedItem();
            if (seleccionado != null) {
                String[] partes = seleccionado.split(" / ");
                if (partes.length > 0) {
                    String codigoAlmacen = partes[0];
                    cargarReporteInventarioAlmacen(codigoAlmacen);
                }
            }
        } else if ("Reporte Ventas Mensuales".equals(reporteSeleccionado)) {
            cargarReporteVentasMensuales();
        } else if ("Reporte de Pedidos por Procedencia".equals(reporteSeleccionado)) {
            cargarReportePedidosPorProcedencia();
        }
    }

    private void cargarReporteInventarioAlmacen(String codigoAlmacen) {
        String query = "{CALL MostrarEstadoAlmacen(?)}";
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {
            stmt.setBigDecimal(1, new BigDecimal(codigoAlmacen));
            cargarResultadosEnTabla(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarReporteVentasMensuales() {
        int anioSeleccionado = (int) comboAnio.getSelectedItem();
        int mesSeleccionado = (int) comboMes.getSelectedItem();

        String query = "{CALL reporte_pedidos_mensuales(?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {

            stmt.setInt(1, anioSeleccionado); // Año
            stmt.setInt(2, mesSeleccionado);  // Mes

            System.out.println("Ejecutando consulta: " + query);
            System.out.println("Parámetros: Año = " + anioSeleccionado + ", Mes = " + mesSeleccionado);

            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No se encontraron resultados");
            } else {
                System.out.println("Se encontraron resultados");
            }

            cargarResultadosEnTabla(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarReporteDesdeDB(String query) {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {

            cargarResultadosEnTabla(stmt);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarResultadosEnTabla(CallableStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();

            // Limpiar modelo de tabla
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // Agregar nombres de columnas
            int columnCount = metaData.getColumnCount();
            System.out.println("Número de columnas: " + columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                System.out.println("Columna " + i + ": " + columnName);
                tableModel.addColumn(columnName);
            }

            // Agregar filas a la tabla
            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(row);
                rowCount++;
            }
            System.out.println("Número de filas agregadas: " + rowCount);
        }
    }
    
    private void generarCSV() {
        String reporteSeleccionado = (String) comboReportes.getSelectedItem();
        String fileName = reporteSeleccionado.replace(" ", "_") + ".csv";

        try (FileWriter csvWriter = new FileWriter(fileName)) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                csvWriter.append(escapeCSV(tableModel.getColumnName(i)));
                if (i < tableModel.getColumnCount() - 1) {
                    csvWriter.append(",");
                }
            }
            csvWriter.append("\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    csvWriter.append(escapeCSV(tableModel.getValueAt(i, j).toString()));
                    if (j < tableModel.getColumnCount() - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
            }

            JOptionPane.showMessageDialog(this, "Reporte guardado como " + fileName, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void generarPDF() {
        String reporteSeleccionado = (String) comboReportes.getSelectedItem();
        String fileName = reporteSeleccionado.replace(" ", "_") + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Agregar el título del reporte
            document.add(new Paragraph(reporteSeleccionado).setFontSize(12).setTextAlignment(TextAlignment.CENTER));

            // Definir el ancho de las columnas (ajustar según el número de columnas)
            float[] columnWidths = new float[tableModel.getColumnCount()];
            for (int i = 0; i < columnWidths.length; i++) {
                columnWidths[i] = 1;
            }

            Table pdfTable = new Table(UnitValue.createPercentArray(columnWidths));
            pdfTable.setWidth(UnitValue.createPercentValue(100));

            // Agregar nombres de columnas
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                pdfTable.addHeaderCell(new Cell().add(new Paragraph(tableModel.getColumnName(i)).setBold()));
            }

            // Agregar filas a la tabla
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    pdfTable.addCell(new Cell().add(new Paragraph(tableModel.getValueAt(i, j).toString())));
                }
            }

            // Añadir la tabla al documento
            document.add(pdfTable);

            // Cerrar el documento
            document.close();

            JOptionPane.showMessageDialog(this, "Reporte guardado como " + fileName, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ocurrió un error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}