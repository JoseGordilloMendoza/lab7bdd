package lab07;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class regalo extends interfazGeneral {
    private JTextField txtLimite;
    private JTextArea txtDescripcion;
    private JComboBox<String> comboTipoRegalo;

    public regalo() {
        super("CRUD Regalo Interface", new String[]{"Límite", "Descripción", "Tipo de Regalo"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarComponentes();
        cargarDatos();
        tablaNombre="regalo";
        PK="COD_REG";
        columns=5;
    }

    private void cargarComponentes() {
        txtLimite = new JTextField();
        addExtraComponent(0, txtLimite);

        txtDescripcion = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(txtDescripcion);
        addExtraComponent(1, scrollPane);

        comboTipoRegalo = new JComboBox<>();
        cargarComboTipoRegalo();
        addExtraComponent(2, comboTipoRegalo);
    }

    private void cargarComboTipoRegalo() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_TIP_REG, CAT_REG FROM tipo_de_regalo WHERE ESTADO = 'A'")) {

            while (rs.next()) {
                int codTipReg = rs.getInt("COD_TIP_REG");
                String catReg = rs.getString("CAT_REG");
                comboTipoRegalo.addItem(codTipReg + " / " + catReg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_REG, LIM, DES, COD_TIP_REG, ESTADO FROM regalo")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                int codReg = rs.getInt("COD_REG");
                int lim = rs.getInt("LIM");
                String des = rs.getString("DES");
                int codTipReg = rs.getInt("COD_TIP_REG");
                String estado = rs.getString("ESTADO");

                tableModel.addRow(new Object[]{codReg, lim, des, codTipReg, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int cod = Integer.parseInt(txtCodigo.getText());
        int lim = Integer.parseInt(txtLimite.getText());
        String des = txtDescripcion.getText();
        String selectedItemTipoRegalo = (String) comboTipoRegalo.getSelectedItem();
        int codTipReg = Integer.parseInt(selectedItemTipoRegalo.split(" / ")[0]);
        String estado = "A";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO regalo (COD_REG, LIM, DES, COD_TIP_REG, ESTADO) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setInt(1, cod);
            pstmt.setInt(2, lim);
            pstmt.setString(3, des);
            pstmt.setInt(4, codTipReg);
            pstmt.setString(5, estado);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al adicionar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1 && estado.equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtLimite.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDescripcion.setText(tableModel.getValueAt(selectedRow, 2).toString());
            comboTipoRegalo.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString() + " / " + getTipoRegaloCategoria(Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString())));
            lblEstado.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtCodigo.setEditable(false);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    protected void actualizar() {
        try {
            int cod = Integer.parseInt(txtCodigo.getText());
            int lim = Integer.parseInt(txtLimite.getText());
            String des = txtDescripcion.getText();
            String selectedItemTipoRegalo = (String) comboTipoRegalo.getSelectedItem();
            int codTipReg = Integer.parseInt(selectedItemTipoRegalo.split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE regalo SET LIM = ?, DES = ?, COD_TIP_REG = ?, ESTADO = ? WHERE COD_REG = ?")) {

                pstmt.setInt(1, lim);
                pstmt.setString(2, des);
                pstmt.setInt(3, codTipReg);
                pstmt.setString(4, estado);
                pstmt.setInt(5, cod);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Código de regalo inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private String getTipoRegaloCategoria(int codTipReg) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT CAT_REG FROM tipo_de_regalo WHERE COD_TIP_REG = ?")) {
            pstmt.setInt(1, codTipReg);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("CAT_REG");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}

