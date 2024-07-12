package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ingredienteAlmacen extends interfazGeneral {

    private JTextField txtStoAct, txtStoMin, txtStoMax, txtStoSeg;
    private JComboBox<String> comboIngId, comboCodAlm;
    private Map<String, Integer> ingIdMap, codAlmMap;

    public ingredienteAlmacen() {
        super("CRUD Ingrediente Almacén", new String[]{"STO_ACT", "STO_MIN", "STO_MAX", "STO_SEG", "ING_ID", "COD_ALM"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarCombos();
        tablaNombre="ingrediente_almacen";
        PK="COD_ING_ALM";
        columns=8;
    }

    private void cargarCombos() {
        txtStoAct = new JTextField(15);
        txtStoMin = new JTextField(15);
        txtStoMax = new JTextField(15);
        txtStoSeg = new JTextField(15);

        addExtraComponent(0, txtStoAct);
        addExtraComponent(1, txtStoMin);
        addExtraComponent(2, txtStoMax);
        addExtraComponent(3, txtStoSeg);

        cargarComboIngId();
        cargarComboCodAlm();
    }

    private void cargarComboIngId() {
        ingIdMap = new HashMap<>();
        comboIngId = new JComboBox<>();
        comboIngId.addItem("Seleccionar ingrediente");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ING_ID, NOM_ING FROM ingrediente WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int ingId = rs.getInt("ING_ID");
                String nomIng = rs.getString("NOM_ING");
                ingIdMap.put(nomIng, ingId);
                comboIngId.addItem(ingId + " / " + nomIng);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(4, comboIngId); // Agregar al panel de atributos extras
    }

    private void cargarComboCodAlm() {
        codAlmMap = new HashMap<>();
        comboCodAlm = new JComboBox<>();
        comboCodAlm.addItem("Seleccionar almacén");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_ALM FROM almacen WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codAlm = rs.getInt("COD_ALM");
                String nomAlm = ""+codAlm;
                codAlmMap.put(nomAlm, codAlm);
                comboCodAlm.addItem(codAlm + "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addExtraComponent(5, comboCodAlm); // Agregar al panel de atributos extras
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_ING_ALM, STO_ACT, STO_MIN, STO_MAX, STO_SEG, ING_ID, COD_ALM, ESTADO FROM ingrediente_almacen")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                int codIngAlm = rs.getInt("COD_ING_ALM");
                int stoAct = rs.getInt("STO_ACT");
                int stoMin = rs.getInt("STO_MIN");
                int stoMax = rs.getInt("STO_MAX");
                int stoSeg = rs.getInt("STO_SEG");
                int ingId = rs.getInt("ING_ID");
                int codAlm = rs.getInt("COD_ALM");
                String estado = rs.getString("ESTADO");

                usedCodes.add(codIngAlm); // Usando COD_ING_ALM como código de identificación
                tableModel.addRow(new Object[]{codIngAlm, stoAct, stoMin, stoMax, stoSeg, ingId, codAlm, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        try {
            int codingalm=Integer.parseInt(txtCodigo.getText());
            int stoAct = Integer.parseInt(txtStoAct.getText());
            int stoMin = Integer.parseInt(txtStoMin.getText());
            int stoMax = Integer.parseInt(txtStoMax.getText());
            int stoSeg = Integer.parseInt(txtStoSeg.getText());
            String selectedItemIng = (String) comboIngId.getSelectedItem();
            int ingId = Integer.parseInt(selectedItemIng.split(" / ")[0]);
            String selectedItemAlm = (String) comboCodAlm.getSelectedItem();
            int codAlm = Integer.parseInt(selectedItemAlm.split(" / ")[0]);
            String estado = "A";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ingrediente_almacen (COD_ING_ALM, STO_ACT, STO_MIN, STO_MAX, STO_SEG, ING_ID, COD_ALM, ESTADO) VALUES (?,?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, codingalm);
                pstmt.setInt(2, stoAct);
                pstmt.setInt(3, stoMin);
                pstmt.setInt(4, stoMax);
                pstmt.setInt(5, stoSeg);
                pstmt.setInt(6, ingId);
                pstmt.setInt(7, codAlm);
                pstmt.setString(8, estado);
                pstmt.executeUpdate();

                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Formato de número inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow != -1) {
            int codIngAlm = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

            // Verificar si el estado permite la modificación (estado != '*')
            if (!estado.equals("*")&& estado.equals("A")) {
                txtCodigo.setText(String.valueOf(codIngAlm));
                txtStoAct.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtStoMin.setText(tableModel.getValueAt(selectedRow, 2).toString());
                txtStoMax.setText(tableModel.getValueAt(selectedRow, 3).toString());
                txtStoSeg.setText(tableModel.getValueAt(selectedRow, 4).toString());

                int ingId = (int) tableModel.getValueAt(selectedRow, 5);
                comboIngId.setSelectedItem(getComboItemText(ingId, ingIdMap));

                int codAlm = (int) tableModel.getValueAt(selectedRow, 6);
                comboCodAlm.setSelectedItem(getComboItemText(codAlm, codAlmMap));

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

    private String getComboItemText(int id, Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null; // Manejo de caso donde no se encuentra el ítem
    }


    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codIngAlm = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, 7).toString();
            if (!estado.equals("*")) {
                int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente_almacen SET ESTADO = '*' WHERE COD_ING_ALM = ?")) {
                        pstmt.setInt(1, codIngAlm);
                        pstmt.executeUpdate();
                        cargarDatos();
                        cancelar();
                        JOptionPane.showMessageDialog(this, "Registro eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al eliminar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se puede eliminar un registro marcado como eliminado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codIngAlm = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente_almacen SET ESTADO = 'I' WHERE COD_ING_ALM = ?")) {
                pstmt.setInt(1, codIngAlm);
                pstmt.executeUpdate();
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Registro inactivado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al inactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para inactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codIngAlm = (int) tableModel.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente_almacen SET ESTADO = 'A' WHERE COD_ING_ALM = ?")) {
                pstmt.setInt(1, codIngAlm);
                pstmt.executeUpdate();
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Registro reactivado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al reactivar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro para reactivar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        try {
            int codIngAlm = Integer.parseInt(txtCodigo.getText());
            int stoAct = Integer.parseInt(txtStoAct.getText());
            int stoMin = Integer.parseInt(txtStoMin.getText());
            int stoMax = Integer.parseInt(txtStoMax.getText());
            int stoSeg = Integer.parseInt(txtStoSeg.getText());

            // Verificar que haya un ítem seleccionado en comboIngId
            int ingId = -1;
            String selectedItemIng = (String) comboIngId.getSelectedItem();
            if (selectedItemIng != null && selectedItemIng.contains("/")) {
                ingId = Integer.parseInt(selectedItemIng.split(" / ")[0]);
            }

            // Verificar que haya un ítem seleccionado en comboCodAlm
            int codAlm = -1;
            String selectedItemAlm = (String) comboCodAlm.getSelectedItem();
            if (selectedItemAlm != null && selectedItemAlm.contains("/")) {
                codAlm = Integer.parseInt(selectedItemAlm.split(" / ")[0]);
            }

            String estado = lblEstado.getText(); // Verificar que este valor sea correcto

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE ingrediente_almacen SET STO_ACT = ?, STO_MIN = ?, STO_MAX = ?, STO_SEG = ?, ING_ID = ?, COD_ALM = ?, ESTADO = ? WHERE COD_ING_ALM = ?")) {
                pstmt.setInt(1, stoAct);
                pstmt.setInt(2, stoMin);
                pstmt.setInt(3, stoMax);
                pstmt.setInt(4, stoSeg);
                pstmt.setInt(5, ingId);
                pstmt.setInt(6, codAlm);
                pstmt.setString(7, estado);
                pstmt.setInt(8, codIngAlm);

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
