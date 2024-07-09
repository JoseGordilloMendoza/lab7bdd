package lab07;

import java.sql.*;
import javax.swing.*;

public class turno extends interfazGeneral {

    public turno() {
        super("CRUD Turno Interface", new String[]{"Inicio", "Fin"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM turno")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("TIP_TUR");
                String inicio = rs.getString("INI_TUR");
                String fin = rs.getString("FIN_TUR");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, inicio, fin, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int codigo = generateNextCode("turno", "TIP_TUR");
        String inicio = txtAtributosExtras[0].getText();
        String fin = txtAtributosExtras[1].getText();
        String estado = "A";

        if (!usedCodes.contains(codigo)) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO turno (TIP_TUR, INI_TUR, FIN_TUR, ESTADO) VALUES (?, ?, ?, ?)")) {
                pstmt.setInt(1, codigo);
                pstmt.setString(2, inicio);
                pstmt.setString(3, fin);
                pstmt.setString(4, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "El registro con la clave " + codigo + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            txtAtributosExtras[0].setEditable(true);
            txtAtributosExtras[1].setEditable(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            txtAtributosExtras[0].setEditable(false);
            txtAtributosExtras[1].setEditable(false);
            btnActualizar.setEnabled(true);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            String inicio = txtAtributosExtras[0].getText();
            String fin = txtAtributosExtras[1].getText();
            String estado = lblEstado.getText();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE turno SET INI_TUR = ?, FIN_TUR = ?, ESTADO = ? WHERE TIP_TUR = ?")) {
                pstmt.setString(1, inicio);
                pstmt.setString(2, fin);
                pstmt.setString(3, estado);
                pstmt.setString(4, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
                txtCodigo.setEditable(true);
                txtAtributosExtras[0].setEditable(true);
                txtAtributosExtras[1].setEditable(true);
                btnActualizar.setEnabled(false);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
