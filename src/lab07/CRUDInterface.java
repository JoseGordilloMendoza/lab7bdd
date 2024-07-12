package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CRUDInterface extends JFrame {

    private JComboBox<Object> tableSelector;

    public CRUDInterface() {
        setTitle("Seleccionar Tabla");
        setLayout(new BorderLayout());

        JPanel selectorPanel = new JPanel();
        selectorPanel.add(new JLabel("Selecciona la tabla:"));

        // Crear categorías de tablas
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        

        // Agregar tablas relacionadas a cada categoría
        model.addElement("articulo");
        model.addElement("pedidoArticulos");
        model.addElement("pedidoBase");
        model.addElement("boleta");
        model.addElement("pais");
        model.addElement("registroScooter");
        model.addElement("repartidor");
        model.addElement("tamaño_del_articulo");
        model.addElement("procedencia_de_pedido");
        model.addElement("tipo_de_regalo");
        model.addElement("turno");
        model.addElement("cliente");
        model.addElement("region");
        model.addElement("franquicia");
        model.addElement("localidad");
        model.addElement("ciudad");
        model.addElement("almacen");
        model.addElement("ingredienteAlmacen");
        model.addElement("regaloAlmacen");
        model.addElement("facturaGas");
        model.addElement("regalo");
        model.addElement("tip_art");
        model.addElement("ingrediente");
        model.addElement("receta_det");
        model.addElement("receta");
        model.addElement("scooter");

        tableSelector = new JComboBox<>(model);
        tableSelector.setRenderer(new ComboBoxRenderer());
        selectorPanel.add(tableSelector);
        add(selectorPanel, BorderLayout.CENTER);

        JButton btnSeleccionar = new JButton("Seleccionar");
        btnSeleccionar.addActionListener(e -> seleccionarTabla());
        add(btnSeleccionar, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setVisible(true);

        // Aplicar la fuente predeterminada a toda la interfaz
        applyDefaultFont();
    }

    private void applyDefaultFont() {
        Font defaultFont = new Font("Arial", Font.PLAIN, 12);

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
                case "tamaño_del_articulo":
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
                case "regaloAlmacen":
                    new regaloAlmacen();
                    break;
                case "facturaGas":
                    new facturaGas();
                    break;
                case "regalo":
                    new regalo();
                    break;
                case "tip_art":
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CRUDInterface::new);
    }

    // Renderer personalizado para el JComboBox
    private static class ComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof String) {
                String text = (String) value;
                if (text.equals("Artículos") || text.equals("Pedidos") || text.equals("Clientes") ||
                    text.equals("Registros") || text.equals("Ubicaciones") || text.equals("Almacenes") ||
                    text.equals("Otros")) {
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                setText(text);
            }
            return this;
        }
    }
}
