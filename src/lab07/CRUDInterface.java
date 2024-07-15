package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CRUDInterface extends JFrame {

    private JComboBox<Object> categorySelector;
    private JComboBox<Object> tableSelector;
    private JLabel titleLabel;

    public CRUDInterface() {
        
        setTitle("Seleccionar Tabla");
        setLayout(new BorderLayout());
        setSize(400, 300);
        
        titleLabel = new JLabel("Editor de tablas", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.PLAIN, 18));
        add(titleLabel, BorderLayout.NORTH);

        
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new GridLayout(3, 1)); 

        selectorPanel.add(new JLabel("Selecciona la categoría:"));

        // Crear y agregar categorías al JComboBox
        String[] categories = {"Artículos", "Pedidos", "Gestion", "Ubicaciones", "Almacenes e insumos", "Otros"};
        categorySelector = new JComboBox<>(categories);
        categorySelector.addActionListener(e -> updateTableSelector());
        selectorPanel.add(categorySelector);

        selectorPanel.add(new JLabel("Selecciona la tabla:"));
        tableSelector = new JComboBox<>();
        selectorPanel.add(tableSelector);

        add(selectorPanel, BorderLayout.CENTER);

        JButton btnSeleccionar = new JButton("Seleccionar");
        btnSeleccionar.addActionListener(e -> seleccionarTabla());
        add(btnSeleccionar, BorderLayout.SOUTH);
        
        JButton btnSalir = new JButton("Salir");
        btnSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Cierra la ventana actual
                new home(); // Crea una nueva instancia de la ventana Home
            }
        });
        // Cambia el layout de BorderLayout a FlowLayout para el panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());

// Añade los botones al panel
        buttonPanel.add(btnSeleccionar);
        buttonPanel.add(btnSalir);

// Añade el panel de botones al sur de la ventana
        add(buttonPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setVisible(true);

        // Aplicar la fuente predeterminada a toda la interfaz
        applyDefaultFont();
        updateTableSelector(); // Inicializar tablas
    }

    private void updateTableSelector() {
        String selectedCategory = (String) categorySelector.getSelectedItem();
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        switch (selectedCategory) {
            case "Artículos":
                model.addElement("articulo");
                model.addElement("tamaño Articulo");
                model.addElement("tipo Articulo");
                break;
            case "Pedidos":
                model.addElement("pedidoArticulos");
                model.addElement("pedidoBase");
                break;
            case "Gestion":
                model.addElement("boleta");
                model.addElement("facturaGas");
                model.addElement("registroScooter");
                model.addElement("repartidor");
                break;
            case "Ubicaciones":
                model.addElement("pais");
                model.addElement("region");
                model.addElement("franquicia");
                model.addElement("localidad");
                model.addElement("ciudad");
                break;
            case "Almacenes e insumos":
                model.addElement("almacen");
                model.addElement("ingredienteAlmacen");
                model.addElement("ingrediente");
                break;
            case "Otros":
                model.addElement("cliente");
                model.addElement("tipo_de_regalo");
                model.addElement("turno");
                model.addElement("regalo");
                model.addElement("receta_det");
                model.addElement("receta");
                model.addElement("scooter");
                break;
        }
        tableSelector.setModel(model);
    }

// Actualizar el método seleccionarTabla
    private void seleccionarTabla() {
        Object selectedItem = tableSelector.getSelectedItem();
        if (selectedItem != null && selectedItem instanceof String) {
            String selectedTable = (String) selectedItem;
            switch (selectedTable) {
                case "articulo":
                    new articulo();
                    break;
                case "pedidoArticulos":
                    new pedidoArticulos();
                    break;
                case "pedidoBase":
                    new pedidoBase();
                    break;
                case "boleta":
                    new boleta();
                    break;
                case "pais":
                    new pais();
                    break;
                case "registroScooter":
                    new registroScooter();
                    break;
                case "repartidor":
                    new repartidor();
                    break;
                case "tamaño Articulo":
                    new tam_art();
                    break;
                case "procedencia_de_pedido":
                    new prod_ped();
                    break;
                case "tipo_de_regalo":
                    new tip_reg();
                    break;
                case "turno":
                    new turno();
                    break;
                case "cliente":
                    new cliente();
                    break;
                case "region":
                    new region();
                    break;
                case "franquicia":
                    new franquicia();
                    break;
                case "localidad":
                    new localidad();
                    break;
                case "ciudad":
                    new ciudad();
                    break;
                case "almacen":
                    new almacen();
                    break;
                case "ingredienteAlmacen":
                    new ingredienteAlmacen();
                    break;
                case "facturaGas":
                    new facturaGas();
                    break;
                case "regalo":
                    new regalo();
                    break;
                case "tipo Articulo":
                    new tip_art();
                    break;
                case "ingrediente":
                    new ingrediente();
                    break;
                case "receta_det":
                    new receta_det();
                    break;
                case "receta":
                    new receta();
                    break;
                case "scooter":
                    new scooter();
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Tabla no reconocida", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        dispose();
    }

    private void applyDefaultFont() {
        Font defaultFont = new Font("Arial Black", Font.PLAIN, 12);

        // Aplicar la fuente a todos los componentes
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                applyFontToPanel(panel, defaultFont);
            } else if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setFont(defaultFont);
            } else if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setFont(defaultFont);
            } else if (component instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) component;
                comboBox.setFont(defaultFont);
            }
        }
    }

    private void applyFontToPanel(JPanel panel, Font font) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                applyFontToPanel((JPanel) component, font);
            } else {
                component.setFont(font);
            }
        }
    }    
    

    private static class ComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof String) {
                String text = (String) value;
                if (text.equals("Artículos") || text.equals("Pedidos") || text.equals("Clientes") ||
                    text.equals("Registros") || text.equals("Ubicaciones") || text.equals("Almacenes") ||
                    text.equals("Otros")) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    setBackground(Color.LIGHT_GRAY);
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setBackground(list.getBackground());
                }
                setText(text.trim());
            }
            return this;
        }
    }
}