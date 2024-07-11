package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class facturaGas extends interfazGeneral {

    public facturaGas() {
        super("Gestión de Factura Gasolinera", new String[]{"Cantidad Cal", "Precio Gal", "Costo Total", "Estado"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
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
            int cantCal = Integer.parseInt(getTextFromPanel(0));
            int precioGal = Integer.parseInt(getTextFromPanel(1));
            int costTot = Integer.parseInt(getTextFromPanel(2));
            String estado = getTextFromPanel(3);

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
            JOptionPane.showMessageDialog(this, "Código de factura o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codFac = (int) tableModel.getValueAt(selectedRow, 0);
            txtCodigo.setText(String.valueOf(codFac));
            txtCodigo.setEditable(false);
            setTextInPanel(0, tableModel.getValueAt(selectedRow, 1).toString());
            setTextInPanel(1, tableModel.getValueAt(selectedRow, 2).toString());
            setTextInPanel(2, tableModel.getValueAt(selectedRow, 3).toString());
            setTextInPanel(3, tableModel.getValueAt(selectedRow, 4).toString());
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
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
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
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
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
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
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codFac = Integer.parseInt(txtCodigo.getText());
            int cantCal = Integer.parseInt(getTextFromPanel(0));
            int precioGal = Integer.parseInt(getTextFromPanel(1));
            int costTot = Integer.parseInt(getTextFromPanel(2));
            String estado = getTextFromPanel(3);

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
            JOptionPane.showMessageDialog(this, "Código de factura o datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        setTextInPanel(0, "");
        setTextInPanel(1, "");
        setTextInPanel(2, "");
        setTextInPanel(3, "");
    }

    @Override
    protected void salir() {
        super.salir();
        setTextInPanel(0, "");
        setTextInPanel(1, "");
        setTextInPanel(2, "");
        setTextInPanel(3, "");
    }

    private String getTextFromPanel(int index) {
        if (index >= 0 && index < pnlAtributosExtras.length) {
            Component component = pnlAtributosExtras[index].getComponent(0);
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                return textField.getText();
            }
        }
        return "";
    }

    private void setTextInPanel(int index, String text) {
        if (index >= 0 && index < pnlAtributosExtras.length) {
            Component component = pnlAtributosExtras[index].getComponent(0);
            if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                textField.setText(text);
            }
        }
    }
}
