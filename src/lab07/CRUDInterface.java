package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.event.*;


import java.util.HashSet;

public class CRUDInterface extends JFrame {
    private JTextField txtCodigo, txtNombre;
    private JLabel lblEstado;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdicionar, btnModificar, btnEliminar, btnInactivar, btnReactivar, btnActualizar, btnCancelar, btnSalir;
    private int CarFlaAct = 0;
    private PreparedStatement pstmt1;
    private String operation = "";
    private HashSet<Integer> usedCodes = new HashSet<Integer>();
    
    public CRUDInterface() {
        setTitle("CRUD Interface");
        setLayout(new BorderLayout());

        // Panel de entrada de datos
        JPanel dataPanel = new JPanel(new GridLayout(3, 2));
        dataPanel.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        dataPanel.add(txtCodigo);
        dataPanel.add(new JLabel("Nombre:"));
        txtCodigo.setEditable(true);
        
        txtNombre = new JTextField();
        dataPanel.add(txtNombre);
        dataPanel.add(new JLabel("Estado:"));
        lblEstado = new JLabel("");
        dataPanel.add(lblEstado);
        txtNombre.setEditable(true);
        add(dataPanel, BorderLayout.NORTH);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3));
        btnAdicionar = new JButton("Adicionar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnInactivar = new JButton("Inactivar");
        btnReactivar = new JButton("Reactivar");
        btnActualizar = new JButton("Grabar cambios");
        btnCancelar = new JButton("Cancelar");
        btnSalir = new JButton("Salir");
        buttonPanel.add(btnAdicionar);
        buttonPanel.add(btnModificar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnInactivar);
        buttonPanel.add(btnReactivar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalir);
        add(buttonPanel, BorderLayout.SOUTH);

        // Tabla
        tableModel = new DefaultTableModel(new String[]{"Código", "Nombre", "Estado"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Acciones de los botones
        btnAdicionar.addActionListener(e -> adicionar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnInactivar.addActionListener(e -> inactivar());
        btnReactivar.addActionListener(e -> reactivar());
        btnActualizar.addActionListener(e -> actualizar());
        btnCancelar.addActionListener(e -> cancelar());
        btnSalir.addActionListener(e -> salir());
        
        
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnModificar.setEnabled(false);
        
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                    btnEliminar.setEnabled(true);
                    btnInactivar.setEnabled(true);
                    btnReactivar.setEnabled(true);
                    btnModificar.setEnabled(true);
                    btnAdicionar.setEnabled(false);
                    btnActualizar.setEnabled(false);
                } else {
                    btnEliminar.setEnabled(false);
                    btnInactivar.setEnabled(false);
                    btnReactivar.setEnabled(false);
                    btnModificar.setEnabled(false);
                    btnAdicionar.setEnabled(true);
                    btnActualizar.setEnabled(false);
                }
            }
        });
        
        // Cargar datos iniciales
        cargarDatos();

        // Configuración de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
        btnActualizar.setEnabled(true);
    }

    private void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pais ")) {
            
            tableModel.setRowCount(0);
            usedCodes.clear();
            while (rs.next()) {
                String codigo = rs.getString("COD_PAI");
                String nombre = rs.getString("NOM_PAI");
                String estado = rs.getString("ESTADO");
                
                usedCodes.add(Integer.parseInt(codigo));
                tableModel.addRow(new Object[]{codigo, nombre, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adicionar(){
         String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        String estado = "A";

        if (!usedCodes.contains(Integer.parseInt(codigo))) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pais (COD_PAI, NOM_PAI, ESTADO) VALUES (?, ?, ?)")) {
                pstmt.setString(1, codigo);
                pstmt.setString(2, nombre);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                cargarDatos();
                txtCodigo.setText(""); txtNombre.setText(""); lblEstado.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al insertar el registro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "El registro con la clave " + codigo + " ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtNombre.setEditable(true);
            CarFlaAct = 1;
            operation = "mod";
            btnActualizar.setEnabled(true);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("I")){
            JOptionPane.showMessageDialog(this, "El registro se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")){
            JOptionPane.showMessageDialog(this, "El registro esta eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            if(tableModel.getValueAt(selectedRow, 2).toString().equals("A") || tableModel.getValueAt(selectedRow, 2).toString().equals("I")){
                int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    lblEstado.setText("*");
                    operation = "mod";
                    CarFlaAct = 1;
                    btnActualizar.setEnabled(true);
                    actualizar();
                }
            }
            else {
            JOptionPane.showMessageDialog(this, "El registro esta eliminado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("A")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("I");
            CarFlaAct = 1;
            operation="mod"; txtCodigo.setEditable(false); txtNombre.setEditable(false);
            btnActualizar.setEnabled(true);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("I")){
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")){
            JOptionPane.showMessageDialog(this, "El registro esta eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && tableModel.getValueAt(selectedRow, 2).toString().equals("I")) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("A");
            CarFlaAct = 1;
            operation="mod";
            btnActualizar.setEnabled(true);
            txtCodigo.setEditable(false);
            txtNombre.setEditable(false);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("A")){
            JOptionPane.showMessageDialog(this, "El registro ya se encuentra activo", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (tableModel.getValueAt(selectedRow, 2).toString().equals("*")){
            JOptionPane.showMessageDialog(this, "El registro esta eliminado", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizar() {
        if (CarFlaAct == 1) {
            try (Connection conn = DatabaseConnection.getConnection();) {
                PreparedStatement pstmt;
                String codigo = txtCodigo.getText();
                String nombre = txtNombre.getText();
                String estado = lblEstado.getText();
                                
                if (operation.equals("mod")){
                   pstmt = conn.prepareStatement("UPDATE pais SET NOM_PAI = ?, ESTADO = ? WHERE COD_PAI = ?");
                   pstmt.setString(1, nombre);
                   pstmt.setString(2, estado);
                   pstmt.setString(3, codigo);
                   pstmt.executeUpdate();
                   
                   txtCodigo.setText("");
                   txtNombre.setText("");
                   lblEstado.setText("");
                   CarFlaAct = 0;
                }
                
                txtCodigo.setEditable(false);
                txtNombre.setEditable(false);
                
            } catch (SQLException e) {
                System.err.println(e.getSQLState());
             
            }
            
            CarFlaAct = 0;
            cargarDatos();
        }
    }

    private void cancelar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        lblEstado.setText("");
        txtCodigo.setEditable(true);
        txtNombre.setEditable(true);
        CarFlaAct = 0;
        table.clearSelection();
    }

    private void salir() {
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CRUDInterface::new);
    }
}
