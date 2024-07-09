package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CRUDInterface extends JFrame {

    private JComboBox<String> tableSelector;

    public CRUDInterface() {
        setTitle("Seleccionar Tabla");
        setLayout(new BorderLayout());

        JPanel selectorPanel = new JPanel();
        selectorPanel.add(new JLabel("Selecciona la tabla:"));
        tableSelector = new JComboBox<>(new String[]{"pais", "tamaño_del_articulo",
            "procedencia_de_pedido", "tipo_de_regalo", "turno", "cliente", "region", "tipo_de_articulo",
            "ingrediente", "receta",
            "franquicia", "localidad", "ciudad", "almacen", "ingrediente Almacen", "regalos Almacen"});
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
        Font defaultFont = interfazGeneral.DEFAULT_FONT;

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
                JComboBox comboBox = (JComboBox) component;
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
        String selectedTable = (String) tableSelector.getSelectedItem();
        if (selectedTable != null) {
            switch (selectedTable) {
                case "pais":
                    new pais();
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
                case "ingrediente Almacen":
                    new ingredienteAlmacen();
                    break;
                case "regalos Almacen":
                    new regaloAlmacen();
                    break;
                case "tipo_de_articulo":
                    new tip_art();
                    break;
                case "ingrediente":
                    new ingrediente();
                    break;
                case "receta":
                    new receta();
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Tabla no reconocida", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CRUDInterface::new);
    }
}