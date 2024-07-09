package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class boleta extends interfazGeneral {

    private JComboBox<Integer> cbCodRegSco;
    private JComboBox<Integer> cbPedId;
    private SimpleDateFormat dateFormat;

    public boleta() {
        super("CRUD Boleta Interface", new String[]{"Fecha Boleta", "Código Registro Scooter", "Pedido ID", "Estado"});

        cbCodRegSco = new JComboBox<>();
        cbPedId = new JComboBox<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        cargarCodigosRegistroScooter();
        cargarCodigosPedidosActivos();
        establecerFechaHoy();

        txtAtributosExtras[1].setLayout(new BorderLayout());
        txtAtributosExtras[1].add(cbCodRegSco, BorderLayout.CENTER);

        txtAtributosExtras[2].setLayout(new BorderLayout());
        txtAtributosExtras[2].add(cbPedId, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarCodigosRegistroScooter() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REG_SCO FROM registro_scooter WHERE ESTADO = 'A'")) {
            cbCodRegSco.removeAllItems();
            while (rs.next()) {
                int codRegSco = rs.getInt("COD_REG_SCO");
                cbCodRegSco.addItem(codRegSco);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los códigos de registro scooter: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCodigosPedidosActivos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT PED_ID FROM pedido_base WHERE ESTADO = 'A'")) {
            cbPedId.removeAllItems();
            while (rs.next()) {
                int pedId = rs.getInt("PED_ID");
                cbPedId.addItem(pedId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los códigos de pedidos activos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void establecerFechaHoy() {
        String fechaHoy = dateFormat.format(new Date());
        txtAtributosExtras[0].setText(fechaHoy);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_BOL, FECH_BOL, COD_REG_SCO, PED_ID, ESTADO FROM boleta")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codBol = rs.getInt("COD_BOL");
                Date fechBol = rs.getDate("FECH_BOL");
                int codRegSco = rs.getInt("COD_REG_SCO");
                int pedId = rs.getInt("PED_ID");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codBol);
                tableModel.addRow(new Object[]{codBol, dateFormat.format(fechBol), codRegSco, pedId, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codBol = generateNextCode("boleta", "COD_BOL");
            Date fechBol = new SimpleDateFormat("yyyy-MM-dd").parse(txtAtributosExtras[0].getText());
            int codRegSco = (int) cbCodRegSco.getSelectedItem();
            int pedId = (int) cbPedId.getSelectedItem();
            String estado = txtAtributosExtras[3].getText();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO boleta (COD_BOL, FECH_BOL, COD_REG_SCO, PED_ID, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, codBol);
                pstmt.setDate(2, new java.sql.Date(fechBol.getTime()));
                pstmt.setInt(3, codRegSco);
                pstmt.setInt(4, pedId);
                pstmt.setString(5, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Código de boleta o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codBol = (int) tableModel.getValueAt(selectedRow, 0);
            txtCodigo.setText(String.valueOf(codBol));
            txtCodigo.setEditable(false);
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            cbCodRegSco.setSelectedItem(tableModel.getValueAt(selectedRow, 2));
            cbPedId.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
            txtAtributosExtras[3].setText(tableModel.getValueAt(selectedRow, 4).toString());
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codBol = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM boleta WHERE COD_BOL = ?")) {
                pstmt.setInt(1, codBol);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codBol = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE boleta SET ESTADO = 'I' WHERE COD_BOL = ?")) {
                pstmt.setInt(1, codBol);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codBol = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE boleta SET ESTADO = 'A' WHERE COD_BOL = ?")) {
                pstmt.setInt(1, codBol);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codBol = Integer.parseInt(txtCodigo.getText());
            Date fechBol = new SimpleDateFormat("yyyy-MM-dd").parse(txtAtributosExtras[0].getText());
            int codRegSco = (int) cbCodRegSco.getSelectedItem();
            int pedId = (int) cbPedId.getSelectedItem();
            String estado = txtAtributosExtras[3].getText();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE boleta SET FECH_BOL = ?, COD_REG_SCO = ?, PED_ID = ?, ESTADO = ? WHERE COD_BOL = ?")) {
                pstmt.setDate(1, new java.sql.Date(fechBol.getTime()));
                pstmt.setInt(2, codRegSco);
                pstmt.setInt(3, pedId);
                pstmt.setString(4, estado);
                pstmt.setInt(5, codBol);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Código de boleta o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
