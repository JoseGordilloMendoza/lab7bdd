package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class localidad extends interfazGeneral {

    private JComboBox<String> comboCodCiu;
    private Map<String, Integer> ciudadMap;

    public localidad() {
        super("CRUD Localidad Interface", new String[]{"Ciudad", "Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarCiudades();
    }

    private void cargarCiudades() {
        ciudadMap = new HashMap<>();
        comboCodCiu = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_CIU, NOM_CIU FROM ciudad WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codCiu = rs.getInt("COD_CIU");
                String nomCiu = rs.getString("NOM_CIU");
                ciudadMap.put(nomCiu, codCiu);
                comboCodCiu.addItem(codCiu + " / " + nomCiu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodCiu, 3);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT l.COD_LOC, l.COD_CIU, l.NOM_LOC, l.ESTADO, c.NOM_CIU FROM localidad l JOIN ciudad c ON l.COD_CIU = c.COD_CIU")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codLoc = rs.getInt("COD_LOC");
                int codCiu = rs.getInt("COD_CIU");
                String nomCiu = rs.getString("NOM_CIU");
                String nomLoc = rs.getString("NOM_LOC");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codLoc);
                tableModel.addRow(new Object[]{codLoc, codCiu + " / " + nomCiu, nomLoc, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getCiudadNameById(int codCiu) {
        for (Map.Entry<String, Integer> entry : ciudadMap.entrySet()) {
            if (entry.getValue() == codCiu) {
                return entry.getKey();
            }
        }
        return "";
    }

    @Override
    protected void adicionar() {
        try {
            String selectedItem = (String) comboCodCiu.getSelectedItem();
            int codCiu = Integer.parseInt(selectedItem.split(" / ")[0]);
            String nomLoc = txtAtributosExtras[1].getText();
            String estado = "A";

            if (isDuplicateName(nomLoc, "localidad", "NOM_LOC")) {
                JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Generar el próximo código disponible
            int codLoc = generateNextCode("localidad", "COD_LOC");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO localidad (COD_LOC, COD_CIU, NOM_LOC, ESTADO) VALUES (?, ?, ?, ?)")) {
                pstmt.setInt(1, codLoc);
                pstmt.setInt(2, codCiu);
                pstmt.setString(3, nomLoc);
                pstmt.setString(4, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de localidad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codCiu = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodCiu.setSelectedItem(codCiu);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodCiu.setEnabled(true);
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
                int codLoc = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                String codCiu = tableModel.getValueAt(selectedRow, 1).toString();
                String nomLoc = tableModel.getValueAt(selectedRow, 2).toString();

                actualizarEstado("franquicia", "ESTADO", "COD_LOC", codLoc, "*", "*");

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE localidad SET ESTADO = '*' WHERE COD_LOC = ?")) {
                    pstmt.setInt(1, codLoc);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            int codLoc = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            String codCiu = tableModel.getValueAt(selectedRow, 1).toString();
            String nomLoc = tableModel.getValueAt(selectedRow, 2).toString();

            actualizarEstado("franquicia", "ESTADO", "COD_LOC", codLoc, "I", "*");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE localidad SET ESTADO = 'I' WHERE COD_LOC = ?")) {
                pstmt.setInt(1, codLoc);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            int codLoc = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            String codCiu = tableModel.getValueAt(selectedRow, 1).toString();
            String nomLoc = tableModel.getValueAt(selectedRow, 2).toString();

            actualizarEstado("franquicia", "ESTADO", "COD_LOC", codLoc, "A", "*");

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE localidad SET ESTADO = 'A' WHERE COD_LOC = ?")) {
                pstmt.setInt(1, codLoc);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                int codLoc = Integer.parseInt(txtCodigo.getText());
                String selectedItem = (String) comboCodCiu.getSelectedItem();
                int codCiu = Integer.parseInt(selectedItem.split(" / ")[0]);
                String nomLoc = txtAtributosExtras[1].getText();
                String estado = lblEstado.getText();

                if (isDuplicateName(nomLoc, "localidad", "NOM_LOC")) {
                    JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE localidad SET COD_CIU = ?, NOM_LOC = ? WHERE COD_LOC = ?")) {
                    pstmt.setInt(1, codCiu);
                    pstmt.setString(2, nomLoc);
                    pstmt.setInt(3, codLoc);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de localidad inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
