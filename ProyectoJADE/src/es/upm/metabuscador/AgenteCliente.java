package es.upm.metabuscador;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AgenteCliente extends Agent {

    private JFrame frame;
    private JTextField searchField;
    private JButton searchButton;
    private JTextArea resultArea;

    public void setup() {
        System.out.println("Soy el agente Cliente");

        // Interfaz en el hilo de Swing
        SwingUtilities.invokeLater(() -> createGUI());

        // Comportamiento cíclico de escucha de respuestas
        addBehaviour(new ComportamientoUsuario());
    }

    private void createGUI() {
        frame = new JFrame("MetaBuscador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout(20, 20));

        // Panel superior: como Google, centrado
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 10, 40));

        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        topPanel.add(searchField, BorderLayout.CENTER);

        searchButton = new JButton("Buscar");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(searchButton, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // Panel central: resultados
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));

        frame.add(scrollPane, BorderLayout.CENTER);

        // Acción del botón
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            System.out.println("Lo que escribió el usuario: " + query);
            if (!query.isEmpty()) {
                resultArea.setText("Buscando...\n");
                // Enviar mensaje desde nuevo hilo (para no bloquear la interfaz)
                new Thread(() -> {
                    Utils.enviarMensaje(this, "buscar", query);
                }).start();
            }
        });

        frame.setLocationRelativeTo(null); // Centrar
        frame.setVisible(true);
    }

    // Comportamiento para recibir respuestas
    class ComportamientoUsuario extends CyclicBehaviour {
        public void action() {
        	
        	ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        	if (msg != null) {
        	    SwingUtilities.invokeLater(() -> {
        	        resultArea.setText(""); // Limpiar
        	    });
        	    try {
        	        // INTENTA LEER COMO OBJETO
        	        Object content = msg.getContentObject();

        	        if (content instanceof List) {
        	            List<String> mensajes = (List<String>) content;
        	            SwingUtilities.invokeLater(() -> {
        	                for (String m : mensajes) {
        	                    resultArea.append("• " + m.toString() + "\n");
        	                }
        	                resultArea.setText("...\n");
        	            });
        	        } else {
        	            // No es lista, mostrar genéricamente
        	            SwingUtilities.invokeLater(() -> {
        	                resultArea.append("Respuesta no reconocida del agente.");
        	            });
        	        }
        	    } catch (UnreadableException | ClassCastException e) {
        	        // Fallback: intentamos leer como texto
        	        try {
        	            String contenido = msg.getContent();
        	            SwingUtilities.invokeLater(() -> {
        	                resultArea.append(contenido + "\n");
        	            });
        	        } catch (Exception ex) {
        	            ex.printStackTrace();
        	        }
        	    }
        	}
        }
    }
}
