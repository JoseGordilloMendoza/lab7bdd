package lab07;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class home extends JFrame {

    public home() {
        setTitle("Inicio");
        setLayout(new GridLayout(6, 1));

        JLabel titleLabel = new JLabel("Selecciona que deseas hacer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.PLAIN, 18));
        add(titleLabel);

        JButton crudButton = new JButton("Edicion de tablas");
        crudButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CRUDInterface();
                dispose();
            }
        });
        add(crudButton);

        JButton consultasButton = new JButton("Consultas");
        consultasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new consultaInterface();
                dispose();
            }
        });
        add(consultasButton);

        JButton triggersButton = new JButton("Prueba Triggers");
        triggersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(triggersButton);

        JButton procedimientosButton = new JButton("Procedimiento Almacenado");
        procedimientosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(procedimientosButton);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(home::new);
    }
}