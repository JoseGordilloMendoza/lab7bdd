package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ciudad extends interfazGeneral {

    private JComboBox<String> comboCodRegi;
    private Map<String, Integer> regionMap;

    public ciudad() {
        super("CRUD Ciudad Interface", new String[]{"Región", "Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarRegiones();
    }

    private void cargarRegiones() {
        regionMap = new HashMap<>();
        comboCodRegi = new JComboBox<>();

        // Agregar elemento predeterminado
        comboCodRegi.addItem("Seleccionar región");

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_REGI, NOM_REGI FROM region WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codRegi = rs.getInt("COD_REGI");
                String nomRegi = rs.getString("NOM_REGI");
                regionMap.put(nomRegi, codRegi);
                comboCodRegi.addItem(codRegi + " / " + nomRegi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Selección del primer elemento como predeterminado
        comboCodRegi.setSelectedIndex(0);

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodRegi, 3);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT c.COD_CIU, c.COD_REGI, c.NOM_CIU, c.ESTADO, r.NOM_REGI FROM ciudad c JOIN region r ON c.COD_REGI = r.COD_REGI")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codCiu = rs.getInt("COD_CIU");
                int codRegi = rs.getInt("COD_REGI");
                String nomRegi = rs.getString("NOM_REGI");
                String nomCiu = rs.getString("NOM_CIU");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codCiu);
                tableModel.addRow(new Object[]{codCiu, codRegi + " / " + nomRegi, nomCiu, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            // Validación para asegurar que haya un elemento seleccionado
            if (comboCodRegi.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Selecciona una región válida.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedItem = (String) comboCodRegi.getSelectedItem();
            int codRegi = Integer.parseInt(selectedItem.split(" / ")[0]);
            String nomCiu = txtAtributosExtras[1].getText();
            String estado = "A";

            if (isDuplicateName(nomCiu, "ciudad", "NOM_CIU")) {
                JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int codCiu = generateNextCode("ciudad", "COD_CIU");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ciudad (COD_CIU, COD_REGI, NOM_CIU, ESTADO) VALUES (?, ?, ?, ?)")) {
                pstmt.setInt(1, codCiu);
                pstmt.setInt(2, codRegi);
                pstmt.setString(3, nomCiu);
                pstmt.setString(4, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de ciudad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codRegi = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodRegi.setSelectedItem(codRegi);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodRegi.setEnabled(true);
            txtAtributosExtras[1].setEditable(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                actualizarEstado("localidad", "ESTADO", "COD_CIU", codCiu, "*", "*");

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = '*' WHERE COD_CIU = ?")) {
                    pstmt.setInt(1, codCiu);
                    pstmt.executeUpdate();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            actualizarEstado("localidad", "ESTADO", "COD_CIU", codCiu, "I", "*");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = 'I' WHERE COD_CIU = ?")) {
                pstmt.setInt(1, codCiu);
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            actualizarEstado("localidad", "ESTADO", "COD_CIU", codCiu, "A", "*");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = 'A' WHERE COD_CIU = ?")) {
                pstmt.setInt(1, codCiu);
                pstmt.executeUpdate();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            try {
                int codCiu = Integer.parseInt(txtCodigo.getText());
                String selectedItem = (String) comboCodRegi.getSelectedItem();
                int codRegi = Integer.parseInt(selectedItem.split(" / ")[0]);
                String nomCiu = txtAtributosExtras[1].getText();
                String estado = lblEstado.getText();

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET COD_REGI = ?, NOM_CIU = ?, ESTADO = ? WHERE COD_CIU = ?")) {
                    pstmt.setInt(1, codRegi);
                    pstmt.setString(2, nomCiu);
                    pstmt.setString(3, estado);
                    pstmt.setInt(4, codCiu);
                    pstmt.executeUpdate();

                    cancelar();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de ciudad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debes seleccionar una opción para el botón Grabar Cambios", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        txtCodigo.setText("");
        comboCodRegi.setSelectedIndex(0);
        lblEstado.setText("A");
        txtCodigo.setEditable(true);
        comboCodRegi.setEnabled(true);
        CarFlaAct = 0;
        operation = "";
        btnActualizar.setEnabled(false);
    }
}
