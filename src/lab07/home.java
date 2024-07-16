package lab07;

import java.sql.*;

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
                try{
                    new consultaInterface();
                }
                catch(SQLException x){
                    x.getMessage();
                }
                dispose();
            }
        });
        add(consultasButton);

        JButton vistasButton = new JButton("Vistas");
        vistasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    new vistasDB();
                } catch (SQLException x) {
                    x.getMessage();
                }
                dispose();
            }
        });
        this.add(vistasButton);
        
        JButton triggersButton = new JButton("Consultas Con Funciones Agregadas");
        triggersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new ConsultasConFuncionesAgregadas();
                } catch (SQLException x) {
                    x.getMessage();
                }
                dispose();
            }
        });
        add(triggersButton);

        JButton procedimientosButton = new JButton("Procedimiento Almacenado");
        procedimientosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new ReportesApp().setVisible(true);
                } catch (Exception x) {
                    x.getMessage();
                }
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