package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class region extends interfazGeneral {

    private JComboBox<String> comboCodPai;
    private Map<String, Integer> paisMap;
    

    public region() {
        super("CRUD Región Interface", new String[]{"País", "Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarPaises();
        txtCodigo = new JTextField(generateNextCode());
    }

    private void cargarPaises() {
        paisMap = new HashMap<>();
        comboCodPai = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COD_PAI, NOM_PAI FROM pais WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codPai = rs.getInt("COD_PAI");
                String nomPai = rs.getString("NOM_PAI");
                paisMap.put(nomPai, codPai);
                comboCodPai.addItem(codPai + " / " + nomPai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboCodPai, 3);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT r.COD_REGI, r.COD_PAI, r.NOM_REGI, r.ESTADO, p.NOM_PAI FROM region r JOIN pais p ON r.COD_PAI = p.COD_PAI")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codRegi = rs.getInt("COD_REGI");
                int codPai = rs.getInt("COD_PAI");
                String nomPai = rs.getString("NOM_PAI");
                String nomRegi = rs.getString("NOM_REGI");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codRegi);
                tableModel.addRow(new Object[]{codRegi, codPai + " / " + nomPai, nomRegi, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getPaisNameById(int codPai) {
        for (Map.Entry<String, Integer> entry : paisMap.entrySet()) {
            if (entry.getValue() == codPai) {
                return entry.getKey();
            }
        }
        return "";
    }

    @Override
    protected void adicionar() {
        try {
            int codRegi = generateNextCode();
            String selectedItem = (String) comboCodPai.getSelectedItem();
            int codPai = Integer.parseInt(selectedItem.split(" / ")[0]);
            String nomRegi = txtAtributosExtras[1].getText();
            String estado = "A";

            if (isDuplicateName(nomRegi)) {
                JOptionPane.showMessageDialog(this, "El nombre ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!usedCodes.contains(codRegi)) {
                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO region (COD_REGI, COD_PAI, NOM_REGI, ESTADO) VALUES (?, ?, ?, ?)")) {
                    pstmt.setInt(1, codRegi);
                    pstmt.setInt(2, codPai);
                    pstmt.setString(3, nomRegi);
                    pstmt.setString(4, estado);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "El registro con la clave " + codRegi + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de región inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codPai = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodPai.setSelectedItem(codPai);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodPai.setEnabled(true);
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
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                String codPai = tableModel.getValueAt(selectedRow, 1).toString();
                comboCodPai.setSelectedItem(codPai);
                txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
                lblEstado.setText("*");
                operation = "mod";
                CarFlaAct = 1;
                btnActualizar.setEnabled(true);
                actualizar();
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codPai = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodPai.setSelectedItem(codPai);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboCodPai.setEnabled(false);
            txtAtributosExtras[1].setEditable(false);
            btnActualizar.setEnabled(true);
            actualizar();
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
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codPai = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodPai.setSelectedItem(codPai);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            actualizar();
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
                int codRegi = Integer.parseInt(txtCodigo.getText());
                String selectedItem = (String) comboCodPai.getSelectedItem();
                int codPai = Integer.parseInt(selectedItem.split(" / ")[0]);
                String nomRegi = txtAtributosExtras[1].getText();
                String estado = lblEstado.getText();

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE region SET COD_PAI = ?, NOM_REGI = ?, ESTADO = ? WHERE COD_REGI = ?")) {
                    pstmt.setInt(1, codPai);
                    pstmt.setString(2, nomRegi);
                    pstmt.setString(3, estado);
                    pstmt.setInt(4, codRegi);
                    pstmt.executeUpdate();

                    cancelar();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código de región inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void cancelar() {
        txtCodigo.setText("");
        comboCodPai.setSelectedIndex(0);
        txtAtributosExtras[1].setText("");
        lblEstado.setText("A");
        txtCodigo.setEditable(true);
        comboCodPai.setEnabled(true);
        txtAtributosExtras[1].setEditable(true);
        CarFlaAct = 0;
        operation = "";
        btnActualizar.setEnabled(false);
    }

    private boolean isDuplicateName(String nombre) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM region WHERE NOM_REGI = ?")) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private int generateNextCode() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(COD_REGI) FROM region")) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default code if table is empty
    }
}
