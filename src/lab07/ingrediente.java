package lab07;

import java.sql.*;
import javax.swing.*;

public class ingrediente extends interfazGeneral {
    private JTextField txtNombre;

    public ingrediente() {
        super("CRUD Ingrediente Interface", new String[]{"Nombre"});
        txtNombre = new JTextField();
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        addExtraComponent(0, txtNombre);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ingrediente")) {

            txtCodigo.setText("" + generateNextCode("ingrediente", "ING_ID"));
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("ING_ID");
                String nombre = rs.getString("NOM_ING");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, nombre, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        String nombre = txtNombre.getText();
        String estado = "A";

        if (isDuplicateName(nombre, "ingrediente", "NOM_ING")) {
            JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int codigo = generateNextCode("ingrediente", "ING_ID");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ingrediente (ING_ID, NOM_ING, ESTADO) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.setString(3, estado);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCodigo.setEditable(false);
            txtNombre.setEditable(true);
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
                txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
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
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            txtNombre.setEditable(false);
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
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
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
            String codigo = txtCodigo.getText();
            String nombre = txtNombre.getText();
            String estado = lblEstado.getText();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente SET NOM_ING = ?, ESTADO = ? WHERE ING_ID = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, estado);
                pstmt.setString(3, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @Override
    protected void cancelar() {
        txtNombre.setText("");
        lblEstado.setText("");
        txtCodigo.setEditable(true);
        txtNombre.setEditable(true);
        btnActualizar.setEnabled(false);
        CarFlaAct = 0;
        operation = "";
        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);
        cargarDatos();
    }

}
