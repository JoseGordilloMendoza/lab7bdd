package lab07;

import javax.swing.*;
import java.sql.*;

public class prod_ped extends interfazGeneral {

    private JTextField txtTipo;

    public prod_ped() {
        super("CRUD PROCEDENCIA DE PEDIDOS Interface", new String[]{"Tipo"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        initializeComponents();
        tablaNombre="procedencia_de_pedido";
        PK="PRO_PED_COD";
        columns=3;
    }

    private void initializeComponents() {
        txtTipo = new JTextField(15);
        pnlAtributosExtras[0].add(txtTipo); // Add the JTextField to the respective panel for "Tipo"
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM procedencia_de_pedido")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("PRO_PED_COD");
                String tipo = rs.getString("PROC_PED_TIP");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, tipo, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int codigo = Integer.parseInt(txtCodigo.getText());
        String tipo = txtTipo.getText();
        String estado = "A";

        if (!usedCodes.contains(codigo)) {
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO procedencia_de_pedido (PRO_PED_COD, PROC_PED_TIP, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, codigo);
                pstmt.setString(2, tipo);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtTipo.setText("");
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
            String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1 && estado.equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTipo.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtTipo.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            txtCodigo.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            String tipo = txtTipo.getText();
            String estado = lblEstado.getText();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE procedencia_de_pedido SET PROC_PED_TIP = ?, ESTADO = ? WHERE PRO_PED_COD = ?")) {
                pstmt.setString(1, tipo);
                pstmt.setString(2, estado);
                pstmt.setString(3, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText("");
                txtTipo.setText("");
                lblEstado.setText("");
                txtCodigo.setEditable(true);
                txtTipo.setEditable(true);
                btnActualizar.setEnabled(false);
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
        txtTipo.setText("");

        // Restablecer el estado del label de estado
        lblEstado.setText("");

        // Habilitar/Deshabilitar campos y botones según corresponda
        txtCodigo.setEditable(true);
        txtTipo.setEditable(true);
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
