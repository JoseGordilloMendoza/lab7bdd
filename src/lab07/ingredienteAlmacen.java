package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ingredienteAlmacen extends interfazGeneral {

    private JComboBox<String> comboCodIng;
    private JComboBox<String> comboCodAlm;
    private JTextField txtStoAct;
    private JTextField txtStoMin;
    private JTextField txtStoMax;
    private JTextField txtStoSeg;
    private Map<String, Integer> ingredienteMap;
    private Map<String, Integer> almacenMap;

    public ingredienteAlmacen() {
        super("CRUD Ingrediente Almacen Interface", new String[]{"Stock Actual", "Stock Mínimo", "Stock Máximo", "Stock Seguridad", "Ingrediente", "Almacen"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarIngredientes();
        cargarAlmacenes();
        inicializarComponentes();
    }

    private void cargarIngredientes() {
        ingredienteMap = new HashMap<>();
        comboCodIng = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ING_ID, NOM_ING FROM ingrediente WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                int ingId = rs.getInt("ING_ID");
                String nombre = rs.getString("NOM_ING");
                String item = ingId + " / " + nombre;
                ingredienteMap.put(item, ingId);
                comboCodIng.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(4, comboCodIng);

        revalidate();
        repaint();
    }

    private void cargarAlmacenes() {
        almacenMap = new HashMap<>();
        comboCodAlm = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_ALM FROM almacen WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                int codAlm = rs.getInt("COD_ALM");
                almacenMap.put(String.valueOf(codAlm), codAlm);
                comboCodAlm.addItem(String.valueOf(codAlm));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(5, comboCodAlm);

        revalidate();
        repaint();
    }

    private void inicializarComponentes() {
        txtStoAct = new JTextField(10);
        txtStoMin = new JTextField(10);
        txtStoMax = new JTextField(10);
        txtStoSeg = new JTextField(10);

        addExtraComponent(0, txtStoAct);
        addExtraComponent(1, txtStoMin);
        addExtraComponent(2, txtStoMax);
        addExtraComponent(3, txtStoSeg);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT STO_ACT, STO_MIN, STO_MAX, STO_SEG, ING_ID, COD_ALM, ESTADO FROM ingrediente_almacen")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                long stoAct = rs.getLong("STO_ACT");
                long stoMin = rs.getLong("STO_MIN");
                long stoMax = rs.getLong("STO_MAX");
                long stoSeg = rs.getLong("STO_SEG");
                int ingId = rs.getInt("ING_ID");
                int codAlm = rs.getInt("COD_ALM");
                String estado = rs.getString("ESTADO");

                usedCodes.add(ingId);
                tableModel.addRow(new Object[]{stoAct, stoMin, stoMax, stoSeg, ingId, codAlm, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
protected void adicionar() {
    try {
        int ingId = Integer.parseInt(txtCodigo.getText());  // Obtener el ID de ingrediente desde txtCodigo
        int codAlm = Integer.parseInt((String) comboCodAlm.getSelectedItem());  // Obtener el código de almacén desde comboCodAlm
        long stoAct = Long.parseLong(txtStoAct.getText());
        long stoMin = Long.parseLong(txtStoMin.getText());
        long stoMax = Long.parseLong(txtStoMax.getText());
        long stoSeg = Long.parseLong(txtStoSeg.getText());
        String estado = "A";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ingrediente_almacen (STO_ACT, STO_MIN, STO_MAX, STO_SEG, ING_ID, COD_ALM, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setLong(1, stoAct);
            pstmt.setLong(2, stoMin);
            pstmt.setLong(3, stoMax);
            pstmt.setLong(4, stoSeg);
            pstmt.setInt(5, ingId);
            pstmt.setInt(6, codAlm);
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
        txtStoAct.setText(tableModel.getValueAt(selectedRow, 0).toString());
        txtStoMin.setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtStoMax.setText(tableModel.getValueAt(selectedRow, 2).toString());
        txtStoSeg.setText(tableModel.getValueAt(selectedRow, 3).toString());
        int ingId = (int) tableModel.getValueAt(selectedRow, 4);
        txtCodigo.setText(String.valueOf(ingId));  // Establecer el ID de ingrediente en txtCodigo
        comboCodAlm.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
        lblEstado.setText(tableModel.getValueAt(selectedRow, 6).toString());
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
    if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 6).toString().equals("*")) {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            int ingId = (int) tableModel.getValueAt(selectedRow, 4);
            txtCodigo.setText(String.valueOf(ingId));  // Establecer el ID de ingrediente en txtCodigo
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
    if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
        txtStoAct.setText(tableModel.getValueAt(selectedRow, 0).toString());
        int ingId = (int) tableModel.getValueAt(selectedRow, 4);
        txtCodigo.setText(String.valueOf(ingId));  // Establecer el ID de ingrediente en txtCodigo
        comboCodAlm.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
        lblEstado.setText("I");
        CarFlaAct = 1;
        operation = "mod";
        txtCodigo.setEditable(false);
        comboCodIng.setEnabled(false);
        comboCodAlm.setEnabled(false);
        btnActualizar.setEnabled(true);
        actualizar();
    } else if (tableModel.getValueAt(selectedRow, 6).toString().equals("I")) {
        JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
    } else if (tableModel.getValueAt(selectedRow, 6).toString().equals("*")) {
        JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

@Override
protected void reactivar() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 6).toString().equals("I")) {
        txtStoAct.setText(tableModel.getValueAt(selectedRow, 0).toString());
        int ingId = (int) tableModel.getValueAt(selectedRow, 4);
        txtCodigo.setText(String.valueOf(ingId));  // Establecer el ID de ingrediente en txtCodigo
        comboCodAlm.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
        lblEstado.setText("A");
        CarFlaAct = 1;
        operation = "mod";
        btnActualizar.setEnabled(true);
        actualizar();
    } else if (tableModel.getValueAt(selectedRow, 6).toString().equals("A")) {
        JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
    } else if (tableModel.getValueAt(selectedRow, 6).toString().equals("*")) {
        JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

@Override
protected void actualizar() {
    if (CarFlaAct == 1) {
        if (operation.equals("add")) {
            adicionar();
        } else if (operation.equals("mod")) {
            try {
                int ingId = Integer.parseInt(txtCodigo.getText());  // Obtener el ID de ingrediente desde txtCodigo
                int codAlm = Integer.parseInt((String) comboCodAlm.getSelectedItem());  // Obtener el código de almacén desde comboCodAlm
                long stoAct = Long.parseLong(txtStoAct.getText());
                long stoMin = Long.parseLong(txtStoMin.getText());
                long stoMax = Long.parseLong(txtStoMax.getText());
                long stoSeg = Long.parseLong(txtStoSeg.getText());
                String estado = lblEstado.getText();

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente_almacen SET STO_ACT = ?, STO_MIN = ?, STO_MAX = ?, STO_SEG = ?, ESTADO = ? WHERE ING_ID = ? AND COD_ALM = ?")) {
                    pstmt.setLong(1, stoAct);
                    pstmt.setLong(2, stoMin);
                    pstmt.setLong(3, stoMax);
                    pstmt.setLong(4, stoSeg);
                    pstmt.setString(5, estado);
                    pstmt.setInt(6, ingId);
                    pstmt.setInt(7, codAlm);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Stock actual, mínimo, máximo o de seguridad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        CarFlaAct = 0;
        cancelar();
    }
}

    @Override
    protected void cancelar() {
        super.cancelar();
        CarFlaAct = 0;
        operation = "";
        comboCodIng.setSelectedIndex(-1);
        comboCodAlm.setSelectedIndex(-1);
        txtStoAct.setText("");
        txtStoMin.setText("");
        txtStoMax.setText("");
        txtStoSeg.setText("");
    }
}
