package lab07;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class pedidoArticulos extends interfazGeneral {

    private JComboBox<String> comboArticulo;
    private JComboBox<String> comboPedido;
    private JSpinner spinnerCantidad;
    private JTextField txtSubtotal;

    public pedidoArticulos() {
        super("Gestión de Pedido Artículos", new String[]{"Artículo", "Pedido", "Cantidad", "Subtotal"});
        cargarComponentes();
        cargarDatos();
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        txtCodigo.setText("No disponible para esta tabla");
        txtCodigo.setEditable(false);
    }

    private void cargarComponentes() {
        comboArticulo = new JComboBox<>();
        comboPedido = new JComboBox<>();
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1)); // Spinner para la cantidad
        txtSubtotal = new JTextField(); // TextField para el subtotal
        txtSubtotal.setEditable(false); // No editable manualmente

        // Cargar datos de Artículos en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ART_COD, ART_NOM, PREC_ART FROM articulo WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboArticulo.addItem(rs.getInt("ART_COD") + " / " + rs.getString("ART_NOM"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Pedidos en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT PED_ID FROM pedido_base WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboPedido.addItem(String.valueOf(rs.getInt("PED_ID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Listener para actualizar el subtotal cuando se cambia la cantidad
        spinnerCantidad.addChangeListener(e -> {
            actualizarSubtotal();
        });

        // Agregar componentes a los paneles correspondientes
        addExtraComponent(0, comboArticulo);
        addExtraComponent(1, comboPedido);
        addExtraComponent(2, spinnerCantidad);
        addExtraComponent(3, txtSubtotal);
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ART_COD, PED_ID, CANTIDAD, SUBTOTAL, ESTADO FROM pedido_art")) {
            tableModel.setRowCount(0); // Limpiar la tabla antes de cargar datos
            while (rs.next()) {
                int artCod = rs.getInt("ART_COD");
                String artNombre = getArticuloNombre(conn, artCod);

                tableModel.addRow(new Object[]{
                    "",
                    artCod + " / " + artNombre,
                    rs.getInt("PED_ID"),
                    rs.getInt("CANTIDAD"),
                    rs.getInt("SUBTOTAL"),
                    rs.getString("ESTADO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getArticuloNombre(Connection conn, int artCod) throws SQLException {
        String nombre = "";
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT ART_NOM FROM articulo WHERE ART_COD = ?")) {
            pstmt.setInt(1, artCod);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("ART_NOM");
            }
        }
        return nombre;
    }

    @Override
    protected void adicionar() {
        String articuloItem = (String) comboArticulo.getSelectedItem();
        int artCod = Integer.parseInt(articuloItem.split(" / ")[0]);
        int pedId = Integer.parseInt((String) comboPedido.getSelectedItem());
        int cantidad = (int) spinnerCantidad.getValue();
        int subtotal = calcularSubtotal(artCod, cantidad);
        String estado = "A";

        // Verificar si el artículo ya existe en el pedido
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM pedido_art WHERE ART_COD = ? AND PED_ID = ?")) {
            pstmt.setInt(1, artCod);
            pstmt.setInt(2, pedId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Este artículo ya está en el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pedido_art (ART_COD, PED_ID, CANTIDAD, SUBTOTAL, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, artCod);
            pstmt.setInt(2, pedId);
            pstmt.setInt(3, cantidad);
            pstmt.setInt(4, subtotal);
            pstmt.setString(5, estado);
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
        String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1) {
            String artCodStr = table.getValueAt(selectedRow, 1).toString();
            int artCod = Integer.parseInt(artCodStr.split(" / ")[0]);

            String pedIdStr = table.getValueAt(selectedRow, 2).toString();
            int pedId = Integer.parseInt(pedIdStr.split(" / ")[0]);

            int cantidad = (int) table.getValueAt(selectedRow, 3);
            int subtotal = (int) table.getValueAt(selectedRow, 4);

            if (!estado.equals("*") || !estado.equals("I")) {
                comboArticulo.setSelectedItem(artCod);
                comboPedido.setSelectedItem(String.valueOf(pedId));
                spinnerCantidad.setValue(cantidad);
                txtSubtotal.setText(String.valueOf(subtotal));
                lblEstado.setText(estado);
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser modificado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un artículo para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && table.getValueAt(selectedRow, 5).toString().equals("A")) {
            int artCod = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString().split(" / ")[0]);
            int pedId = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString().split(" / ")[0]);

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstado(conn, "pedido_art", "ESTADO", "ART_COD", "PED_ID", artCod, pedId, "*");
                cargarDatos();
                cancelar();
                txtCodigo.setEditable(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser eliminado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && table.getValueAt(selectedRow, 5).toString().equals("A")) {
            int artCod = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString().split(" / ")[0]);
            int pedId = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString().split(" / ")[0]);

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstado(conn, "pedido_art", "ESTADO", "ART_COD", "PED_ID", artCod, pedId, "I");
                cargarDatos();
                cancelar();
                txtCodigo.setEditable(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser inactivado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && !table.getValueAt(selectedRow, 5).toString().equals("A")) {
            int artCod = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString().split(" / ")[0]);
            int pedId = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString().split(" / ")[0]);

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstado(conn, "pedido_art", "ESTADO", "ART_COD", "PED_ID", artCod, pedId, "A");
                cargarDatos();
                cancelar();
                txtCodigo.setEditable(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
                JOptionPane.showMessageDialog(this, "Este artículo no puede ser reactivado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    @Override
    protected void cancelar() {
        super.cancelar();
        comboArticulo.setSelectedIndex(0);
        comboPedido.setSelectedIndex(0);
        spinnerCantidad.setValue(1);
        txtSubtotal.setText("");
        txtCodigo.setEditable(false);
    }

    private void actualizarEstado(Connection conn, String tableName, String estadoColumn, String primaryKey1, String primaryKey2, int key1, int key2, String newEstado) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE " + tableName + " SET " + estadoColumn + " = ? WHERE " + primaryKey1 + " = ? AND " + primaryKey2 + " = ?")) {
            pstmt.setString(1, newEstado);
            pstmt.setInt(2, key1);
            pstmt.setInt(3, key2);
            pstmt.executeUpdate();
        }
    }
    
    
    @Override
    protected void actualizar() {
        int artCod = Integer.parseInt(((String) comboArticulo.getSelectedItem()).split(" / ")[0]);
        int pedId = Integer.parseInt(((String) comboPedido.getSelectedItem()).split(" / ")[0]);
        int cantidad = (int) spinnerCantidad.getValue();
        int subtotal = calcularSubtotal(artCod, cantidad);
        String estado = lblEstado.getText();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE pedido_art SET CANTIDAD = ?, SUBTOTAL = ?, ESTADO = ? WHERE ART_COD = ? AND PED_ID = ?")) {
            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, subtotal);
            pstmt.setString(3, estado);
            pstmt.setInt(4, artCod);
            pstmt.setInt(5, pedId);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
            txtCodigo.setEditable(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    private int calcularSubtotal(int artCod, int cantidad) {
        int precio = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT PREC_ART FROM articulo WHERE ART_COD = ?")) {
            pstmt.setInt(1, artCod);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                precio = rs.getInt("PREC_ART");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return precio * cantidad;
    }
    private void actualizarSubtotal() {
        String articuloItem = (String) comboArticulo.getSelectedItem();
        if (articuloItem != null) {
            int artCod = Integer.parseInt(articuloItem.split(" / ")[0]);
            int cantidad = (int) spinnerCantidad.getValue();
            int subtotal = calcularSubtotal(artCod, cantidad);
            txtSubtotal.setText(String.valueOf(subtotal));
        }
    }

}
