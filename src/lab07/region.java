package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class region extends interfazGeneral {

    private JComboBox<String> comboCodPai;
    private JTextField txtNombreRegion;
    private Map<String, Integer> paisMap;

    public region() {
        super("CRUD Región Interface", new String[]{"País", "Nombre"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        txtNombreRegion = new JTextField();
        cargarPaises();
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

        // Add the combo box and the text field to the correct panel
        addExtraComponent(0, comboCodPai);
        addExtraComponent(1, txtNombreRegion);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT r.COD_REGI, r.COD_PAI, r.NOM_REGI, r.ESTADO, p.NOM_PAI FROM region r JOIN pais p ON r.COD_PAI = p.COD_PAI")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            txtCodigo.setText("" + generateNextCode("REGION", "COD_REGI"));
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
            int codRegi = generateNextCode("region", "COD_REGI");
            String selectedItem = (String) comboCodPai.getSelectedItem();
            int codPai = Integer.parseInt(selectedItem.split(" / ")[0]);
            String nomRegi = txtNombreRegion.getText();
            String estado = "A";

            if (isDuplicateName(nomRegi, "region", "NOM_REGI")) {
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
            txtNombreRegion.setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodPai.setEnabled(true);
            txtNombreRegion.setEditable(true);
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
                int codRegi = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                try (Connection conn = DatabaseConnection.getConnection()) {
                    actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_REGI", codRegi, "*");
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM region WHERE COD_REGI = ?")) {
                        pstmt.setInt(1, codRegi);
                        pstmt.executeUpdate();
                        cargarDatos();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar la región: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (selectedRow != -1) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            int codRegi = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_REGI", codRegi, "I");
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE region SET ESTADO = 'I' WHERE COD_REGI = ?")) {
                    pstmt.setInt(1, codRegi);
                    pstmt.executeUpdate();
                    cargarDatos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar la región: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1) {
            String estado = tableModel.getValueAt(selectedRow, 3).toString();
            if (estado.equals("I")) {
                JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (estado.equals("*")) {
                JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            try {
                int codRegi = Integer.parseInt(txtCodigo.getText());
                String selectedItem = (String) comboCodPai.getSelectedItem(); // Suponiendo que `comboCodPai` es el combo box para seleccionar el país
                Integer codPai = paisMap.get(selectedItem);

                // Verificar si el selectedItem está presente en el mapa
                if (codPai == null) {
                    JOptionPane.showMessageDialog(this, "El país seleccionado no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String nomRegi = txtNombreRegion.getText(); // Suponiendo que `txtNomRegi` es el campo de texto para el nombre de la región
                String estado = lblEstado.getText(); // Suponiendo que `lblEstado` es la etiqueta para el estado de la región

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
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            int codRegi = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_REGI", codRegi, "A");
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE region SET ESTADO = 'A' WHERE COD_REGI = ?")) {
                    pstmt.setInt(1, codRegi);
                    pstmt.executeUpdate();
                    cargarDatos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar la región: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow != -1) {
            String estado = tableModel.getValueAt(selectedRow, 3).toString();
            if (estado.equals("A")) {
                JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (estado.equals("*")) {
                JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void cancelar() {
        // Limpiar los campos de texto y restablecer estado de componentes
        txtCodigo.setText("");
        comboCodPai.setSelectedIndex(0); // Puedes ajustar este índice según tu lógica
        txtNombreRegion.setText("");
        lblEstado.setText("");

        // Restablecer estado de edición de campos y botones
        txtCodigo.setEditable(true);
        comboCodPai.setEnabled(true); // Ajustar según necesidad
        txtNombreRegion.setEditable(true);
        btnActualizar.setEnabled(false);

        // Restablecer variables de control
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
