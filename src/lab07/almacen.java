package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class almacen extends interfazGeneral {

    private JComboBox<String> comboCodFran;
    private Map<String, Integer> franquiciaMap;

    public almacen() {
        super("CRUD Almacen Interface", new String[]{"Franquicia"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarFranquicias();
    }

    public almacen(String title, String[] columnNames) {
        super(title, columnNames);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }

    private void cargarFranquicias() {
        franquiciaMap = new HashMap<>();
        comboCodFran = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_FRAN FROM franquicia WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                String codFran = rs.getString("COD_FRAN");
                comboCodFran.addItem(codFran);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodFran, 3);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT a.COD_ALM, a.COD_FRAN, a.ESTADO FROM almacen a")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codAlm = rs.getInt("COD_ALM");
                int codFran = rs.getInt("COD_FRAN");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codAlm);
                tableModel.addRow(new Object[]{codAlm, codFran, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            String codFran = (String) comboCodFran.getSelectedItem();
            String estado = "A";

            int nuevoCodAlm = generateNextCode("almacen", "COD_ALM");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO almacen (COD_ALM, COD_FRAN, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, nuevoCodAlm);
                pstmt.setString(2, codFran);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de franquicia inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codFran = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodFran.setSelectedItem(codFran);
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCodigo.setEditable(false);
            comboCodFran.setEnabled(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                String codFran = tableModel.getValueAt(selectedRow, 1).toString();
                comboCodFran.setSelectedItem(codFran);
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codFran = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodFran.setSelectedItem(codFran);
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboCodFran.setEnabled(false);
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codFran = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodFran.setSelectedItem(codFran);
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            try {
                int codAlm = Integer.parseInt(txtCodigo.getText());
                String codFran = (String) comboCodFran.getSelectedItem();
                String estado = lblEstado.getText();

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE almacen SET COD_FRAN = ?, ESTADO = ? WHERE COD_ALM = ?")) {
                    pstmt.setString(1, codFran);
                    pstmt.setString(2, estado);
                    pstmt.setInt(3, codAlm);
                    pstmt.executeUpdate();

                    cancelar();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de almacén inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void cancelar() {
        txtCodigo.setText("");
        comboCodFran.setSelectedIndex(0);
        lblEstado.setText("A");
        txtCodigo.setEditable(true);
        comboCodFran.setEnabled(true);
        CarFlaAct = 0;
        operation = "";
        btnActualizar.setEnabled(false);
    }
}
