package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;

public class scooter extends interfazGeneral {
    private JSpinner spinAño;
    private JSpinner spinKilom;
    private JSpinner spinFecha;
    private JSpinner.DateEditor de;

    public scooter() {
        super("CRUD Scooter Interface", new String[]{"Año", "Kilometraje", "Fecha de Abastecimiento"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarComponentes();
        cargarDatos();
        tablaNombre="scooter";
        PK="COD_SCO";
        columns=5;
    }

    private void cargarComponentes() {
        // Año Scooter
        spinAño = new JSpinner(new SpinnerNumberModel(2024, 1900, 2100, 1));
        addExtraComponent(0, spinAño);

        // Kilometraje
        spinKilom = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1000));
        addExtraComponent(1, spinKilom);

        // Fecha de Abastecimiento
        spinFecha = new JSpinner(new SpinnerDateModel());
        de = new JSpinner.DateEditor(spinFecha, "dd/MM/yyyy");
        spinFecha.setEditor(de);
        addExtraComponent(2, spinFecha);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_SCO, AÑO_SCO, KILOM, ABAST_SCO, ESTADO FROM scooter")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codSco = rs.getInt("COD_SCO");
                int añoSco = rs.getInt("AÑO_SCO");
                int kilom = rs.getInt("KILOM");
                Date abastSco = rs.getDate("ABAST_SCO");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codSco);
                tableModel.addRow(new Object[]{codSco, añoSco, kilom, abastSco, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int añoSco = (int) spinAño.getValue();
        int kilom = (int) spinKilom.getValue();
        Date abastSco = (Date) spinFecha.getValue();
        String estado = "A";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO scooter (COD_SCO, AÑO_SCO, KILOM, ABAST_SCO, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, generateNextCode("scooter", "COD_SCO"));
            pstmt.setInt(2, añoSco);
            pstmt.setInt(3, kilom);
            pstmt.setDate(4, new java.sql.Date(abastSco.getTime()));
            pstmt.setString(5, estado);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codSco = (int) tableModel.getValueAt(selectedRow, 0);
            int añoSco = (int) tableModel.getValueAt(selectedRow, 1);
            int kilom = (int) tableModel.getValueAt(selectedRow, 2);
            Date abastSco = (Date) tableModel.getValueAt(selectedRow, 3);
            String estado = tableModel.getValueAt(selectedRow, 4).toString();
            
            txtCodigo.setText(codSco+"");
            spinAño.setValue(añoSco);
            spinKilom.setValue(kilom);
            spinFecha.setValue(abastSco);
            lblEstado.setText(estado);

            txtCodigo.setEditable(false);
            spinAño.setEnabled(true);
            spinKilom.setEnabled(true);
            spinFecha.setEnabled(true);

            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    protected void actualizar() {
        int codSco = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
        int añoSco = (int) spinAño.getValue();
        int kilom = (int) spinKilom.getValue();
        Date abastSco = (Date) spinFecha.getValue();
        String estado = lblEstado.getText();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE scooter SET AÑO_SCO = ?, KILOM = ?, ABAST_SCO = ?, ESTADO = ? WHERE COD_SCO = ?")) {
            pstmt.setInt(1, añoSco);
            pstmt.setInt(2, kilom);
            pstmt.setDate(3, new java.sql.Date(abastSco.getTime()));
            pstmt.setString(4, estado);
            pstmt.setInt(5, codSco);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
