package lab07;

import java.sql.*;
import javax.swing.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class turno extends interfazGeneral {

    private JSpinner spnInicio;
    private JSpinner spnFin;

    public turno() {
        super("CRUD Turno Interface", new String[]{"Inicio Turno", "Fin Turno"});
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        initializeComponents();
        tablaNombre="turno";
        PK="TIP_TUR";
        columns=4;
    }

    private void initializeComponents() {
        spnInicio = new JSpinner(new SpinnerDateModel());
        spnFin = new JSpinner(new SpinnerDateModel());

        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spnInicio, "HH:mm");
        JSpinner.DateEditor editorFin = new JSpinner.DateEditor(spnFin, "HH:mm");
        
        spnInicio.setEditor(editorInicio);
        spnFin.setEditor(editorFin);

        pnlAtributosExtras[0].add(spnInicio); // Add the JSpinner to the respective panel for "Inicio Turno"
        pnlAtributosExtras[1].add(spnFin); // Add the JSpinner to the respective panel for "Fin Turno"
    }

    @Override
    protected void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM turno")) {

            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("TIP_TUR");
                String inicio = rs.getString("INI_TUR");
                String fin = rs.getString("FIN_TUR");
                String estado = rs.getString("ESTADO");

                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, inicio, fin, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adicionar() {
        Date inicioDate = (Date) spnInicio.getValue();
        Date finDate = (Date) spnFin.getValue();
        String estado = "A";

        String inicio = formatTime(inicioDate);
        String fin = formatTime(finDate);

        int codigo = generateNextCode("turno", "TIP_TUR");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO turno (TIP_TUR, INI_TUR, FIN_TUR, ESTADO) VALUES (?, ?, ?, ?)")) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, inicio);
            pstmt.setString(3, fin);
            pstmt.setString(4, estado);
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            spnInicio.setValue(parseTime(tableModel.getValueAt(selectedRow, 1).toString()));
            spnFin.setValue(parseTime(tableModel.getValueAt(selectedRow, 2).toString()));
            lblEstado.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCodigo.setEditable(false);
            spnInicio.setEnabled(true);
            spnFin.setEnabled(true);
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
        if (selectedRow != -1 && !tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                spnInicio.setValue(parseTime(tableModel.getValueAt(selectedRow, 1).toString()));
                spnFin.setValue(parseTime(tableModel.getValueAt(selectedRow, 2).toString()));
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
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            spnInicio.setValue(parseTime(tableModel.getValueAt(selectedRow, 1).toString()));
            spnFin.setValue(parseTime(tableModel.getValueAt(selectedRow, 2).toString()));
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation = "mod";
            txtCodigo.setEditable(false);
            spnInicio.setEnabled(false);
            spnFin.setEnabled(false);
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 3).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            spnInicio.setValue(parseTime(tableModel.getValueAt(selectedRow, 1).toString()));
            spnFin.setValue(parseTime(tableModel.getValueAt(selectedRow, 2).toString()));
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
            actualizar();
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("A")) {
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (tableModel.getValueAt(selectedRow, 3).toString().equals("*")) {
            JOptionPane.showMessageDialog(this, "El registro está eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void actualizar() {
        if (CarFlaAct == 1) {
            String codigo = txtCodigo.getText();
            Date inicioDate = (Date) spnInicio.getValue();
            Date finDate = (Date) spnFin.getValue();
            String estado = lblEstado.getText();

            String inicio = formatTime(inicioDate);
            String fin = formatTime(finDate);

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE turno SET INI_TUR = ?, FIN_TUR = ?, ESTADO = ? WHERE TIP_TUR = ?")) {
                pstmt.setString(1, inicio);
                pstmt.setString(2, fin);
                pstmt.setString(3, estado);
                pstmt.setString(4, codigo);
                pstmt.executeUpdate();

                cargarDatos();
                cancelar();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    private Date parseTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void cancelar() {
        // Limpiar los campos de texto y restablecer estado de componentes
        txtCodigo.setText("");
        spnInicio.setValue(new Date());
        spnFin.setValue(new Date());
        lblEstado.setText("");

        // Restablecer estado de edición de campos
        txtCodigo.setEditable(true);
        spnInicio.setEnabled(true);
        spnFin.setEnabled(true);

        // Deshabilitar botones según el estado deseado
        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);

        // Cargar los datos nuevamente en la tabla
        cargarDatos();
    }

}
