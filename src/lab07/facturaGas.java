package lab07;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.sql.*;

public class facturaGas extends interfazGeneral {

    private JTextField txtCantidadCal;
    private JSpinner spnPrecioGal;
    private JTextField txtCostoTotal;

    public facturaGas() {
        super("Gestión de Factura Gasolinera", new String[]{"Cantidad Cal", "Precio Gal", "Costo Total"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // Inicializar componentes
        txtCantidadCal = new JTextField();
        addExtraComponent(0, txtCantidadCal);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        spnPrecioGal = new JSpinner(spinnerModel);
        addExtraComponent(1, spnPrecioGal);

        txtCostoTotal = new JTextField();
        txtCostoTotal.setEditable(false); // El campo de costo total es de solo lectura
        addExtraComponent(2, txtCostoTotal);

        // Añadir listeners para actualizar el costo total
        txtCantidadCal.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarCostoTotal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarCostoTotal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarCostoTotal();
            }
        });

        spnPrecioGal.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                actualizarCostoTotal();
            }
        });
    }

    private void actualizarCostoTotal() {
        try {
            int cantidadCal = Integer.parseInt(txtCantidadCal.getText());
            int precioGal = (int) spnPrecioGal.getValue();
            int costoTotal = cantidadCal * precioGal;
            txtCostoTotal.setText(String.valueOf(costoTotal));
        } catch (NumberFormatException e) {
            txtCostoTotal.setText("0");
        }
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_FAC, CANT_CAL, PRECIO_GAL, COST_TOT, ESTADO FROM factura_gasolinera")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                int codFac = rs.getInt("COD_FAC");
                int cantCal = rs.getInt("CANT_CAL");
                int precioGal = rs.getInt("PRECIO_GAL");
                int costTot = rs.getInt("COST_TOT");
                String estado = rs.getString("ESTADO");

                tableModel.addRow(new Object[]{codFac, cantCal, precioGal, costTot, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codFac = generateNextCode("factura_gasolinera", "COD_FAC");
            int cantCal = Integer.parseInt(txtCantidadCal.getText());
            int precioGal = (int) spnPrecioGal.getValue();
            int costTot = Integer.parseInt(txtCostoTotal.getText());
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO factura_gasolinera (COD_FAC, CANT_CAL, PRECIO_GAL, COST_TOT, ESTADO) VALUES (?, ?, ?, ?, ?)")) {

                pstmt.setInt(1, codFac);
                pstmt.setInt(2, cantCal);
                pstmt.setInt(3, precioGal);
                pstmt.setInt(4, costTot);
                pstmt.setString(5, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad, Precio o Costo inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        String estado = table.getValueAt(selectedRow, 4).toString();
        if (selectedRow != -1) {
            if (!estado.equals("*") && !estado.equals("I")) {
                int codFac = (int) tableModel.getValueAt(selectedRow, 0);
                txtCodigo.setText(String.valueOf(codFac));
                txtCodigo.setEditable(false);
                txtCantidadCal.setText(tableModel.getValueAt(selectedRow, 1).toString());
                spnPrecioGal.setValue(tableModel.getValueAt(selectedRow, 2));
                txtCostoTotal.setText(tableModel.getValueAt(selectedRow, 3).toString());
                lblEstado.setText(tableModel.getValueAt(selectedRow, 4).toString());
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser modificado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("A")) {
            String estado = table.getValueAt(selectedRow, 4).toString();
            int codFac = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM factura_gasolinera WHERE COD_FAC = ?")) {

                    pstmt.setInt(1, codFac);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila activa para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("A")) {
            int codFac = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE factura_gasolinera SET ESTADO = 'I' WHERE COD_FAC = ?")) {

                pstmt.setInt(1, codFac);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila activa para inactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("I")) {
            int codFac = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE factura_gasolinera SET ESTADO = 'A' WHERE COD_FAC = ?")) {

                pstmt.setInt(1, codFac);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila inactiva para reactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codFac = Integer.parseInt(txtCodigo.getText());
            int cantCal = Integer.parseInt(txtCantidadCal.getText());
            int precioGal = (int) spnPrecioGal.getValue();
            int costTot = Integer.parseInt(txtCostoTotal.getText());
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE factura_gasolinera SET CANT_CAL = ?, PRECIO_GAL = ?, COST_TOT = ?, ESTADO = ? WHERE COD_FAC = ?")) {

                pstmt.setInt(1, cantCal);
                pstmt.setInt(2, precioGal);
                pstmt.setInt(3, costTot);
                pstmt.setString(4, estado);
                pstmt.setInt(5, codFac);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad, Precio o Costo inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
