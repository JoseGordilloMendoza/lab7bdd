package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class receta extends interfazGeneral {
    private JComboBox<String> comboTipoArticulo;
    private JComboBox<String> comboIngrediente;
    private Map<String, Integer> tipoArticuloMap;
    private Map<String, Integer> ingredienteMap;

    public receta() {
        super("CRUD Receta Interface", new String[]{"Tipo de Artículo", "Instrucciones", "Ingrediente"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarTipoArticulos();
        cargarIngredientes();
    }

    private void cargarTipoArticulos() {
        tipoArticuloMap = new HashMap<>();
        comboTipoArticulo = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_TIP_ART, CAT FROM tipo_de_articulo WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codTipArt = rs.getInt("COD_TIP_ART");
                String cat = rs.getString("CAT");
                tipoArticuloMap.put(cat, codTipArt);
                comboTipoArticulo.addItem(codTipArt + " / " + cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[0]);
        dataPanel.add(comboTipoArticulo, 3);

        revalidate();
        repaint();
    }

    private void cargarIngredientes() {
        ingredienteMap = new HashMap<>();
        comboIngrediente = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ING_ID, NOM_ING FROM ingrediente WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int ingId = rs.getInt("ING_ID");
                String nomIng = rs.getString("NOM_ING");
                ingredienteMap.put(nomIng, ingId);
                comboIngrediente.addItem(ingId + " / " + nomIng);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel dataPanel = (JPanel) getContentPane().getComponent(0);
        dataPanel.remove(txtAtributosExtras[2]);
        dataPanel.add(comboIngrediente, 7);

        revalidate();
        repaint();
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT r.COD_RECETA, r.COD_TIP_ART, r.INS, r.ING_ID, r.ESTADO, t.CAT, i.NOM_ING FROM receta r JOIN tipo_de_articulo t ON r.COD_TIP_ART = t.COD_TIP_ART JOIN ingrediente i ON r.ING_ID = i.ING_ID")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            txtCodigo.setText("" + generateNextCode("receta", "COD_RECETA"));
            while (rs.next()) {
                int codRec = rs.getInt("COD_RECETA");
                int codTipArt = rs.getInt("COD_TIP_ART");
                String cat = rs.getString("CAT");
                String ins = rs.getString("INS");
                int ingId = rs.getInt("ING_ID");
                String nomIng = rs.getString("NOM_ING");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codRec);
                tableModel.addRow(new Object[]{codRec, codTipArt + " / " + cat, ins, ingId + " / " + nomIng, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codRec = generateNextCode("receta", "COD_RECETA");
            String selectedItemTipoArticulo = (String) comboTipoArticulo.getSelectedItem();
            int codTipArt = Integer.parseInt(selectedItemTipoArticulo.split(" / ")[0]);
            String ins = txtAtributosExtras[1].getText();
            String selectedItemIngrediente = (String) comboIngrediente.getSelectedItem();
            int ingId = Integer.parseInt(selectedItemIngrediente.split(" / ")[0]);
            String estado = "A";

            if (!usedCodes.contains(codRec)) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO receta (COD_RECETA, COD_TIP_ART, INS, ING_ID, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
                    pstmt.setInt(1, codRec);
                    pstmt.setInt(2, codTipArt);
                    pstmt.setString(3, ins);
                    pstmt.setInt(4, ingId);
                    pstmt.setString(5, estado);
                    pstmt.executeUpdate();

                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "El registro con la clave " + codRec + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de receta inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codTipArt = tableModel.getValueAt(selectedRow, 1).toString();
            comboTipoArticulo.setSelectedItem(codTipArt);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            String codIng = tableModel.getValueAt(selectedRow, 3).toString();
            comboIngrediente.setSelectedItem(codIng);
            lblEstado.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtCodigo.setEditable(false);
            comboTipoArticulo.setEnabled(true);
            txtAtributosExtras[1].setEditable(true);
            comboIngrediente.setEnabled(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 4).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                String codTipArt = tableModel.getValueAt(selectedRow, 1).toString();
                comboTipoArticulo.setSelectedItem(codTipArt);
                txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
                String codIng = tableModel.getValueAt(selectedRow, 3).toString();
                comboIngrediente.setSelectedItem(codIng);
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codTipArt = tableModel.getValueAt(selectedRow, 1).toString();
            comboTipoArticulo.setSelectedItem(codTipArt);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            String codIng = tableModel.getValueAt(selectedRow, 3).toString();
            comboIngrediente.setSelectedItem(codIng);
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboTipoArticulo.setEnabled(false);
            txtAtributosExtras[1].setEditable(false);
            comboIngrediente.setEnabled(false);
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 4).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "Este registro ya está inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede inactivarse", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codRec = Integer.parseInt(txtCodigo.getText());
            String selectedItemTipoArticulo = (String) comboTipoArticulo.getSelectedItem();
            int codTipArt = Integer.parseInt(selectedItemTipoArticulo.split(" / ")[0]);
            String ins = txtAtributosExtras[1].getText();
            String selectedItemIngrediente = (String) comboIngrediente.getSelectedItem();
            int ingId = Integer.parseInt(selectedItemIngrediente.split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE receta SET COD_TIP_ART = ?, INS = ?, ING_ID = ?, ESTADO = ? WHERE COD_REC = ?")) {
                pstmt.setInt(1, codTipArt);
                pstmt.setString(2, ins);
                pstmt.setInt(3, ingId);
                pstmt.setString(4, estado);
                pstmt.setInt(5, codRec);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de receta inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 4).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codTipArt = tableModel.getValueAt(selectedRow, 1).toString();
            comboTipoArticulo.setSelectedItem(codTipArt);
            txtAtributosExtras[1].setText(tableModel.getValueAt(selectedRow, 2).toString());
            String codIng = tableModel.getValueAt(selectedRow, 3).toString();
            comboIngrediente.setSelectedItem(codIng);
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            comboTipoArticulo.setEnabled(false);
            txtAtributosExtras[1].setEditable(false);
            comboIngrediente.setEnabled(false);
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 4).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "Este registro ya está activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede reactivarse", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
