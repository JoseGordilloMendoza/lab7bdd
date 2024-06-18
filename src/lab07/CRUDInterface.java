package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CRUDInterface extends JFrame {
    private JTextField txtCodigo, txtNombre;
    private JLabel lblEstado;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdicionar, btnModificar, btnEliminar, btnInactivar, btnReactivar, btnActualizar, btnCancelar, btnSalir;
    private int CarFlaAct = 0;

    public CRUDInterface() {
        setTitle("CRUD Interface");
        setLayout(new BorderLayout());

        // Panel de entrada de datos
        JPanel dataPanel = new JPanel(new GridLayout(3, 2));
        dataPanel.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        dataPanel.add(txtCodigo);
        dataPanel.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        dataPanel.add(txtNombre);
        dataPanel.add(new JLabel("Estado:"));
        lblEstado = new JLabel("A");
        dataPanel.add(lblEstado);
        add(dataPanel, BorderLayout.NORTH);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 8));
        btnAdicionar = new JButton("Adicionar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnInactivar = new JButton("Inactivar");
        btnReactivar = new JButton("Reactivar");
        btnActualizar = new JButton("Actualizar");
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

        // Cargar datos iniciales
        cargarDatos();

        // Configuración de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    private void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pais")) {
            while (rs.next()) {
                String codigo = rs.getString("COD_PAI");
                String nombre = rs.getString("NOM_PAI");
                String estado = rs.getString("ESTADO");
                tableModel.addRow(new Object[]{codigo, nombre, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adicionar() {
        String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        String estado = lblEstado.getText();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pais (COD_PAI, NOM_PAI, ESTADO) VALUES (?, ?, ?)")) {
            pstmt.setString(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.setString(3, estado);
            pstmt.executeUpdate();
            // Agregar el nuevo registro a la tabla
            tableModel.addRow(new Object[]{codigo, nombre, estado});
            // Limpiar los campos después de adicionar
            txtCodigo.setText("");
            txtNombre.setText("");
            lblEstado.setText("A");
            CarFlaAct = 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText(tableModel.getValueAt(selectedRow, 2).toString());
            CarFlaAct = 1;
        }
    }

    private void eliminar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("*");
            CarFlaAct = 1;
        }
    }

    private void inactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("I");
            CarFlaAct = 1;
        }
    }

    private void reactivar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtCodigo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombre.setText(tableModel.getValueAt(selectedRow, 1).toString());
            lblEstado.setText("A");
            CarFlaAct = 1;
        }
    }

    private void actualizar() {
        if (CarFlaAct == 1) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE pais SET NOM_PAI = ?, ESTADO = ? WHERE COD_PAI = ?")) {
                pstmt.setString(1, txtNombre.getText());
                pstmt.setString(2, lblEstado.getText());
                pstmt.setString(3, txtCodigo.getText());
                pstmt.executeUpdate();
                // Limpiar la tabla y volver a cargar los datos
                tableModel.setRowCount(0);
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            CarFlaAct = 0;
        }
    }

    private void cancelar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        lblEstado.setText("A");
        CarFlaAct = 0;
    }

    private void salir() {
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CRUDInterface::new);
    }
}