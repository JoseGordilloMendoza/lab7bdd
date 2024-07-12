package lab07;

import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class repartidor extends interfazGeneral {

    private JComboBox<String> comboScooter;
    private JComboBox<String> comboTurno;
    private JTextField txtNombre;

    public repartidor() {
        super("Gestión de Repartidores", new String[]{"Nombre", "Scooter", "Turno"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cargarDatosExtra();
    }

    private void cargarDatosExtra() {
        comboScooter = new JComboBox<>();
        comboTurno = new JComboBox<>();
        txtNombre = new JTextField();

        // Cargar datos de Scooter en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COD_SCO, AÑO_SCO FROM scooter WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboScooter.addItem(rs.getString("COD_SCO") + " / " + rs.getString("AÑO_SCO"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar datos de Turno en el JComboBox
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TIP_TUR, INI_TUR, FIN_TUR FROM turno WHERE ESTADO = 'A'")) {
            while (rs.next()) {
                comboTurno.addItem(rs.getString("TIP_TUR") + " / " + rs.getString("INI_TUR") + " - " + rs.getString("FIN_TUR"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Agregar componentes a los paneles correspondientes
        addExtraComponent(0, txtNombre);
        addExtraComponent(1, comboScooter);
        addExtraComponent(2, comboTurno);

    }


    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT r.COD_REP, r.NOM_REP, s.COD_SCO, s.AÑO_SCO, t.TIP_TUR, t.INI_TUR, t.FIN_TUR, r.ESTADO " +
                                                "FROM repartidor r " +
                                                "JOIN scooter s ON r.COD_SCO = s.COD_SCO " +
                                                "JOIN turno t ON r.TIP_TUR = t.TIP_TUR")) {
            tableModel.setRowCount(0); // Limpiar la tabla antes de cargar datos
            while (rs.next()) {
                String scooterFormat = rs.getString("COD_SCO") + " / " + rs.getString("AÑO_SCO");
                String turnoFormat = rs.getString("TIP_TUR") + " / " + rs.getString("INI_TUR") + " - " + rs.getString("FIN_TUR");
                tableModel.addRow(new Object[]{
                    rs.getInt("COD_REP"),
                    rs.getString("NOM_REP"),
                    scooterFormat,
                    turnoFormat,
                    rs.getString("ESTADO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        int codigo = generateNextCode("repartidor", "COD_REP");
        String nombre = txtNombre.getText();
        
        String codScoSelec = (String) comboScooter.getSelectedItem();
        int codScooter = Integer.parseInt(codScoSelec.split(" / ")[0]);
        
        String codTurSelec = (String) comboTurno.getSelectedItem();
        int tipTurno = Integer.parseInt(codTurSelec.split(" / ")[0]);
        String estado = "A";

        if (!isDuplicateName(nombre, "repartidor", "NOM_REP")) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO repartidor (COD_REP, NOM_REP, COD_SCO, TIP_TUR, ESTADO) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, codigo);
                pstmt.setString(2, nombre);
                pstmt.setInt(3, codScooter);
                pstmt.setInt(4, tipTurno);
                pstmt.setString(5, estado);
                pstmt.executeUpdate();
                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "El nombre del repartidor ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codRep = (int) tableModel.getValueAt(selectedRow, 0);
            String estado = tableModel.getValueAt(selectedRow, 4).toString();

            if (!estado.equals("*")) {
                txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtCodigo.setText(String.valueOf(codRep));
                txtCodigo.setEditable(false);
                int codScooter = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 2)).split(" / ")[0]);
                comboScooter.setSelectedItem(String.valueOf(codScooter));

                int tipTurno = Integer.parseInt(((String) tableModel.getValueAt(selectedRow, 3)).split(" / ")[0]);
                comboTurno.setSelectedItem(String.valueOf(tipTurno));

                lblEstado.setText(estado);
                operation = "mod";
                btnActualizar.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Este repartidor no puede ser modificado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un repartidor para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    protected void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "repartidor", "ESTADO", "COD_REP", codigo, "*");
                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                actualizarEstadoEnCascada(conn, "repartidor", "ESTADO", "COD_REP", codigo, "I");
                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void reactivar() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int codigo = (int) tableModel.getValueAt(selectedRow, 0);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    actualizarEstadoEnCascada(conn, "repartidor", "ESTADO", "COD_REP", codigo, "A");
                    cargarDatos();
                    cancelar();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
    protected void actualizar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tableModel.getValueAt(selectedRow, 0);
            String nombre = txtNombre.getText();
            String codScoSelec = (String) comboScooter.getSelectedItem();
            int codScooter = Integer.parseInt(codScoSelec.split(" / ")[0]);

            String codTurSelec = (String) comboTurno.getSelectedItem();
            int tipTurno = Integer.parseInt(codTurSelec.split(" / ")[0]);
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE repartidor SET NOM_REP = ?, COD_SCO = ?, TIP_TUR = ?, ESTADO = ? WHERE COD_REP = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, codScooter);
                pstmt.setInt(3, tipTurno);
                pstmt.setString(4, estado);
                pstmt.setInt(5, codigo);
                pstmt.executeUpdate();
                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
