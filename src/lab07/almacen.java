package lab07;

import javax.swing.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class almacen extends interfazGeneral {

    private JComboBox<String> comboCodFran;
    private Map<String, Integer> franquiciaMap;

    public almacen() {
        super("CRUD Almacen Interface", new String[]{"Franquicia"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarFranquicias();
    }

    private void cargarFranquicias() {
        franquiciaMap = new HashMap<>();
        comboCodFran = new JComboBox<>();

        // Agregar elemento predeterminado
        comboCodFran.addItem("Seleccionar franquicia");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.COD_FRAN, l.NOM_LOC " +
                             "FROM franquicia f " +
                             "JOIN localidad l ON f.COD_LOC = l.COD_LOC " +
                             "WHERE f.ESTADO = 'A'")) {
            while (rs.next()) {
                int codFran = rs.getInt("COD_FRAN");
                String nomLoc = rs.getString("NOM_LOC");
                String item = codFran + " / " + nomLoc; // Corregido el formato del item
                franquiciaMap.put(item, codFran);
                comboCodFran.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Selección del primer elemento como predeterminado
        comboCodFran.setSelectedIndex(0);

        // Usar el método addExtraComponent para agregar componentes a los paneles correspondientes
        addExtraComponent(0, comboCodFran);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT a.COD_ALM, f.COD_FRAN, l.NOM_LOC, a.ESTADO " +
                             "FROM almacen a " +
                             "JOIN franquicia f ON a.COD_FRAN = f.COD_FRAN " +
                             "JOIN localidad l ON f.COD_LOC = l.COD_LOC")) {
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codAlm = rs.getInt("COD_ALM");
                int codFran = rs.getInt("COD_FRAN");
                String nomLoc = rs.getString("NOM_LOC");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codAlm);
                tableModel.addRow(new Object[]{codAlm, codFran + " / " + nomLoc, estado}); // Corregido el formato de la fila
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            // Validar que txtCodigo no esté vacío y sea un número válido
            String txtCodigoValue = txtCodigo.getText().trim();
            if (txtCodigoValue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa un código de almacén válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int nuevoCodAlm;
            try {
                nuevoCodAlm = Integer.parseInt(txtCodigoValue);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Código de almacén inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validación para asegurar que haya una franquicia seleccionada
            if (comboCodFran.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Selecciona una franquicia válida.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedItem = (String) comboCodFran.getSelectedItem();
            int codFran = Integer.parseInt(selectedItem.split(" / ")[0]);
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO almacen (COD_ALM, COD_FRAN, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, nuevoCodAlm);
                pstmt.setInt(2, codFran);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de almacén inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codFran = tableModel.getValueAt(selectedRow, 1).toString();
            comboCodFran.setSelectedItem(codFran);
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCodigo.setEditable(false);
            comboCodFran.setEnabled(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                int codAlm = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM almacen WHERE COD_ALM = ?")) {
                    pstmt.setInt(1, codAlm);
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            int codAlm = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE almacen SET ESTADO = 'I' WHERE COD_ALM = ?")) {
                pstmt.setInt(1, codAlm);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            int codAlm = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE almacen SET ESTADO = 'A' WHERE COD_ALM = ?")) {
                pstmt.setInt(1, codAlm);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            int codAlm = Integer.parseInt(txtCodigo.getText().trim());
            String selectedItem = (String) comboCodFran.getSelectedItem();
            int codFran = Integer.parseInt(selectedItem.split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE almacen SET COD_FRAN = ?, ESTADO = ? WHERE COD_ALM = ?")) {
                pstmt.setInt(1, codFran);
                pstmt.setString(2, estado);
                pstmt.setInt(3, codAlm);
                pstmt.executeUpdate();

                cancelar();
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void cancelar() {
        // Limpiar campos de texto
        txtCodigo.setText("");
        comboCodFran.setSelectedIndex(0); // Restablecer la selección del combo box
        lblEstado.setText("");
        txtCodigo.setEditable(true);
        comboCodFran.setEnabled(true);
        CarFlaAct = 0;
        btnActualizar.setEnabled(false);
    }
}
