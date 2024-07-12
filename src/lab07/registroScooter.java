package lab07;

import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class registroScooter extends interfazGeneral {

    private JComboBox<String> comboRepartidor;
    private JSpinner spinKilometrajeInicio;
    private JSpinner spinKilometrajeFin;
    private JComboBox<String> comboFactura;

    public registroScooter() {
        super("Registro de Scooters", new String[]{"Repartidor", "Kilometraje Inicio", "Kilometraje Fin", "Factura"});
                table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarDatosExtra();
        tablaNombre="articulo";
        PK="ART_COD";
        columns=6;
    }

    private void cargarDatosExtra() {
        comboRepartidor = new JComboBox<>();
        comboFactura = new JComboBox<>();

        // Configurar JSpinner para Kilometraje Inicio
        spinKilometrajeInicio = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        JSpinner.DefaultEditor spinEditorInicio = (JSpinner.DefaultEditor) spinKilometrajeInicio.getEditor();
        spinEditorInicio.getTextField().setEditable(true);

        // Configurar JSpinner para Kilometraje Fin
        spinKilometrajeFin = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        JSpinner.DefaultEditor spinEditorFin = (JSpinner.DefaultEditor) spinKilometrajeFin.getEditor();
        spinEditorFin.getTextField().setEditable(true);

        // Cargar datos de Repartidor en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_REP, NOM_REP FROM repartidor WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboRepartidor.addItem(rs.getString("COD_REP") + " / " + rs.getString("NOM_REP"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Factura en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_FAC, COST_TOT FROM factura_gasolinera WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboFactura.addItem(rs.getString("COD_FAC") + " / " + "S/ "+rs.getString("COST_TOT"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Agregar componentes a los paneles correspondientes
        addExtraComponent(0, comboRepartidor);
        addExtraComponent(1, spinKilometrajeInicio);
        addExtraComponent(2, spinKilometrajeFin);
        addExtraComponent(3, comboFactura);
    }

    @Override
protected void cargarDatos() {
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM registro_scooter")) {
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar datos
        while (rs.next()) {
            // Obtener datos del repartidor usando su código
            int codRep = rs.getInt("COD_REP");
            String nomRep = getRepartidorNombre(conn, codRep);

            // Obtener datos de la factura usando su código
            int codFac = rs.getInt("COD_FAC");
            double costTot = getCostoTotalFactura(conn, codFac);

            tableModel.addRow(new Object[]{
                rs.getInt("COD_REG_SCO"),
                rs.getInt("KIL_INI"),
                rs.getInt("KIL_FIN"),
                codRep + " / " + nomRep, // Concatenar COD_REP con NOM_REP
                codFac + " / " + costTot, // Concatenar COD_FAC con COST_TOT
                rs.getString("ESTADO"),
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private double getCostoTotalFactura(Connection conn, int codFac) throws SQLException {
    double costoTotal = 0.0;
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COST_TOT FROM factura_gasolinera WHERE COD_FAC = " + codFac)) {
        if (rs.next()) {
            costoTotal = rs.getDouble("COST_TOT");
        }
    }
    return costoTotal;
}

    private String getRepartidorNombre(Connection conn, int codRep) throws SQLException {
        String nombre = "";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NOM_REP FROM repartidor WHERE COD_REP = " + codRep)) {
            if (rs.next()) {
                nombre = rs.getString("NOM_REP");
            }
        }
        return nombre;
    }

    @Override
    protected void adicionar() {
        int codigo = generateNextCode("registro_scooter", "COD_REG_SCO");
        int kilInicio = (int) spinKilometrajeInicio.getValue();
        int kilFin = (int) spinKilometrajeFin.getValue();
        int codRep = Integer.parseInt(((String) comboRepartidor.getSelectedItem()).split(" / ")[0]);
        int codFac = Integer.parseInt(((String) comboFactura.getSelectedItem()).split(" / ")[0]);
        String estado = "A";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO registro_scooter (COD_REG_SCO, KIL_INI, KIL_FIN, COD_REP, COD_FAC, ESTADO) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, codigo);
            pstmt.setInt(2, kilInicio);
            pstmt.setInt(3, kilFin);
            pstmt.setInt(4, codRep);
            pstmt.setInt(5, codFac);
            pstmt.setString(6, estado);
            pstmt.executeUpdate();
            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
                    String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);

            if (!estado.equals("*")&& estado.equals("A")) {
                spinKilometrajeInicio.setValue(tableModel.getValueAt(selectedRow, 1));
                spinKilometrajeFin.setValue(tableModel.getValueAt(selectedRow, 2));
                txtCodigo.setText(String.valueOf(codRegSco));
                txtCodigo.setEditable(false);

                int codRep = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 3)).split(" / ")[0]);
                comboRepartidor.setSelectedItem(String.valueOf(codRep));

                int codFac = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 4)).split(" / ")[0]);
                comboFactura.setSelectedItem(String.valueOf(codFac));

                lblEstado.setText(estado);
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este registro no puede ser modificado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tableModel.getValueAt(selectedRow, 0);
            int kilInicio = (int) spinKilometrajeInicio.getValue();
            int kilFin = (int) spinKilometrajeFin.getValue();
            int codRep = Integer.parseInt(((String) comboRepartidor.getSelectedItem()).split(" / ")[0]);
            int codFac = Integer.parseInt(((String) comboFactura.getSelectedItem()).split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE registro_scooter SET KIL_INI = ?, KIL_FIN = ?, COD_REP = ?, COD_FAC = ?, ESTADO = ? WHERE COD_REG_SCO = ?")) {
                pstmt.setInt(1, kilInicio);
                pstmt.setInt(2, kilFin);
                pstmt.setInt(3, codRep);
                pstmt.setInt(4, codFac);
                pstmt.setString(5, estado);
                pstmt.setInt(6, codigo);
                pstmt.executeUpdate();
                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
