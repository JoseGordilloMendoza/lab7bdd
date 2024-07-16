package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.io.FileNotFoundException;


public class ReportesApp extends JFrame {

    private JComboBox<String> comboReportes;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnSalir;
    private JButton btnGenerarCSV;
    private JButton btnGenerarPDF;


    public ReportesApp() {
        setTitle("Generador de Reportes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        addListeners();
        
    }

    private void initComponents() {
        comboReportes = new JComboBox<>(new String[]{"Reporte de Inventario Almacen", "Reporte Ventas 2024 JULIO"});
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setShowGrid(true); // Mostrar líneas de la tabla para mejor visibilidad
        btnSalir = new JButton("Salir");
        btnGenerarCSV = new JButton("Generar CSV");
        btnGenerarPDF = new JButton("Generar PDF");

    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel panelNorth = new JPanel();
        panelNorth.add(new JLabel("Seleccione el Reporte:"));
        panelNorth.add(comboReportes);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelSouth = new JPanel();
        panelSouth.add(btnGenerarCSV);
        panelSouth.add(btnSalir);
        panelSouth.add(btnGenerarPDF);


        add(panelNorth, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelSouth, BorderLayout.SOUTH);
    }

    private void addListeners() {
        comboReportes.addActionListener(e -> cargarReporte());
        btnSalir.addActionListener(e -> System.exit(0));
        btnGenerarCSV.addActionListener(e -> generarCSV());
        btnGenerarPDF.addActionListener(e -> generarPDF());

    }

    private void cargarReporte() {
        String reporteSeleccionado = (String) comboReportes.getSelectedItem();
        if ("Reporte de Inventario Almacen".equals(reporteSeleccionado)) {
            cargarReporteInventarioAlmacen();
        } else if ("Reporte de Ventas Mensuales".equals(reporteSeleccionado)) {
            cargarReporteVentasMensuales();
        }
    }

    private void cargarReporteInventarioAlmacen() {
        String query = "{CALL reporte_inventario_almacenes()}";
        cargarReporteDesdeDB(query);
    }

    private void cargarReporteVentasMensuales() {
        String query = "{CALL reporte_pedidos_mensuales(?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(query)) {

            stmt.setInt(1, 2024); // Año
            stmt.setInt(2, 7);    // Mes (Julio)

            System.out.println("Ejecutando consulta: " + query);
            System.out.println("Parámetros: Año = 2024, Mes = 7");

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
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(query)) {

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