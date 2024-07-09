package lab07;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashSet;
import javax.swing.plaf.FontUIResource;
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

    // Fuente global para toda la interfaz
    protected static final Font DEFAULT_FONT = new Font("Oswald", Font.BOLD, 14);

    public interfazGeneral(String title, String[] attributeNames) {
        setTitle(title);
        setLayout(new BorderLayout());
        JPanel dataPanel = new JPanel(new GridLayout(2 + attributeNames.length, 2));
        dataPanel.add(new JLabel("Código:"));
        txtCodigo = new JTextField("");
        dataPanel.add(txtCodigo);
        txtCodigo.setEditable(true);

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

        // Configurar la fuente global
        setUIFont(new FontUIResource(DEFAULT_FONT));

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

    // Método para establecer la fuente global en todos los componentes
    private static void setUIFont(FontUIResource f) {
        UIManager.put("Button.font", f);
        UIManager.put("ToggleButton.font", f);
        UIManager.put("RadioButton.font", f);
        UIManager.put("CheckBox.font", f);
        UIManager.put("ColorChooser.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("Label.font", f);
        UIManager.put("List.font", f);
        UIManager.put("MenuBar.font", f);
        UIManager.put("MenuItem.font", f);
        UIManager.put("RadioButtonMenuItem.font", f);
        UIManager.put("CheckBoxMenuItem.font", f);
        UIManager.put("Menu.font", f);
        UIManager.put("PopupMenu.font", f);
        UIManager.put("OptionPane.font", f);
        UIManager.put("Panel.font", f);
        UIManager.put("ProgressBar.font", f);
        UIManager.put("ScrollPane.font", f);
        UIManager.put("Viewport.font", f);
        UIManager.put("TabbedPane.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("PasswordField.font", f);
        UIManager.put("TextArea.font", f);
        UIManager.put("TextPane.font", f);
        UIManager.put("EditorPane.font", f);
        UIManager.put("TitledBorder.font", f);
        UIManager.put("ToolBar.font", f);
        UIManager.put("ToolTip.font", f);
        UIManager.put("Tree.font", f);
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
        txtCodigo.setEditable(false);
        for (JTextField txtAtributoExtra : txtAtributosExtras) {
            txtAtributoExtra.setEditable(true);
        }

        btnAdicionar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnInactivar.setEnabled(false);
        btnReactivar.setEnabled(false);
        btnActualizar.setEnabled(false);
        cargarDatos();
    }

    protected void salir() {
        dispose();
    }

    protected class CustomTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Obtener el estado desde el modelo de la tabla
            String estado = (String) table.getModel().getValueAt(row, table.getColumnCount() - 1);

            // Definir colores de fondo según el estado
            if ("A".equals(estado)) {
                c.setBackground(Color.GREEN);
            } else if ("I".equals(estado)) {
                c.setBackground(Color.YELLOW);
            } else if ("*".equals(estado)) {
                c.setBackground(new Color(255, 118, 89)); // Color personalizado
            } else {
                c.setBackground(Color.WHITE); // Color por defecto si el estado no coincide con ninguno de los anteriores
            }

            // Cambiar el color de fondo de toda la fila
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            }

            return c;
        }
    }

    protected int generateNextCode(String nomTabla, String nomColumna) {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(" + nomColumna + ") FROM " + nomTabla)) {
            if (rs.next()) {
                int maxCodigo = rs.getInt(1);
                return maxCodigo == 0 ? 1 : maxCodigo + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Código por defecto si la tabla está vacía o ocurre un error
    }

    protected boolean isDuplicateName(String nombre, String nomTabla, String nomColumna) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM " + nomTabla + " WHERE " + nomColumna + " = ?")) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    protected void actualizarEstado(String tabla, String columnaEstado, String columnaCodigo, int codigo, String nuevoEstado, String condicionEstado) {
        String query = "UPDATE " + tabla + " SET " + columnaEstado + " = ? WHERE " + columnaCodigo + " = ? AND " + columnaEstado + " != ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, codigo);
            pstmt.setString(3, condicionEstado);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado en " + tabla + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
