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

    public regaloAlmacen() {
        super("CRUD Regalo Almacen Interface", new String[]{"Almacen", "Regalo", "Stock Actual", "Stock Mínimo", "Stock Máximo", "Stock Seguridad"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarAlmacenes();
        cargarRegalos();
    }

    public regaloAlmacen(String title, String[] columnNames) {
        super(title, columnNames);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }

    private void cargarAlmacenes() {
        almacenMap = new HashMap<>();
        comboCodAlm = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_ALM FROM almacen WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codAlm = rs.getString("COD_ALM");
                comboCodAlm.addItem(codAlm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodAlm, 3);

        revalidate();
        repaint();
    }

    private void cargarRegalos() {
        regaloMap = new HashMap<>();
        comboCodReg = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REG FROM regalo WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codReg = rs.getString("COD_REG");
                comboCodReg.addItem(codReg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[1]);
        dataPanel.add(comboCodReg, 5);

        revalidate();
        repaint();
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
            double stoAct = Double.parseDouble(txtAtributosExtras[0].getText());
            double stoMin = Double.parseDouble(txtAtributosExtras[1].getText());
            double stoMax = Double.parseDouble(txtAtributosExtras[2].getText());
            double stoSeg = Double.parseDouble(txtAtributosExtras[3].getText());
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
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codAlm = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodAlm.setSelectedItem(codAlm);
            comboCodReg.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtAtributosExtras[2].setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtAtributosExtras[3].setText(tableModel.getValueAt(selectedRow, 6).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 7).toString());
            txtCodigo.setEditable(false);
            comboCodAlm.setEnabled(true);
            comboCodReg.setEnabled(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 7).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                String codAlm = tableModel.getValueAt(selectedRow, 1).toString();
                comboCodAlm.setSelectedItem(codAlm);
                lblEstado.setText("*");
                operation = "mod";
                CarFlaAct = 1;
                btnActualizar.setEnabled(true);
                actualizar();
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 7).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codAlm = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodAlm.setSelectedItem(codAlm);
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboCodAlm.setEnabled(false);
            comboCodReg.setEnabled(false);
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 7).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 7).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 7).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codAlm = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodAlm.setSelectedItem(codAlm);
            comboCodReg.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 7).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 7).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            if (operation.equals("add")) {
                adicionar();
            } else {
                if (operation.equals("mod")) {
                    modificar();
                }
            }
            CarFlaAct = 0;
            txtCodigo.setText("");
            for (JTextField txtAtributoExtra : txtAtributosExtras) {
                txtAtributoExtra.setText("");
            }
            lblEstado.setText("");
            table.clearSelection();
            txtCodigo.setEditable(true);
            for (JTextField txtAtributoExtra : txtAtributosExtras) {
                txtAtributoExtra.setEditable(true);
            }
            btnAdicionar.setEnabled(true);
            btnModificar.setEnabled(false);
            btnEliminar.setEnabled(false);
            btnInactivar.setEnabled(false);
            btnReactivar.setEnabled(false);
            btnActualizar.setEnabled(false);
        }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        CarFlaAct = 0;
        operation = "";
        comboCodAlm.setSelectedIndex(-1);
        comboCodReg.setSelectedIndex(-1);
    }
}
