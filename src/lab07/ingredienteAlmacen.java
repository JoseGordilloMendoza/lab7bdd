package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ingredienteAlmacen extends interfazGeneral {

    private JComboBox<String> comboCodIng;
    private JComboBox<String> comboCodAlm;
    private Map<String, Integer> ingredienteMap;
    private Map<String, Integer> almacenMap;

    public ingredienteAlmacen() {
        super("CRUD Ingrediente Almacen Interface", new String[]{"Ingrediente", "Almacen", "Stock Actual", "Stock Mínimo", "Stock Máximo", "Stock Seguridad"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarIngredientes();
        cargarAlmacenes();
    }

    public ingredienteAlmacen(String title, String[] columnNames) {
        super(title, columnNames);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }

    private void cargarIngredientes() {
        ingredienteMap = new HashMap<>();
        comboCodIng = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ING_ID FROM ingrediente WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codIng = rs.getString("ING_ID");
                comboCodIng.addItem(codIng);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodIng, 3);

        revalidate();
        repaint();
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
        dataPanel.remove(txtAtributosExtras[1]);
        dataPanel.add(comboCodAlm, 5);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ia.ING_ID, ia.COD_ALM, ia.STO_ACT, ia.STO_MIN, ia.STO_MAX, ia.STO_SEG, ia.ESTADO FROM ingrediente_almacen ia")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int ingId = rs.getInt("ING_ID");
                int codAlm = rs.getInt("COD_ALM");
                double stoAct = rs.getDouble("STO_ACT");
                double stoMin = rs.getDouble("STO_MIN");
                double stoMax = rs.getDouble("STO_MAX");
                double stoSeg = rs.getDouble("STO_SEG");
                String estado = rs.getString("ESTADO");

                usedCodes.add(ingId);
                tableModel.addRow(new Object[]{ingId, codAlm, stoAct, stoMin, stoMax, stoSeg, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            String codIng = (String) comboCodIng.getSelectedItem();
            String codAlm = (String) comboCodAlm.getSelectedItem();
            double stoAct = Double.parseDouble(txtAtributosExtras[0].getText());
            double stoMin = Double.parseDouble(txtAtributosExtras[1].getText());
            double stoMax = Double.parseDouble(txtAtributosExtras[2].getText());
            double stoSeg = Double.parseDouble(txtAtributosExtras[3].getText());
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ingrediente_almacen (ING_ID, COD_ALM, STO_ACT, STO_MIN, STO_MAX, STO_SEG, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, codIng);
                pstmt.setString(2, codAlm);
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
            String codIng = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodIng.setSelectedItem(codIng);
            comboCodAlm.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtAtributosExtras[2].setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtAtributosExtras[3].setText(tableModel.getValueAt(selectedRow, 6).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 7).toString());
            txtCodigo.setEditable(false);
            comboCodIng.setEnabled(true);
            comboCodAlm.setEnabled(true);
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
                String codIng = tableModel.getValueAt(selectedRow, 1).toString();
                comboCodIng.setSelectedItem(codIng);
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
            String codIng = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodIng.setSelectedItem(codIng);
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboCodIng.setEnabled(false);
            comboCodAlm.setEnabled(false);
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
            String codIng = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodIng.setSelectedItem(codIng);
            comboCodAlm.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
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
        comboCodIng.setSelectedIndex(-1);
        comboCodAlm.setSelectedIndex(-1);
    }
}
