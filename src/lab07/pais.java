package lab07;

import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class pais extends interfazGeneral {

    private JTextField txtNombre;

    public pais() {
        super("CRUD Pais Interface", new String[]{"Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        
        // Crear el JTextField para el atributo "Nombre" y agregarlo al panel
        txtNombre = new JTextField();
        addExtraComponent(0, txtNombre);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery("SELECT * FROM pais")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("COD_PAI");
                String nombre = rs.getString("NOM_PAI");
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

        if (isDuplicateName(nombre, "pais", "NOM_PAI")) {
            JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int codigo = generateNextCode("pais", "COD_PAI");

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pais (COD_PAI, NOM_PAI, ESTADO) VALUES (?, ?, ?)")) {
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
                int codPai = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                actualizarEstado("region", "ESTADO", "COD_PAI", codPai, "*", "*");

                try (Connection conn = DatabaseConnection.getConnection(); 
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET ESTADO = '*' WHERE COD_PAI = ?")) {
                    pstmt.setInt(1, codPai);
                    pstmt.executeUpdate();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar el país: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            int codPai = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            actualizarEstado("region", "ESTADO", "COD_PAI", codPai, "I", "*");

            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET ESTADO = 'I' WHERE COD_PAI = ?")) {
                pstmt.setInt(1, codPai);
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el país: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
            int codPai = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            actualizarEstado("region", "ESTADO", "COD_PAI", codPai, "A", "*");

            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET ESTADO = 'A' WHERE COD_PAI = ?")) {
                pstmt.setInt(1, codPai);
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el país: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET NOM_PAI = ?, ESTADO = ? WHERE COD_PAI = ?")) {
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
        // Limpiar el campo de texto del código y el campo adicional
        txtCodigo.setText("");
        txtNombre.setText("");

        // Restablecer el estado del label de estado
        lblEstado.setText("");

        // Habilitar/Deshabilitar campos y botones según corresponda
        txtCodigo.setEditable(true);
        txtNombre.setEditable(true);
        btnActualizar.setEnabled(false);

        // Restablecer las variables de control
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
