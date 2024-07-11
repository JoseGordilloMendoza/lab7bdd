package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ciudad extends interfazGeneral {

    private JComboBox<String> comboCodRegi;
    private JTextField txtNomCiu;
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
                regionMap.put(codRegi + " / " + nomRegi, codRegi);
                comboCodRegi.addItem(codRegi + " / " + nomRegi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Selección del primer elemento como predeterminado
        comboCodRegi.setSelectedIndex(0);

        txtNomCiu = new JTextField(15);

        // Usar el método addExtraComponent para agregar componentes a los paneles correspondientes
        addExtraComponent(0, comboCodRegi);
        addExtraComponent(1, txtNomCiu);

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
            int codRegi = regionMap.get(selectedItem);
            String nomCiu = txtNomCiu.getText();
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
            txtNomCiu.setText(tableModel.getValueAt(selectedRow, 2).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            comboCodRegi.setEnabled(true);
            txtNomCiu.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_CIU", codCiu, "I");

                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = 'I' WHERE COD_CIU = ?")) {
                    pstmt.setInt(1, codCiu);
                    pstmt.executeUpdate();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al inactivar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_CIU", codCiu, "*");

                    try (PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = '*' WHERE COD_CIU = ?")) {
                        pstmt.setInt(1, codCiu);
                        pstmt.executeUpdate();
                        cargarDatos();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al eliminar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            int codCiu = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "ciudad", "ESTADO", "COD_CIU", codCiu, "A");

                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE ciudad SET ESTADO = 'A' WHERE COD_CIU = ?")) {
                    pstmt.setInt(1, codCiu);
                    pstmt.executeUpdate();
                    cargarDatos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al reactivar la ciudad: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                Integer codRegi = regionMap.get(selectedItem);

                // Verificar si el selectedItem está presente en el mapa
                if (codRegi == null) {
                    JOptionPane.showMessageDialog(this, "La región seleccionada no es válida.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String nomCiu = txtNomCiu.getText();
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
        }
    }

    @Override
    protected void cancelar() {
        // Limpiar campos de texto
        txtCodigo.setText("");
        txtNomCiu.setText(""); // Limpiar el campo de nombre de la ciudad
        comboCodRegi.setSelectedIndex(0); // Restablecer la selección del combo box

        lblEstado.setText("");

        // Deseleccionar cualquier fila en la tabla
        table.clearSelection();

        // Restablecer la configuración inicial de los botones y campos de texto
        txtCodigo.setEditable(true);
        comboCodRegi.setEnabled(true); // Habilitar el combo box
        txtNomCiu.setEditable(true); // Habilitar el campo de nombre de la ciudad

        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);

        cargarDatos();
    }
}
