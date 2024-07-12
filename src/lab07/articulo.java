package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class articulo extends interfazGeneral {
    private JTextField txtArtNom, txtPrecArt;
    private JComboBox<String> comboCodTipArt, comboCodTamArt;
    private Map<String, Long> codTipArtMap, codTamArtMap;

    public articulo() {
        super("CRUD Artículo", new String[]{"ART_NOM", "PREC_ART", "COD_TIP_ART", "COD_TAM_ART"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarCombos();
        txtArtNom.setEditable(false);
        tablaNombre="articulo";
        PK="ART_COD";
        columns=6;
    }

    private void cargarCombos() {
        txtArtNom = new JTextField(15);
        txtPrecArt = new JTextField(15);

        addExtraComponent(0, txtArtNom);
        addExtraComponent(1, txtPrecArt);

        cargarComboCodTipArt();
        cargarComboCodTamArt();

        ActionListener comboListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarNombreArticulo();
            }
        };

        comboCodTipArt.addActionListener(comboListener);
        comboCodTamArt.addActionListener(comboListener);
    }

    private void cargarComboCodTipArt() {
        codTipArtMap = new HashMap<>();
        comboCodTipArt = new JComboBox<>();
        comboCodTipArt.addItem("Seleccionar tipo de artículo");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_TIP_ART, CAT FROM tipo_de_articulo WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                long codTipArt = rs.getLong("COD_TIP_ART");
                String cat = rs.getString("CAT");
                codTipArtMap.put(cat, codTipArt);
                comboCodTipArt.addItem(codTipArt + " / " + cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(2, comboCodTipArt);
    }

    private void cargarComboCodTamArt() {
        codTamArtMap = new HashMap<>();
        comboCodTamArt = new JComboBox<>();
        comboCodTamArt.addItem("Seleccionar tamaño de artículo");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_TAM_ART, TAM FROM tamaño_del_articulo WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                long codTamArt = rs.getLong("COD_TAM_ART");
                String tam = rs.getString("TAM");
                codTamArtMap.put(tam, codTamArt);
                comboCodTamArt.addItem(codTamArt + " / " + tam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(3, comboCodTamArt);
    }

    private void actualizarNombreArticulo() {
        String tipoArticulo = (String) comboCodTipArt.getSelectedItem();
        String tamanoArticulo = (String) comboCodTamArt.getSelectedItem();

        if (tipoArticulo != null && !tipoArticulo.equals("Seleccionar tipo de artículo") && tamanoArticulo != null && !tamanoArticulo.equals("Seleccionar tamaño de artículo")) {
            String nombreArticulo = tipoArticulo.split(" / ")[1] + " " + tamanoArticulo.split(" / ")[1];
            txtArtNom.setText(nombreArticulo);
        }
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ART_COD, ART_NOM, PREC_ART, COD_TIP_ART, COD_TAM_ART, ESTADO FROM articulo")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int artCod = rs.getInt("ART_COD");
                String artNom = rs.getString("ART_NOM");
                double precArt = rs.getDouble("PREC_ART");
                long codTipArt = rs.getLong("COD_TIP_ART");
                long codTamArt = rs.getLong("COD_TAM_ART");
                String estado = rs.getString("ESTADO");

                usedCodes.add(artCod);
                tableModel.addRow(new Object[]{artCod, artNom, precArt, codTipArt, codTamArt, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            long artCod = generateNextCode("articulo", "ART_COD");
            if (usedCodes.contains((int) artCod)) {
                JOptionPane.showMessageDialog(this, "El código ya está en uso. Intente nuevamente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String artNom = txtArtNom.getText();
            double precArt = Double.parseDouble(txtPrecArt.getText());
            String selectedItemTip = (String) comboCodTipArt.getSelectedItem();
            long codTipArt = Long.parseLong(selectedItemTip.split(" / ")[0]);
            String selectedItemTam = (String) comboCodTamArt.getSelectedItem();
            long codTamArt = Long.parseLong(selectedItemTam.split(" / ")[0]);
            String estado = "A";

            if (isDuplicateName(artNom, "articulo", "ART_NOM")) {
                JOptionPane.showMessageDialog(this, "El nombre del artículo ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO articulo (ART_COD, ART_NOM, PREC_ART, COD_TIP_ART, COD_TAM_ART, ESTADO) VALUES (?, ?, ?, ?, ?, ?)")) {
                pstmt.setLong(1, artCod);
                pstmt.setString(2, artNom);
                pstmt.setDouble(3, precArt);
                pstmt.setLong(4, codTipArt);
                pstmt.setLong(5, codTamArt);
                pstmt.setString(6, estado);
                pstmt.executeUpdate();
                usedCodes.add((int) artCod); // Añadir código a usedCodes
                cargarDatos();
                cancelar();
                JOptionPane.showMessageDialog(this, "Registro añadido con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al añadir el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Formato de número inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int artCod = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, 5).toString();

            if (!estado.equals("*")) {
                txtCodigo.setText(String.valueOf(artCod));
                txtArtNom.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtPrecArt.setText(tableModel.getValueAt(selectedRow, 2).toString());

                int codTipArt = (int) tableModel.getValueAt(selectedRow, 3);
                comboCodTipArt.setSelectedItem(getComboItemText(codTipArt, codTipArtMap));

                int codTamArt = (int) tableModel.getValueAt(selectedRow, 4);
                comboCodTamArt.setSelectedItem(getComboItemText(codTamArt, codTamArtMap));

                lblEstado.setText(estado);
                txtCodigo.setEditable(false);
                CarFlaAct = 1;
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getComboItemText(int id, Map<String, Long> map) {
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }    

    @Override
    protected void actualizar() {
        try {
            long artCod = Long.parseLong(txtCodigo.getText());
            String artNom = txtArtNom.getText();
            double precArt = Double.parseDouble(txtPrecArt.getText());
            String selectedItemTip = (String) comboCodTipArt.getSelectedItem();
            long codTipArt = Long.parseLong(selectedItemTip.split(" / ")[0]);
            String selectedItemTam = (String) comboCodTamArt.getSelectedItem();
            long codTamArt = Long.parseLong(selectedItemTam.split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE articulo SET ART_NOM = ?, PREC_ART = ?, COD_TIP_ART = ?, COD_TAM_ART = ?, ESTADO = ? WHERE ART_COD = ?")) {
                pstmt.setString(1, artNom);
                pstmt.setDouble(2, precArt);
                pstmt.setLong(3, codTipArt);
                pstmt.setLong(4, codTamArt);
                pstmt.setString(5, estado);
                pstmt.setLong(6, artCod);
                pstmt.executeUpdate();
                cargarDatos();
                cancelar();
                JOptionPane.showMessageDialog(this, "Registro actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Formato de número inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
