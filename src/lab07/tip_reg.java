package lab07;

import java.sql.*;
import javax.swing.*;

public class tip_reg extends interfazGeneral {

    public tip_reg() {
        super("CRUD Tipo de Regalo Interface", new String[]{"Categoría"});
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tipo_de_regalo")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("COD_TIP_REG");
                String categoria = rs.getString("CAT_REG");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, categoria, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        String codigo = txtCodigo.getText();
        String categoria = txtAtributosExtras[0].getText();
        String estado = "A";

        if (!usedCodes.contains(Integer.parseInt(codigo))) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tipo_de_regalo (COD_TIP_REG, CAT_REG, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setString(1, codigo);
                pstmt.setString(2, categoria);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtAtributosExtras[0].setText("");
                lblEstado.setText("");
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtAtributosExtras[0].setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            txtCodigo.setEnabled(false);
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
                String codigo = tableModel.getValueAt(selectedRow, 0).toString();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE tipo_de_regalo SET ESTADO = '*' WHERE COD_TIP_REG = ?")) {
                    pstmt.setString(1, codigo);
                    pstmt.executeUpdate();

                    cargarDatos();
                    txtCodigo.setText("");
                    txtAtributosExtras[0].setText("");
                    lblEstado.setText("");
                    txtCodigo.setEditable(true);
                    txtAtributosExtras[0].setEditable(true);
                    btnActualizar.setEnabled(false);
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            String codigo = tableModel.getValueAt(selectedRow, 0).toString();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tipo_de_regalo SET ESTADO = 'I' WHERE COD_TIP_REG = ?")) {
                pstmt.setString(1, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtAtributosExtras[0].setText("");
                lblEstado.setText("");
                txtCodigo.setEditable(true);
                txtAtributosExtras[0].setEditable(true);
                btnActualizar.setEnabled(false);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            String codigo = tableModel.getValueAt(selectedRow, 0).toString();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tipo_de_regalo SET ESTADO = 'A' WHERE COD_TIP_REG = ?")) {
                pstmt.setString(1, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtAtributosExtras[0].setText("");
                lblEstado.setText("");
                txtCodigo.setEditable(true);
                txtAtributosExtras[0].setEditable(true);
                btnActualizar.setEnabled(false);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            String categoria = txtAtributosExtras[0].getText();
            String estado = lblEstado.getText();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tipo_de_regalo SET CAT_REG = ?, ESTADO = ? WHERE COD_TIP_REG = ?")) {
                pstmt.setString(1, categoria);
                pstmt.setString(2, estado);
                pstmt.setString(3, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtAtributosExtras[0].setText("");
                lblEstado.setText("");
                txtCodigo.setEditable(true);
                txtAtributosExtras[0].setEditable(true);
                btnActualizar.setEnabled(false);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
