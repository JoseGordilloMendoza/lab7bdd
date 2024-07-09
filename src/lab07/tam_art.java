package lab07;

import javax.swing.*;
import java.sql.*;

public class tam_art extends interfazGeneral {

    private JTextField txtNombre;

    public tam_art() {
        super("CRUD TAMAÑO ARTICULOS Interface", new String[]{"Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        txtNombre = new JTextField();
        addExtraComponent(0, txtNombre);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tamaño_del_articulo")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("COD_TAM_ART");
                String nombre = rs.getString("TAM");
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
        int codigo = Integer.parseInt(txtCodigo.getText());
        String nombre = txtNombre.getText();
        String estado = "A";

        if (!usedCodes.contains(codigo)) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tamaño_del_articulo (COD_TAM_ART, TAM, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, codigo);
                pstmt.setString(2, nombre);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtNombre.setText("");
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
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtNombre.setEditable(true);
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
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tamaño_del_articulo SET TAM = ?, ESTADO = ? WHERE COD_TAM_ART = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, estado);
                pstmt.setString(3, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtNombre.setText("");
                lblEstado.setText("");
                txtCodigo.setEditable(true);
                txtCodigo.setEnabled(true);
                txtNombre.setEditable(true);
                btnActualizar.setEnabled(false);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @Override
    protected void cancelar() {
        // Limpiar los campos de texto y restablecer estado de componentes
        txtCodigo.setText("");
        txtNombre.setText("");
        lblEstado.setText("");

        // Restablecer estado de edición de campos y botones
        txtCodigo.setEditable(true);
        txtNombre.setEditable(true);

        // Deshabilitar botones de operación según el estado deseado
        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);

        // Cargar los datos nuevamente en la tabla
        cargarDatos();
    }

}
