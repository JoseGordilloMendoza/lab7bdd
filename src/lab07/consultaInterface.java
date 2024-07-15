package lab07;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class consultaInterface extends JFrame {

    public consultaInterface() throws SQLException {
        setTitle("Consultas");
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Seleccionar Tipo de Consulta", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        JComboBox<String> consultaSelector = new JComboBox<>(new String[]{"Consulta Simple", "Consulta de Combinación"});
        consultaSelector.addActionListener(e -> {
            String selectedOption = (String) consultaSelector.getSelectedItem();
            if (selectedOption != null) {
                switch (selectedOption) {
                    case "Consulta Simple":
                        try{
                            new consultaSimple();}
                        catch (SQLException s){
                            s.getMessage();
                        }
                        break;
                    case "Consulta de Combinación":
                        try{
                        new ConsultaCombinada();
                        }
                        catch(SQLException s){
                            s.getMessage();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Selecciona el tipo de consulta:"));
        panel.add(consultaSelector);
        add(panel, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> {
            dispose(); // Cierra la ventana actual
            new home(); // Vuelve a la ventana Home
        });
        add(btnCerrar, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setVisible(true);
    }
}
