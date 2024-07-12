package lab07;

import java.sql.*;
import javax.swing.*;

public class tip_reg extends interfazGeneral {

    private JTextField txtCategoria;

    public tip_reg() {
        super("CRUD Tipo de Regalo Interface", new String[]{"Categoría"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        initializeComponents();
        tablaNombre="tipo_de_regalo";
        PK="COD_TIP_REG";
        columns=3;
    }

    private void initializeComponents() {
        txtCategoria = new JTextField(15);
        pnlAtributosExtras[0].add(txtCategoria); // Add the JTextField to the respective panel for "Categoría"
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
        String categoria = txtCategoria.getText();
        String estado = "A";

        if (isDuplicateName(categoria, "tipo_de_regalo", "CAT_REG")) {
            JOptionPane.showMessageDialog(this, "La categoría ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int codigo = generateNextCode("tipo_de_regalo", "COD_TIP_REG");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tipo_de_regalo (COD_TIP_REG, CAT_REG, ESTADO) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, categoria);
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
            txtCategoria.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCodigo.setEditable(false);
            txtCategoria.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            String categoria = txtCategoria.getText();
            String estado = lblEstado.getText();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tipo_de_regalo SET CAT_REG = ?, ESTADO = ? WHERE COD_TIP_REG = ?")) {
                pstmt.setString(1, categoria);
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
        // Limpiar los campos de texto y restablecer estado de componentes
        txtCodigo.setText("");
        txtCategoria.setText("");
        lblEstado.setText("");

        // Restablecer estado de edición de campos
        txtCodigo.setEditable(true);
        txtCategoria.setEditable(true);

        // Deshabilitar botones según el estado deseado
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
