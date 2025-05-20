package es.upm.metabuscador;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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
		SwingUtilities.invokeLater(this::createGUI);
	}

	private void createGUI() {
		frame = new JFrame("MetaBuscador");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
		frame.setLayout(new BorderLayout(20, 20));

		JPanel topPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 10, 40));

		searchField = new JTextField();
		searchField.setFont(new Font("SansSerif", Font.PLAIN, 18));
		topPanel.add(searchField, BorderLayout.CENTER);

		searchButton = new JButton("Buscar");
		searchButton.setFont(new Font("SansSerif", Font.BOLD, 16));
		topPanel.add(searchButton, BorderLayout.EAST);

		frame.add(topPanel, BorderLayout.NORTH);

		resultArea = new JTextArea();
		resultArea.setEditable(false);
		resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		JScrollPane scrollPane = new JScrollPane(resultArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));

		frame.add(scrollPane, BorderLayout.CENTER);

		searchButton.addActionListener(e -> {
			String query = searchField.getText();
			if (!query.isEmpty()) {
				resultArea.setText("Buscando: " + query + "...\n");
				addBehaviour(new BuscarYMostrar(query));
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// Comportamiento que ejecuta la búsqueda de forma secuencial
	class BuscarYMostrar extends OneShotBehaviour {
		private final String consulta;

		public BuscarYMostrar(String consulta) {
			this.consulta = consulta;
		}

		public void action() {
			try {
				// Enviar mensaje al agente buscador
				Utils.enviarMensaje(myAgent, "buscar", consulta);

				// Esperar bloqueantemente respuesta
				ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

				if (msg != null) {
					Object content = msg.getContentObject();

					if (content instanceof List) {
						List<String> resultados = (List<String>) content;
						SwingUtilities.invokeLater(() -> {
							resultArea.setText("");
							if (resultados.isEmpty()) {
								resultArea.append("No se encontraron resultados.\n");
							} else {
								for (String resultado : resultados) {
									resultArea.append("• " + resultado + "\n");
								}
							}
						});
					}
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
	}
}