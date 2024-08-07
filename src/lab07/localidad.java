package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class localidad extends interfazGeneral {

    private JComboBox<String> comboCodCiu;
    private JTextField txtNomLoc;
    private Map<String, Integer> ciudadMap;

    public localidad() {
        super("CRUD Localidad Interface", new String[]{"Ciudad", "Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarCiudades();
        tablaNombre="localidad";
        PK="COD_LOC";
        columns=4;
    }

    private void cargarCiudades() {
        ciudadMap = new HashMap<>();
        comboCodCiu = new JComboBox<>();
        comboCodCiu.addItem("Seleccionar ciudad");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_CIU, NOM_CIU FROM ciudad WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codCiu = rs.getInt("COD_CIU");
                String nomCiu = rs.getString("NOM_CIU");
                ciudadMap.put(nomCiu, codCiu);
                comboCodCiu.addItem(codCiu + " / " + nomCiu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        comboCodCiu.setSelectedIndex(0);

        txtNomLoc = new JTextField(15);
        addExtraComponent(0, comboCodCiu);
        addExtraComponent(1, txtNomLoc);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT l.COD_LOC, l.COD_CIU, l.NOM_LOC, l.ESTADO, c.NOM_CIU FROM localidad l JOIN ciudad c ON l.COD_CIU = c.COD_CIU")) {

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

    @Override
    protected void adicionar() {
        try {
            if (comboCodCiu.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Selecciona una ciudad válida.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedItem = (String) comboCodCiu.getSelectedItem();
            int codCiu = Integer.parseInt(selectedItem.split(" / ")[0]);
            String nomLoc = txtNomLoc.getText();
            String estado = "A";

            if (isDuplicateName(nomLoc, "localidad", "NOM_LOC")) {
                JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int codLoc = generateNextCode("localidad", "COD_LOC");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO localidad (COD_LOC, COD_CIU, NOM_LOC, ESTADO) VALUES (?, ?, ?, ?)")) {
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
        int selectedRow = table.getSelectedRow();String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1 && estado.equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codCiu = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodCiu.setSelectedItem(codCiu);
            txtNomLoc.setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodCiu.setEnabled(true);
            txtNomLoc.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    protected void actualizar() {
        try {
            String selectedItem = (String) comboCodCiu.getSelectedItem();
            int codCiu = Integer.parseInt(selectedItem.split(" / ")[0]);
            int codLoc = Integer.parseInt(txtCodigo.getText());
            String nomLoc = txtNomLoc.getText();
            String estado = lblEstado.getText();

            if (isDuplicateName(nomLoc, "localidad", "NOM_LOC")) {
                JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE localidad SET COD_CIU = ?, NOM_LOC = ? WHERE COD_LOC = ?")) {
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

    @Override
    protected void cancelar() {
        comboCodCiu.setSelectedIndex(0);
        txtNomLoc.setText("");
        lblEstado.setText("A");
        txtCodigo.setEditable(true);
        comboCodCiu.setEnabled(true);
        txtNomLoc.setEditable(true);
        btnActualizar.setEnabled(false);
        operation = "add";
    }
}
