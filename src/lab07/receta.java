package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class receta extends interfazGeneral {
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private Map<String, Integer> tipoArticuloMap;
    private Map<String, Integer> ingredienteMap;

    public receta() {
        super("CRUD Receta Interface", new String[]{"Nombre", "Descripción"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarDatos();
        cargarComponentes();
    }

    private void cargarComponentes() {
        txtNombre = new JTextField();
        addExtraComponent(0, txtNombre);

        txtDescripcion = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(txtDescripcion);
        addExtraComponent(1, scrollPane);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_RECETA, NOMBRE, DESCRIPCION, ESTADO FROM receta")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codReceta = rs.getInt("COD_RECETA");
                String nombre = rs.getString("NOMBRE");
                String descripcion = rs.getString("DESCRIPCION");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codReceta);
                tableModel.addRow(new Object[]{codReceta, nombre, descripcion, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();
        String estado = "A";
        int cod = Integer.parseInt(txtCodigo.getText());
        
        try {
            if (!usedCodes.contains(nombre)) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO receta (COD_RECETA, NOMBRE, DESCRIPCION, ESTADO) VALUES (?, ?, ?, ?)")) {
                    pstmt.setInt(1, cod);
                    pstmt.setString(2, nombre);
                    pstmt.setString(3, descripcion);
                    pstmt.setString(4, estado);
                    pstmt.executeUpdate();

                    cargarDatos();
                    // Limpiar campos después de insertar
                    txtNombre.setText("");
                    txtDescripcion.setText("");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "El registro con el nombre " + nombre + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nombre inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDescripcion.setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtNombre.setEditable(true);
            txtDescripcion.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int cod=Integer.parseInt(txtCodigo.getText());
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE receta SET ESTADO = '*' WHERE COD_RECETA = ?")) {
                    pstmt.setInt(1, cod);
                    pstmt.executeUpdate();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser eliminado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE receta SET ESTADO = 'I' WHERE COD_RECETA = ?")) {
                pstmt.setInt(1, Integer.parseInt(txtCodigo.getText()));
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "Este registro ya está inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede inactivarse", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codReceta = Integer.parseInt(txtCodigo.getText());
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE receta SET NOMBRE = ?, DESCRIPCION = ?, ESTADO = ? WHERE COD_RECETA = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, descripcion);
                pstmt.setString(3, estado);
                pstmt.setInt(4, codReceta);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de receta inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE receta SET ESTADO = 'A' WHERE COD_RECETA = ?")) {
                pstmt.setInt(1, Integer.parseInt(txtCodigo.getText()));
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "Este registro ya está activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede reactivarse", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
