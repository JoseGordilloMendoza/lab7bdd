package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashSet;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class interfazGeneral extends JFrame {
    protected JTextField txtCodigo;
    protected JLabel lblEstado;
    protected JTable table;
    protected DefaultTableModel tableModel;
    protected JButton btnAdicionar, btnModificar, btnEliminar, btnInactivar, btnReactivar, btnActualizar, btnCancelar, btnSalir;
    protected int CarFlaAct = 0;
    protected PreparedStatement pstmt1;
    protected String operation = "";
    protected HashSet<Integer> usedCodes = new HashSet<>();
    protected JTextField[] txtAtributosExtras;
    protected JLabel[] lblAtributosExtras;

    public interfazGeneral(String title, String[] attributeNames) {
        setTitle(title);
        setLayout(new BorderLayout());
        JPanel dataPanel = new JPanel(new GridLayout(2 + attributeNames.length, 2));
        dataPanel.add(new JLabel("Código:"));
        dataPanel.add(txtCodigo);
        txtCodigo.setEditable(false);

        // tamaño dinamico
        txtAtributosExtras = new JTextField[attributeNames.length];
        lblAtributosExtras = new JLabel[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            lblAtributosExtras[i] = new JLabel(attributeNames[i] + ":");
            txtAtributosExtras[i] = new JTextField();
            dataPanel.add(lblAtributosExtras[i]);
            dataPanel.add(txtAtributosExtras[i]);
        }

        dataPanel.add(new JLabel("Estado:"));
        lblEstado = new JLabel("");
        dataPanel.add(lblEstado);

        add(dataPanel, BorderLayout.NORTH);

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

        String[] columnNames = new String[attributeNames.length + 2];
        columnNames[0] = "Codigo";
        for (int i = 1; i <= attributeNames.length; i++) {
            columnNames[i] = attributeNames[i - 1];
        }
        columnNames[attributeNames.length + 1] = "Estado";
        tableModel = new DefaultTableModel(columnNames, 0);
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

        table.getSelectionModel().addListSelectionListener(event -> {
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
        });

        // Cargar datos iniciales
        cargarDatos();

        // Configuración de la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
        btnActualizar.setEnabled(true);
    }

    protected abstract void cargarDatos();

    protected abstract void adicionar();

    protected abstract void modificar();

    protected abstract void eliminar();

    protected abstract void inactivar();

    protected abstract void reactivar();

    protected abstract void actualizar();

    protected void cancelar() {
        // Limpiar campos de texto
        txtCodigo.setText("");
        for (JTextField txtAtributoExtra : txtAtributosExtras) {
            txtAtributoExtra.setText("");
        }
        lblEstado.setText("");

        // Deseleccionar cualquier fila en la tabla
        table.clearSelection();

        // Restablecer la configuración inicial de los botones y campos de texto
        txtCodigo.setEditable(true);
        for (JTextField txtAtributoExtra : txtAtributosExtras) {
            txtAtributoExtra.setEditable(true);
        }

        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);
    }
    
    protected void salir() {
        dispose();
    }
    
    protected class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = table.getModel().getValueAt(row, table.getColumnCount() - 1).toString();
            if (estado.equals("A")) {
                c.setBackground(Color.GREEN);
            } else if (estado.equals("I")) {
                c.setBackground(Color.YELLOW);
            } else if (estado.equals("*")) {
                c.setBackground(Color.RED);
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
