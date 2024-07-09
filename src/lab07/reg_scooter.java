package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class reg_scooter extends interfazGeneral {

    private JButton btnFactura;
    private JComboBox<Integer> cbCodFac;
    private JComboBox<Integer> cbCodRep;

    public reg_scooter() {
        super("CRUD Registro Scooter Interface", new String[]{"Kilometraje Inicial", "Kilometraje Final", "Código de Repartidor", "Gasolina Scooter", "Código de Factura"});

        // Crear botón para gestionar la factura
        btnFactura = new JButton("Gestionar Factura");
        cbCodFac = new JComboBox<>();
        cbCodRep = new JComboBox<>();
        cargarCodigosFacturas();
        cargarCodigosRepartidores();

        txtAtributosExtras[2].setLayout(new BorderLayout());
        txtAtributosExtras[2].add(cbCodRep, BorderLayout.CENTER);
        txtAtributosExtras[4].setLayout(new BorderLayout());
        txtAtributosExtras[4].add(cbCodFac, BorderLayout.CENTER);
        txtAtributosExtras[4].add(btnFactura, BorderLayout.EAST);

        btnFactura.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFacturaGasolinera();
            }
        });
    }

    private void cargarCodigosRepartidores() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REP FROM repartidor WHERE ESTADO = 'A'")) {
            cbCodRep.removeAllItems();
            while (rs.next()) {
                int codRep = rs.getInt("COD_REP");
                cbCodRep.addItem(codRep);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los códigos de repartidores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCodigosFacturas() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_FAC FROM factura_gasolinera WHERE ESTADO = 'A'")) {

            cbCodFac.removeAllItems();
            while (rs.next()) {
                int codFac = rs.getInt("COD_FAC");
                cbCodFac.addItem(codFac);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los códigos de facturas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REG_SCO, KIL_INI, KIL_FIN, COD_REP, GAS_SCO, COD_FAC FROM registro_scooter")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codRegSco = rs.getInt("COD_REG_SCO");
                int kilIni = rs.getInt("KIL_INI");
                int kilFin = rs.getInt("KIL_FIN");
                int codRep = rs.getInt("COD_REP");
                int gasSco = rs.getInt("GAS_SCO");
                int codFac = rs.getInt("COD_FAC");

                usedCodes.add(codRegSco);
                tableModel.addRow(new Object[]{codRegSco, kilIni, kilFin, codRep, gasSco, codFac});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codRegSco = generateNextCode("registro_scooter", "COD_REG_SCO");
            int kilIni = Integer.parseInt(txtAtributosExtras[0].getText());
            int kilFin = Integer.parseInt(txtAtributosExtras[1].getText());
            int codRep = (int) cbCodRep.getSelectedItem();
            int gasSco = Integer.parseInt(txtAtributosExtras[3].getText());
            int codFac = (int) cbCodFac.getSelectedItem();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO registro_scooter (COD_REG_SCO, KIL_INI, KIL_FIN, COD_REP, GAS_SCO, COD_FAC) VALUES (?, ?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, codRegSco);
                pstmt.setInt(2, kilIni);
                pstmt.setInt(3, kilFin);
                pstmt.setInt(4, codRep);
                pstmt.setInt(5, gasSco);
                pstmt.setInt(6, codFac);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de registro o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            txtCodigo.setText(String.valueOf(codRegSco));
            txtCodigo.setEditable(false);
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtAtributosExtras[2].setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtAtributosExtras[3].setText(tableModel.getValueAt(selectedRow, 4).toString());
            cbCodFac.setSelectedItem(tableModel.getValueAt(selectedRow, 5));
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM registro_scooter WHERE COD_REG_SCO = ?")) {

                pstmt.setInt(1, codRegSco);
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
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE registro_scooter SET ESTADO = 'I' WHERE COD_REG_SCO = ?")) {

                pstmt.setInt(1, codRegSco);
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
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE registro_scooter SET ESTADO = 'A' WHERE COD_REG_SCO = ?")) {

                pstmt.setInt(1, codRegSco);
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
            int codRegSco = Integer.parseInt(txtCodigo.getText());
            int kilIni = Integer.parseInt(txtAtributosExtras[0].getText());
            int kilFin = Integer.parseInt(txtAtributosExtras[1].getText());
            int codRep = Integer.parseInt(txtAtributosExtras[2].getText());
            int gasSco = Integer.parseInt(txtAtributosExtras[3].getText());
            int codFac = (int) cbCodFac.getSelectedItem();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE registro_scooter SET KIL_INI = ?, KIL_FIN = ?, COD_REP = ?, GAS_SCO = ?, COD_FAC = ? WHERE COD_REG_SCO = ?")) {

                pstmt.setInt(1, kilIni);
                pstmt.setInt(2, kilFin);
                pstmt.setInt(3, codRep);
                pstmt.setInt(4, gasSco);
                pstmt.setInt(5, codFac);
                pstmt.setInt(6, codRegSco);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de registro o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFacturaGasolinera() {
        new facturaGas();
        cargarCodigosFacturas();
    }
}
