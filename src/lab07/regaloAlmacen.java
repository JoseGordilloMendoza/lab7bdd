package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class regaloAlmacen extends interfazGeneral {

    private JComboBox<String> comboCodAlm;
    private JComboBox<String> comboCodReg;
    private Map<String, Integer> almacenMap;
    private Map<String, Integer> regaloMap;

    // Atributos extras
    private JTextField txtStockActual;
    private JTextField txtStockMinimo;
    private JTextField txtStockMaximo;
    private JTextField txtStockSeguridad;

    public regaloAlmacen() {
        super("CRUD Regalo Almacen Interface", new String[]{"Almacen", "Regalo", "Stock Actual", "Stock Mínimo", "Stock Máximo", "Stock Seguridad"});
        cargarAlmacenes();
        cargarRegalos();
        cargarDatos();
        configurarAtributosExtras();
    }

    public regaloAlmacen(String title, String[] columnNames) {
        super(title, columnNames);
        cargarAlmacenes();
        cargarRegalos();
        cargarDatos();
        configurarAtributosExtras();
    }

    private void cargarAlmacenes() {
        almacenMap = new HashMap<>();
        comboCodAlm = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_ALM FROM almacen WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codAlm = rs.getString("COD_ALM");
                comboCodAlm.addItem(codAlm);
                almacenMap.put(codAlm, rs.getInt("COD_ALM"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(0, comboCodAlm);
    }

    private void cargarRegalos() {
        regaloMap = new HashMap<>();
        comboCodReg = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REG FROM regalo WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codReg = rs.getString("COD_REG");
                comboCodReg.addItem(codReg);
                regaloMap.put(codReg, rs.getInt("COD_REG"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(1, comboCodReg);
    }

    private void configurarAtributosExtras() {
        txtStockActual = new JTextField();
        addExtraComponent(2, txtStockActual);

        txtStockMinimo = new JTextField();
        addExtraComponent(3, txtStockMinimo);

        txtStockMaximo = new JTextField();
        addExtraComponent(4, txtStockMaximo);

        txtStockSeguridad = new JTextField();
        addExtraComponent(5, txtStockSeguridad);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ra.COD_ALM, ra.COD_REG, ra.STOC_ACT, ra.STO_MIN, ra.STO_MAX, ra.STO_SEG, ra.ESTADO FROM regalo_almacen ra")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codAlm = rs.getInt("COD_ALM");
                int codReg = rs.getInt("COD_REG");
                double stoAct = rs.getDouble("STOC_ACT");
                double stoMin = rs.getDouble("STO_MIN");
                double stoMax = rs.getDouble("STO_MAX");
                double stoSeg = rs.getDouble("STO_SEG");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codAlm);
                tableModel.addRow(new Object[]{codAlm, codReg, stoAct, stoMin, stoMax, stoSeg, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            String codAlm = (String) comboCodAlm.getSelectedItem();
            String codReg = (String) comboCodReg.getSelectedItem();
            double stoAct = Double.parseDouble(txtStockActual.getText());
            double stoMin = Double.parseDouble(txtStockMinimo.getText());
            double stoMax = Double.parseDouble(txtStockMaximo.getText());
            double stoSeg = Double.parseDouble(txtStockSeguridad.getText());
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO regalo_almacen (COD_ALM, COD_REG, STOC_ACT, STO_MIN, STO_MAX, STO_SEG, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, codAlm);
                pstmt.setString(2, codReg);
                pstmt.setDouble(3, stoAct);
                pstmt.setDouble(4, stoMin);
                pstmt.setDouble(5, stoMax);
                pstmt.setDouble(6, stoSeg);
                pstmt.setString(7, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stock actual, mínimo, máximo o de seguridad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
            String codAlm = (String) comboCodAlm.getSelectedItem();
            String codReg = (String) comboCodReg.getSelectedItem();
            double stoAct = Double.parseDouble(txtStockActual.getText());
            double stoMin = Double.parseDouble(txtStockMinimo.getText());
            double stoMax = Double.parseDouble(txtStockMaximo.getText());
            double stoSeg = Double.parseDouble(txtStockSeguridad.getText());
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE regalo_almacen SET STOC_ACT = ?, STO_MIN = ?, STO_MAX = ?, STO_SEG = ?, ESTADO = ? WHERE COD_ALM = ? AND COD_REG = ?")) {
                pstmt.setDouble(1, stoAct);
                pstmt.setDouble(2, stoMin);
                pstmt.setDouble(3, stoMax);
                pstmt.setDouble(4, stoSeg);
                pstmt.setString(5, estado);
                pstmt.setString(6, codAlm);
                pstmt.setString(7, codReg);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 6).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                String codAlm = tableModel.getValueAt(selectedRow, 0).toString();
                String codReg = tableModel.getValueAt(selectedRow, 1).toString();

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE regalo_almacen SET ESTADO = '*' WHERE COD_ALM = ? AND COD_REG = ?")) {
                    pstmt.setString(1, codAlm);
                    pstmt.setString(2, codReg);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
            String codAlm = tableModel.getValueAt(selectedRow, 0).toString();
            String codReg = tableModel.getValueAt(selectedRow, 1).toString();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE regalo_almacen SET ESTADO = 'I' WHERE COD_ALM = ? AND COD_REG = ?")) {
                pstmt.setString(1, codAlm);
                pstmt.setString(2, codReg);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede inactivarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("I")) {
            String codAlm = tableModel.getValueAt(selectedRow, 0).toString();
            String codReg = tableModel.getValueAt(selectedRow, 1).toString();
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE regalo_almacen SET ESTADO = 'A' WHERE COD_ALM = ? AND COD_REG = ?")) {
                pstmt.setString(1, codAlm);
                pstmt.setString(2, codReg);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede reactivarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String codAlm = tableModel.getValueAt(selectedRow, 0).toString();
            String codReg = tableModel.getValueAt(selectedRow, 1).toString();

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT STOC_ACT, STO_MIN, STO_MAX, STO_SEG, ESTADO FROM regalo_almacen WHERE COD_ALM = ? AND COD_REG = ?")) {
                pstmt.setString(1, codAlm);
                pstmt.setString(2, codReg);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        comboCodAlm.setSelectedItem(codAlm);
                        comboCodReg.setSelectedItem(codReg);
                        ((JTextField) pnlAtributosExtras[0].getComponent(1)).setText(String.valueOf(rs.getDouble("STOC_ACT")));
                        ((JTextField) pnlAtributosExtras[1].getComponent(1)).setText(String.valueOf(rs.getDouble("STO_MIN")));
                        ((JTextField) pnlAtributosExtras[2].getComponent(1)).setText(String.valueOf(rs.getDouble("STO_MAX")));
                        ((JTextField) pnlAtributosExtras[3].getComponent(1)).setText(String.valueOf(rs.getDouble("STO_SEG")));
                        lblEstado.setText(rs.getString("ESTADO"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un registro para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        comboCodAlm.setSelectedIndex(0);
        comboCodReg.setSelectedIndex(0);
        txtStockActual.setText("");
        txtStockMinimo.setText("");
        txtStockMaximo.setText("");
        txtStockSeguridad.setText("");
        lblEstado.setText("");
        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
    }
}
