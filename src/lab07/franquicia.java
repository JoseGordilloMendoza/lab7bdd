package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class franquicia extends interfazGeneral {

    private JComboBox<String> comboCodLoc;
    private Map<String, Integer> locMap;

    public franquicia() {
        super("CRUD Franquicia Interface", new String[]{"Localidad"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarLocalidades();
        tablaNombre="franquicia";
        PK="COD_FRAN";
        columns=3;
    }

    private void cargarLocalidades() {
        locMap = new HashMap<>();
        comboCodLoc = new JComboBox<>();

        // Agregar elemento predeterminado
        comboCodLoc.addItem("Seleccionar localidad");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_LOC, NOM_LOC FROM localidad WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codLoc = rs.getInt("COD_LOC");
                String nomLoc = rs.getString("NOM_LOC");
                locMap.put(nomLoc, codLoc);
                comboCodLoc.addItem(codLoc + " / " + nomLoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Selección del primer elemento como predeterminado
        comboCodLoc.setSelectedIndex(0);

        // Usar el método addExtraComponent para agregar componentes a los paneles correspondientes
        addExtraComponent(0, comboCodLoc);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT f.COD_FRAN, f.COD_LOC, f.ESTADO, l.NOM_LOC FROM franquicia f JOIN localidad l ON f.COD_LOC = l.COD_LOC")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codFran = rs.getInt("COD_FRAN");
                int codLoc = rs.getInt("COD_LOC");
                String nomLoc = rs.getString("NOM_LOC");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codFran);
                tableModel.addRow(new Object[]{codFran, codLoc + " / " + nomLoc, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codFran = generateNextCode("franquicia", "COD_FRAN");
            String selectedItem = (String) comboCodLoc.getSelectedItem();
            int codLoc = Integer.parseInt(selectedItem.split(" / ")[0]);
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO franquicia (COD_FRAN, COD_LOC, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, codFran);
                pstmt.setInt(2, codLoc);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de franquicia inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1 && estado.equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codLoc = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodLoc.setSelectedItem(codLoc);
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCodigo.setEditable(false);
            comboCodLoc.setEnabled(true);
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
            try {
                int codFran = Integer.parseInt(txtCodigo.getText());
                String selectedItem = (String) comboCodLoc.getSelectedItem();
                if (selectedItem != null) {
                    int codLoc = Integer.parseInt(selectedItem.split(" / ")[0]);
                    String estado = lblEstado.getText();

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE franquicia SET COD_LOC = ?, ESTADO = ? WHERE COD_FRAN = ?")) {
                    pstmt.setInt(1, codLoc);
                    pstmt.setString(2, estado);
                    pstmt.setInt(3, codFran);
                    pstmt.executeUpdate();

                        cancelar();
                        cargarDatos();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No se ha seleccionado una localidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de franquicia inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void cancelar() {
        txtCodigo.setText("");
        comboCodLoc.setSelectedIndex(0);
        lblEstado.setText("");
        txtCodigo.setEditable(true);
        comboCodLoc.setEnabled(true);
        CarFlaAct = 0;
        operation = "";
        btnActualizar.setEnabled(false);
    }
}
