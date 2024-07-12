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

public class pedidoBase extends interfazGeneral {

    private JComboBox<String> comboRepartidor;
    private JComboBox<String> comboCliente;
    private JComboBox<String> comboProcedencia;
    private JDateChooser dateChooser;

    public pedidoBase() {
        super("Gestión de Pedidos Base", new String[]{"Fecha Pedido", "Repartidor", "Cliente", "Procedencia"});
        cargarComponentes();
        cargarDatos();
                table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
                
        tablaNombre="pedido_base";
        PK="PED_ID";
        columns=6;

    }

    private void cargarComponentes() {
        comboRepartidor = new JComboBox<>();
        comboCliente = new JComboBox<>();
        comboProcedencia = new JComboBox<>();
        dateChooser = new JDateChooser();

        // Cargar datos de Repartidor en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_REP, NOM_REP FROM repartidor WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboRepartidor.addItem(rs.getInt("COD_REP") + " / " + rs.getString("NOM_REP"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Cliente en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CLI_ID, NOM_CLI FROM cliente WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboCliente.addItem(rs.getInt("CLI_ID") + " / " + rs.getString("NOM_CLI"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Procedencia de Pedido en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT PRO_PED_COD, PROC_PED_TIP FROM procedencia_de_pedido WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboProcedencia.addItem(rs.getInt("PRO_PED_COD") + " / " + rs.getString("PROC_PED_TIP"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Agregar componentes a los paneles correspondientes
        addExtraComponent(0, dateChooser);
        addExtraComponent(1, comboRepartidor);
        addExtraComponent(2, comboCliente);
        addExtraComponent(3, comboProcedencia);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT PED_ID, FEC_PED, COD_REP, CLI_ID, PRO_PED_COD, ESTADO FROM pedido_base")) {
            tableModel.setRowCount(0); // Limpiar la tabla antes de cargar datos
            while (rs.next()) {
                // Obtener nombre del repartidor usando su código
                int codRep = rs.getInt("COD_REP");
                String nomRep = getRepartidorNombre(conn, codRep);

                // Obtener nombre del cliente usando su ID
                int cliId = rs.getInt("CLI_ID");
                String nomCli = getClienteNombre(conn, cliId);

                // Obtener tipo de procedencia de pedido usando su código
                int proPedCod = rs.getInt("PRO_PED_COD");
                String procPedTip = getProcedenciaPedido(conn, proPedCod);

                tableModel.addRow(new Object[]{
                    rs.getInt("PED_ID"),
                    rs.getString("FEC_PED"),
                    codRep + " / " + nomRep,
                    cliId + " / " + nomCli,
                    proPedCod + " / " + procPedTip,
                    rs.getString("ESTADO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getRepartidorNombre(Connection conn, int codRep) throws SQLException {
        String nombre = "";
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT NOM_REP FROM repartidor WHERE COD_REP = ?")) {
            pstmt.setInt(1, codRep);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("NOM_REP");
            }
        }
        return nombre;
    }

    private String getClienteNombre(Connection conn, int cliId) throws SQLException {
        String nombre = "";
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT NOM_CLI FROM cliente WHERE CLI_ID = ?")) {
            pstmt.setInt(1, cliId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("NOM_CLI");
            }
        }
        return nombre;
    }

    private String getProcedenciaPedido(Connection conn, int proPedCod) throws SQLException {
        String tipo = "";
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT PROC_PED_TIP FROM procedencia_de_pedido WHERE PRO_PED_COD = ?")) {
            pstmt.setInt(1, proPedCod);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                tipo = rs.getString("PROC_PED_TIP");
            }
        }
        return tipo;
    }

    @Override
    protected void adicionar() {
        int pedId= Integer.parseInt(txtCodigo.getText());
        String fechaPedido = formatDate(dateChooser.getDate());
        String repartidorItem = (String) comboRepartidor.getSelectedItem();
        int codRep = Integer.parseInt(repartidorItem.split(" / ")[0]);
        String clienteItem = (String) comboCliente.getSelectedItem();
        int cliId = Integer.parseInt(clienteItem.split(" / ")[0]);
        String procedenciaItem = (String) comboProcedencia.getSelectedItem();
        int proPedCod = Integer.parseInt(procedenciaItem.split(" / ")[0]);
        String estado = "A";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pedido_base (PED_ID, FEC_PED, COD_REP, CLI_ID, PRO_PED_COD, ESTADO) VALUES (?,?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, pedId);
            pstmt.setString(2, fechaPedido);
            pstmt.setInt(3, codRep);
            pstmt.setInt(4, cliId);
            pstmt.setInt(5, proPedCod);
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
            int pedId = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

            if (selectedRow != -1 && estado.equals("A")) {
                txtCodigo.setText(pedId+"");
                txtCodigo.setEditable(false);
                dateChooser.setDate(parseDate(tableModel.getValueAt(selectedRow, 1).toString()));
                int codRep = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 2)).split(" / ")[0]);
                comboRepartidor.setSelectedItem(getComboItemText(codRep, comboRepartidor));
                int cliId = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 3)).split(" / ")[0]);
                comboCliente.setSelectedItem(getComboItemText(cliId, comboCliente));
                int proPedCod = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 4)).split(" / ")[0]);
                comboProcedencia.setSelectedItem(getComboItemText(proPedCod, comboProcedencia));
                lblEstado.setText(estado);
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este pedido no puede ser modificado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un pedido para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        dateChooser.setDate(null);
        comboRepartidor.setSelectedIndex(0);
        comboCliente.setSelectedIndex(0);
        comboProcedencia.setSelectedIndex(0);
    }

    private String formatDate(java.util.Date date) {
        if (date != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
        return null;
    }

    private java.util.Date parseDate(String date) {
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
    @Override
    protected void actualizar() {
        int pedId = Integer.parseInt(txtCodigo.getText());
        String fechaPedido = formatDate(dateChooser.getDate());
        String repartidorItem = (String) comboRepartidor.getSelectedItem();
        int codRep = Integer.parseInt(repartidorItem.split(" / ")[0]);
        String clienteItem = (String) comboCliente.getSelectedItem();
        int cliId = Integer.parseInt(clienteItem.split(" / ")[0]);
        String procedenciaItem = (String) comboProcedencia.getSelectedItem();
        int proPedCod = Integer.parseInt(procedenciaItem.split(" / ")[0]);
        String estado = lblEstado.getText();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE pedido_base SET FEC_PED = ?, COD_REP = ?, CLI_ID = ?, PRO_PED_COD = ?, ESTADO = ? WHERE PED_ID = ?")) {
            pstmt.setString(1, fechaPedido);
            pstmt.setInt(2, codRep);
            pstmt.setInt(3, cliId);
            pstmt.setInt(4, proPedCod);
            pstmt.setString(5, estado);
            pstmt.setInt(6, pedId);
            pstmt.executeUpdate();
            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
