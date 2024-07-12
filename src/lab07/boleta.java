package lab07;

import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;

public class boleta extends interfazGeneral {

    private JDateChooser dateChooser;
    private JComboBox<String> comboRegSco;
    private JComboBox<String> comboPedId;
    private JTextField txtTotal;

    public boleta() {
        super("Gestión de Boletas", new String[]{"Fecha Boleta", "Registro SCO", "Pedido ID", "Total"});
        cargarComponentes();
        cargarDatos();
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        tablaNombre="boleta";
        PK="COD_BOL";
        columns=6;
    }

    private void cargarComponentes() {
        dateChooser = new JDateChooser();
        comboRegSco = new JComboBox<>();
        comboPedId = new JComboBox<>();
        txtTotal = new JTextField();
        txtTotal.setEditable(false);

        // Cargar datos de Registro SCO en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_REG_SCO FROM registro_scooter WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboRegSco.addItem(rs.getInt("COD_REG_SCO")+"");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Pedido ID en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT PED_ID FROM pedido_base WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboPedId.addItem(String.valueOf(rs.getInt("PED_ID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        comboRegSco.addActionListener(e -> actualizarTotal());
        comboPedId.addActionListener(e -> actualizarTotal());

        // Agregar componentes a los paneles correspondientes
        addExtraComponent(0, dateChooser);
        addExtraComponent(1, comboRegSco);
        addExtraComponent(2, comboPedId);
        addExtraComponent(3, txtTotal);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_BOL, FECH_BOL, COD_REG_SCO, PED_ID, TOTAL, ESTADO FROM boleta")) {
            tableModel.setRowCount(0); // Limpiar la tabla antes de cargar datos
            while (rs.next()) {
                // Obtener nombre del registro SCO usando su código
                int codRegSco = rs.getInt("COD_REG_SCO");
                tableModel.addRow(new Object[]{
                    rs.getInt("COD_BOL"),
                    rs.getString("FECH_BOL"),
                    codRegSco + "",
                    rs.getInt("PED_ID"),
                    rs.getBigDecimal("TOTAL"),
                    rs.getString("ESTADO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int codBol = Integer.parseInt(txtCodigo.getText());
        String fechaBoleta = formatDate(dateChooser.getDate());
        String regScoItem = (String) comboRegSco.getSelectedItem();
        int codRegSco = Integer.parseInt(regScoItem.split(" / ")[0]);
        int pedId = Integer.parseInt((String) comboPedId.getSelectedItem());
        String estado = "A";
        double total = calcularTotal(codRegSco, pedId);

        // Comprobar si el pedido ya tiene una boleta
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM boleta WHERE PED_ID = ? AND ESTADO = 'A'")) {
            checkStmt.setInt(1, pedId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Este pedido ya tiene una boleta activa.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO boleta (COD_BOL, FECH_BOL, COD_REG_SCO, PED_ID, TOTAL, ESTADO) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, codBol);
            pstmt.setString(2, fechaBoleta);
            pstmt.setInt(3, codRegSco);
            pstmt.setInt(4, pedId);
            pstmt.setDouble(5, total);
            pstmt.setString(6, estado);
            pstmt.executeUpdate();
            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codBol = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, 5).toString();

            if (!estado.equals("*") && !estado.equals("I")) {
                txtCodigo.setText(codBol + "");
                txtCodigo.setEditable(false);
                dateChooser.setDate(parseDate(tableModel.getValueAt(selectedRow, 1).toString()));
                int codRegSco = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 2)).split(" / ")[0]);
                comboRegSco.setSelectedItem(getComboItemText(codRegSco, comboRegSco));
                int pedId = (int) tableModel.getValueAt(selectedRow, 3);
                comboPedId.setSelectedItem(String.valueOf(pedId));
                txtTotal.setText(tableModel.getValueAt(selectedRow, 4).toString());
                lblEstado.setText(estado);
                actualizarTotal();
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Esta boleta no puede ser modificada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una boleta para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        dateChooser.setDate(null);
        comboRegSco.setSelectedIndex(0);
        comboPedId.setSelectedIndex(0);
        txtTotal.setText("");
    }

    private String formatDate(Date date) {
        if (date != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
        return null;
    }

    private Date parseDate(String date) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(date);
        } catch (java.text.ParseException ex) {
            return null;
        }
    }

    private String getComboItemText(int id, JComboBox<String> combo) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(String.valueOf(id))) {
                return item;
            }
        }
        return null;
    }

    private double calcularTotal(int codRegSco, int pedId) {
        double total = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Calcular la suma de los subtotales de los artículos del pedido
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT SUM(SUBTOTAL) AS TOTAL_PEDIDO FROM pedido_art WHERE PED_ID = ? AND ESTADO = 'A'")) {
                pstmt.setInt(1, pedId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    total = rs.getDouble("TOTAL_PEDIDO");
                }
            }

            // Obtener el 10% del costo total de la factura correspondiente al código de registro SCO
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT fg.COST_TOT FROM factura_gasolinera fg " +
                    "JOIN registro_scooter rs ON fg.COD_FAC = rs.COD_FAC " +
                    "WHERE rs.COD_REG_SCO = ? AND fg.ESTADO = 'A' AND rs.ESTADO = 'A'")) {
                pstmt.setInt(1, codRegSco);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    total += rs.getDouble("COST_TOT") * 0.1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    @Override
    protected void actualizar() {
        int codBol = Integer.parseInt(txtCodigo.getText());
        String fechaBoleta = formatDate(dateChooser.getDate());
        String regScoItem = (String) comboRegSco.getSelectedItem();
        int codRegSco = Integer.parseInt(regScoItem.split(" / ")[0]);
        int pedId = Integer.parseInt((String) comboPedId.getSelectedItem());
        String estado = lblEstado.getText();
        double total = calcularTotal(codRegSco, pedId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE boleta SET FECH_BOL = ?, COD_REG_SCO = ?, PED_ID = ?, TOTAL = ?, ESTADO = ? WHERE COD_BOL = ?")) {
            pstmt.setString(1, fechaBoleta);
            pstmt.setInt(2, codRegSco);
            pstmt.setInt(3, pedId);
            pstmt.setDouble(4, total);
            pstmt.setString(5, estado);
            pstmt.setInt(6, codBol);
            pstmt.executeUpdate();
            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void actualizarTotal() {
        if (comboRegSco.getSelectedItem() != null && comboPedId.getSelectedItem() != null) {
            int codRegSco = Integer.parseInt(((String) comboRegSco.getSelectedItem()).split(" / ")[0]);
            int pedId = Integer.parseInt((String) comboPedId.getSelectedItem());
            double total = calcularTotal(codRegSco, pedId);
            txtTotal.setText(String.format("%.2f", total));
        }
    }
}
