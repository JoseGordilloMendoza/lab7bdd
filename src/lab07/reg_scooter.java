package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class reg_scooter extends interfazGeneral {

    private JComboBox<Integer> cbCodFac;
    private JComboBox<Integer> cbCodRep;

    public reg_scooter() {
        super("CRUD Registro Scooter Interface", new String[]{"Kilometraje Inicial", "Kilometraje Final", "Código de Repartidor", "Gasolina Scooter", "Código de Factura"});

        // Crear combo boxes y cargar datos
        cbCodFac = new JComboBox<>();
        cbCodRep = new JComboBox<>();
        cargarCodigosFacturas();
        cargarCodigosRepartidores();

        // Configurar paneles extras
        addExtraComponent(2, cbCodRep);
        addExtraComponent(4, cbCodFac);

        // Acción del botón para gestionar factura
        JButton btnFactura = new JButton("Gestionar Factura");
        btnFactura.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFacturaGasolinera();
            }
        });
        addExtraComponent(4, btnFactura);
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
            int kilIni = Integer.parseInt(getTextFromPanel(0));
            int kilFin = Integer.parseInt(getTextFromPanel(1));
            int codRep = (int) cbCodRep.getSelectedItem();
            int gasSco = Integer.parseInt(getTextFromPanel(3));
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
            JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            txtCodigo.setText(String.valueOf(codRegSco));
            txtCodigo.setEditable(false);
            setTextInPanel(0, tableModel.getValueAt(selectedRow, 1).toString());
            setTextInPanel(1, tableModel.getValueAt(selectedRow, 2).toString());
            setTextInPanel(2, tableModel.getValueAt(selectedRow, 3).toString());
            setTextInPanel(3, tableModel.getValueAt(selectedRow, 4).toString());
            cbCodFac.setSelectedItem(tableModel.getValueAt(selectedRow, 5));
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Registro no puede modificarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET ESTADO = '*' WHERE COD_PAI = ?")) {

                pstmt.setInt(1, codRegSco);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser eliminado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            lblEstado.setText("I");
            cargarDatos();
            cancelar();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para inactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("I")) {
            int codRegSco = (int) tableModel.getValueAt(selectedRow, 0);
            lblEstado.setText("A");
            cargarDatos();
            cancelar();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para reactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codRegSco = Integer.parseInt(txtCodigo.getText());
            int kilIni = Integer.parseInt(getTextFromPanel(0));
            int kilFin = Integer.parseInt(getTextFromPanel(1));
            int codRep = Integer.parseInt(getTextFromPanel(2));
            int gasSco = Integer.parseInt(getTextFromPanel(3));
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
            JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            JOptionPane.showMessageDialog(this, "Error al cargar los cód");
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
            JOptionPane.showMessageDialog(this, "Error al cargar los códigos de factura: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        cbCodRep.setSelectedIndex(0);
        cbCodFac.setSelectedIndex(0);
    }

    @Override
    protected void salir() {
        super.salir();
        cbCodRep.setSelectedIndex(0);
        cbCodFac.setSelectedIndex(0);
    }

    private void abrirFacturaGasolinera() {
        int codFac = (int) cbCodFac.getSelectedItem();
        // Implementa la lógica para abrir y gestionar la factura de gasolinera
        JOptionPane.showMessageDialog(this, "Gestionando factura de gasolinera: " + codFac);
    }

    private String getTextFromPanel(int index) {
        if (index >= 0 && index < pnlAtributosExtras.length) {
            Component component = pnlAtributosExtras[index].getComponent(0);
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                return textField.getText();
            }
        }
        return "";
    }

    private void setTextInPanel(int index, String text) {
        if (index >= 0 && index < pnlAtributosExtras.length) {
            Component component = pnlAtributosExtras[index].getComponent(0);
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                textField.setText(text);
            }
        }
    }
}
