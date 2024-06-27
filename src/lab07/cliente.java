package lab07;

import java.sql.*;
import javax.swing.*;

public class cliente extends interfazGeneral {

    public cliente() {
        super("CRUD Cliente Interface", new String[]{"Nombre", "Apellido", "Dirección", "Teléfono", "Consumo P", "Consumo B", "Consumo C"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cliente")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String id = rs.getString("CLI_ID");
                String nombre = rs.getString("NOM_CLI");
                String apellido = rs.getString("APE_CLI");
                String direccion = rs.getString("DIR_CLI");
                String telefono = rs.getString("TEL_CLI");
                String consP = rs.getString("CONS_P");
                String consB = rs.getString("CONS_B");
                String consC = rs.getString("CONS_C");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(id));
                tableModel.addRow(new Object[]{id, nombre, apellido, direccion, telefono, consP, consB, consC, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        String id = txtCodigo.getText();
        String nombre = txtAtributosExtras[0].getText();
        String apellido = txtAtributosExtras[1].getText();
        String direccion = txtAtributosExtras[2].getText();
        String telefono = txtAtributosExtras[3].getText();
        String consP = txtAtributosExtras[4].getText();
        String consB = txtAtributosExtras[5].getText();
        String consC = txtAtributosExtras[6].getText();
        String estado = "A";

        if (!usedCodes.contains(Integer.parseInt(id))) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO cliente (CLI_ID, NOM_CLI, APE_CLI, DIR_CLI, TEL_CLI, CONS_P, CONS_B, CONS_C, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, id);
                pstmt.setString(2, nombre);
                pstmt.setString(3, apellido);
                pstmt.setString(4, direccion);
                pstmt.setString(5, telefono);
                pstmt.setString(6, consP);
                pstmt.setString(7, consB);
                pstmt.setString(8, consC);
                pstmt.setString(9, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "El registro con la clave " + id + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 8).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtAtributosExtras[0].setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtAtributosExtras[2].setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtAtributosExtras[3].setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtAtributosExtras[4].setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtAtributosExtras[5].setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtAtributosExtras[6].setText(tableModel.getValueAt(selectedRow, 7).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 8).toString());
            txtCodigo.setEditable(false);
            for (JTextField txtAtributoExtra : txtAtributosExtras) {
                txtAtributoExtra.setEditable(true);
            }
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 8).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                for (int i = 0; i < txtAtributosExtras.length; i++) {
                    txtAtributosExtras[i].setText(tableModel.getValueAt(selectedRow, i + 1).toString());
                }
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 8).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            for (int i = 0; i < txtAtributosExtras.length; i++) {
                txtAtributosExtras[i].setText(tableModel.getValueAt(selectedRow, i + 1).toString());
            }
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            for (JTextField txtAtributoExtra : txtAtributosExtras) {
                txtAtributoExtra.setEditable(false);
            }
            btnActualizar.setEnabled(true);
        } else if (tableModel.getValueAt(selectedRow, 8).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 8).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 8).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            for (int i = 0; i < txtAtributosExtras.length; i++) {
                txtAtributosExtras[i].setText(tableModel.getValueAt(selectedRow, i + 1).toString());
            }
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else if (tableModel.getValueAt(selectedRow, 8).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 8).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String id = txtCodigo.getText();
            String nombre = txtAtributosExtras[0].getText();
            String apellido = txtAtributosExtras[1].getText();
            String direccion = txtAtributosExtras[2].getText();
            String telefono = txtAtributosExtras[3].getText();
            String consP = txtAtributosExtras[4].getText();
            String consB = txtAtributosExtras[5].getText();
            String consC = txtAtributosExtras[6].getText();
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE cliente SET NOM_CLI = ?, APE_CLI = ?, DIR_CLI = ?, TEL_CLI = ?, CONS_P = ?, CONS_B = ?, CONS_C = ?, ESTADO = ? WHERE CLI_ID = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, apellido);
                pstmt.setString(3, direccion);
                pstmt.setString(4, telefono);
                pstmt.setString(5, consP);
                pstmt.setString(6, consB);
                pstmt.setString(7, consC);
                pstmt.setString(8, estado);
                pstmt.setString(9, id);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
