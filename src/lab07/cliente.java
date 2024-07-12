package lab07;

import java.sql.*;
import javax.swing.*;

public class cliente extends interfazGeneral {

    private JTextField[] txtAtributosExtras;

    public cliente() {
        super("CRUD Cliente Interface", new String[]{"Nombre", "Apellido", "Dirección", "Teléfono", "Consumo P", "Consumo B", "Consumo C"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // Inicializar los JTextField para los atributos extras
        txtAtributosExtras = new JTextField[7];
        for (int i = 0; i < txtAtributosExtras.length; i++) {
            txtAtributosExtras[i] = new JTextField();
            addExtraComponent(i, txtAtributosExtras[i]);
        }
        
        tablaNombre="cliente";
        PK="CLI_ID";
        columns=9;
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cliente")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String id = rs.getString("CLI_ID");
                String nombre = rs.getString("NOM_CLI");
                String apellido = rs.getString("APE_CLI");
                String direccion = rs.getString("DIR_CLI");
                String telefono = rs.getString("TEL_CLI");
                String consP = rs.getString("CONS_P");
                String consB = rs.getString("CONS_B");
                String consC = rs.getString("CONS_C");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(id));
                // Agregar fila al tableModel con los datos obtenidos
                tableModel.addRow(new Object[]{id, nombre, apellido, direccion, telefono, consP, consB, consC, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        String nombre = txtAtributosExtras[0].getText();
        String apellido = txtAtributosExtras[1].getText();
        String direccion = txtAtributosExtras[2].getText();
        String telefono = txtAtributosExtras[3].getText();
        String consP = txtAtributosExtras[4].getText();
        String consB = txtAtributosExtras[5].getText();
        String consC = txtAtributosExtras[6].getText();
        String estado = "A";

        int codigo = generateNextCode("cliente", "CLI_ID");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO cliente (CLI_ID, NOM_CLI, APE_CLI, DIR_CLI, TEL_CLI, CONS_P, CONS_B, CONS_C, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.setString(3, apellido);
            pstmt.setString(4, direccion);
            pstmt.setString(5, telefono);
            pstmt.setString(6, consP);
            pstmt.setString(7, consB);
            pstmt.setString(8, consC);
            pstmt.setString(9, estado);
            pstmt.executeUpdate();

            cargarDatos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void modificar() {
        int selectedRow = table.getSelectedRow();
        String estado = tableModel.getValueAt(selectedRow, columns-1).toString();

        if (selectedRow != -1 && estado.equals("A")) {
            // Obtener y establecer los valores en los campos correspondientes
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            for (int i = 0; i < txtAtributosExtras.length; i++) {
                txtAtributosExtras[i].setText(tableModel.getValueAt(selectedRow, i + 1).toString());
            }
            lblEstado.setText(tableModel.getValueAt(selectedRow, 8).toString());

            // Establecer estado de los componentes
            txtCodigo.setEditable(false);
            for (JTextField txtAtributoExtra : txtAtributosExtras) {
                txtAtributoExtra.setEditable(true);
            }

            // Configurar la operación de modificación
            operation = "mod";
            CarFlaAct = 1;
            btnActualizar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Este registro no puede editarse.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            String nombre = txtAtributosExtras[0].getText();
            String apellido = txtAtributosExtras[1].getText();
            String direccion = txtAtributosExtras[2].getText();
            String telefono = txtAtributosExtras[3].getText();
            String consP = txtAtributosExtras[4].getText();
            String consB = txtAtributosExtras[5].getText();
            String consC = txtAtributosExtras[6].getText();
            String estado = lblEstado.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE cliente SET NOM_CLI = ?, APE_CLI = ?, DIR_CLI = ?, TEL_CLI = ?, CONS_P = ?, CONS_B = ?, CONS_C = ?, ESTADO = ? WHERE CLI_ID = ?")) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, apellido);
                pstmt.setString(3, direccion);
                pstmt.setString(4, telefono);
                pstmt.setString(5, consP);
                pstmt.setString(6, consB);
                pstmt.setString(7, consC);
                pstmt.setString(8, estado);
                pstmt.setString(9, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @Override
    protected void cancelar() {
        // Limpiar el campo de texto del código y los atributos extras
        txtCodigo.setText("");
        for (JTextField txtAtributoExtra : txtAtributosExtras) {
            txtAtributoExtra.setText("");
        }

        // Restablecer el estado del label de estado
        lblEstado.setText("");

        // Habilitar/Deshabilitar campos y botones según corresponda
        txtCodigo.setEditable(true);
        for (JTextField txtAtributoExtra : txtAtributosExtras) {
            txtAtributoExtra.setEditable(true);
        }
        btnActualizar.setEnabled(false);

        // Restablecer las variables de control
        CarFlaAct = 0;
        operation = "";
        
        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);
        cargarDatos();
    }

}
