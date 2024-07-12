package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class receta_det extends interfazGeneral {
    private JComboBox<String> comboTipoArticulo;
    private JComboBox<String> comboIngrediente;
    private JComboBox<String> comboRecetas;
    private JTextField txtInstrucciones;
    private Map<String, Integer> tipoArticuloMap;
    private Map<String, Integer> ingredienteMap;
    private Map<String, Integer> recetaMap;

    public receta_det() {
        super("CRUD Receta Detalle Interface", new String[]{"Tipo de Artículo", "Instrucciones", "Ingrediente","Receta"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarTipoArticulos();
        cargarIngredientes();
        cargarInstrucciones();
        cargarRecetas();
        cargarDatos();
        tablaNombre="receta_detalle";
        PK="COD_REC_DET";
        columns=6;
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

        addExtraComponent(0, comboTipoArticulo);
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

        addExtraComponent(2, comboIngrediente);
    }
    
    private void cargarRecetas() {
        recetaMap = new HashMap<>();
        comboRecetas = new JComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_RECETA, NOMBRE FROM receta WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codRec = rs.getInt("COD_RECETA");
                String nomRec = rs.getString("NOMBRE");
                recetaMap.put(nomRec, codRec);
                comboRecetas.addItem(codRec + " / " + nomRec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(3, comboRecetas);
    }


    private void cargarInstrucciones() {
        txtInstrucciones = new JTextField(20);
        addExtraComponent(1, txtInstrucciones);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT r.COD_REC_DET, r.COD_TIP_ART, r.INS, r.ING_ID, r.COD_RECETA, r.ESTADO, t.CAT, i.NOM_ING FROM receta_detalle r JOIN tipo_de_articulo t ON r.COD_TIP_ART = t.COD_TIP_ART JOIN ingrediente i ON r.ING_ID = i.ING_ID")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            txtCodigo.setText("" + generateNextCode("receta_detalle", "COD_REC_DET"));
            while (rs.next()) {
                int codRecDet = rs.getInt("COD_REC_DET");
                int codTipArt = rs.getInt("COD_TIP_ART");
                String cat = rs.getString("CAT");
                String ins = rs.getString("INS");
                int ingId = rs.getInt("ING_ID");
                String nomIng = rs.getString("NOM_ING");
                int codRec = rs.getInt("COD_RECETA");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codRecDet);
                tableModel.addRow(new Object[]{codRecDet, codTipArt + " / " + cat, ins, ingId + " / " + nomIng,codRec ,estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicateIngredient(int codTipArt, int ingId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM receta_detalle WHERE COD_TIP_ART = ? AND ING_ID = ? AND ESTADO != '*'")) {
            pstmt.setInt(1, codTipArt);
            pstmt.setInt(2, ingId);
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

    @Override
    protected void adicionar() {
        try {
            int codRecDet = generateNextCode("receta_detalle", "COD_REC_DET");
            String selectedItemTipoArticulo = (String) comboTipoArticulo.getSelectedItem();
            int codTipArt = Integer.parseInt(selectedItemTipoArticulo.split(" / ")[0]);
            String ins = txtInstrucciones.getText();
            String selectedItemIngrediente = (String) comboIngrediente.getSelectedItem();
            int ingId = Integer.parseInt(selectedItemIngrediente.split(" / ")[0]);
            String estado = "A";
            String recetaSelec = (String) comboRecetas.getSelectedItem();
            int codRec = Integer.parseInt(recetaSelec.split(" / ")[0]);
            if (isDuplicateIngredient(codTipArt, ingId)) {
                JOptionPane.showMessageDialog(this, "El artículo ya tiene este ingrediente asignado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!usedCodes.contains(codRecDet)) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO receta_detalle (COD_REC_DET, COD_TIP_ART, INS, ING_ID, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
                    pstmt.setInt(1, codRecDet);
                    pstmt.setInt(2, codTipArt);
                    pstmt.setString(3, ins);
                    pstmt.setInt(4, ingId);
                    pstmt.setInt(5, codRec);
                    pstmt.setString(6, estado);
                    pstmt.executeUpdate();

                    cargarDatos();
                    // Limpiar campos después de insertar
                    txtCodigo.setText("" + generateNextCode("receta_detalle", "COD_REC_DET"));
                    comboTipoArticulo.setSelectedIndex(0);
                    txtInstrucciones.setText("");
                    comboIngrediente.setSelectedIndex(0);
                    comboRecetas.setSelectedIndex(0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "El registro con la clave " + codRecDet + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de receta detalle inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 5).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            String codTipArt = tableModel.getValueAt(selectedRow, 1).toString();
            comboTipoArticulo.setSelectedItem(codTipArt);
            txtInstrucciones.setText(tableModel.getValueAt(selectedRow, 2).toString());
            String codIng = tableModel.getValueAt(selectedRow, 3).toString();
            comboIngrediente.setSelectedItem(codIng);
            
            String codRec= tableModel.getValueAt(selectedRow, 4).toString();
            comboRecetas.setSelectedItem(codRec);
            lblEstado.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtCodigo.setEditable(false);
            comboTipoArticulo.setEnabled(true);
            txtInstrucciones.setEditable(true);
            comboIngrediente.setEnabled(true);
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
            int codRecDet = Integer.parseInt(txtCodigo.getText());
            String selectedItemTipoArticulo = (String) comboTipoArticulo.getSelectedItem();
            int codTipArt = Integer.parseInt(selectedItemTipoArticulo.split(" / ")[0]);
            String ins = txtInstrucciones.getText();
            String selectedItemIngrediente = (String) comboIngrediente.getSelectedItem();
            int ingId = Integer.parseInt(selectedItemIngrediente.split(" / ")[0]);
            String estado = lblEstado.getText();

            if (isDuplicateIngredient(codTipArt, ingId)) {
                JOptionPane.showMessageDialog(this, "El artículo ya tiene este ingrediente asignado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE receta_detalle SET COD_TIP_ART = ?, INS = ?, ING_ID = ?, ESTADO = ? WHERE COD_REC_DET = ?")) {
                pstmt.setInt(1, codTipArt);
                pstmt.setString(2, ins);
                pstmt.setInt(3, ingId);
                pstmt.setString(4, estado);
                pstmt.setInt(5, codRecDet);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de receta detalle inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
