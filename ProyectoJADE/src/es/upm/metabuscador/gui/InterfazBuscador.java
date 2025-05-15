package es.upm.metabuscador.gui;

import es.upm.metabuscador.agentes.AgenteInterfaz;
import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Interfaz gráfica Swing para el metabuscador.
 * Permite ingresar términos de búsqueda y muestra los resultados.
 */
public class InterfazBuscador extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private AgenteInterfaz agente;
    private JTextField txtBusqueda;
    private JTextArea txtResultados;
    private JButton btnBuscar;
    private JPanel panelPrincipal;
    private JLabel lblEstado;
    
    /**
     * Constructor de la interfaz.
     * 
     * @param agente El agente de interfaz que maneja esta GUI
     */
    public InterfazBuscador(AgenteInterfaz agente) {
        this.agente = agente;
        inicializarComponentes();
    }
    
    /**
     * Inicializa los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Configuración de la ventana
        setTitle("Metabuscador JADE");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel superior (búsqueda)
        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 0));
        JLabel lblBusqueda = new JLabel("Término de búsqueda:");
        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        
        panelBusqueda.add(lblBusqueda, BorderLayout.WEST);
        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);
        
        // Panel de resultados
        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        txtResultados.setLineWrap(true);
        txtResultados.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtResultados);
        
        // Panel de estado
        JPanel panelEstado = new JPanel(new BorderLayout());
        lblEstado = new JLabel("Listo para buscar");
        panelEstado.add(lblEstado, BorderLayout.WEST);
        
        // Añadir componentes al panel principal
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelEstado, BorderLayout.SOUTH);
        
        // Añadir panel principal a la ventana
        add(panelPrincipal);
        
        // Configurar eventos
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarBusqueda();
            }
        });
        
        // Permitir buscar con Enter
        txtBusqueda.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarBusqueda();
            }
        });
    }
    
    /**
     * Realiza una búsqueda con el término ingresado.
     */
    private void realizarBusqueda() {
        String termino = txtBusqueda.getText().trim();
        
        if (!termino.isEmpty()) {
            txtResultados.setText("Buscando '" + termino + "'...\n");
            lblEstado.setText("Buscando...");
            agente.realizarBusqueda(new ParametrosBusqueda(termino));
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor, ingrese un término de búsqueda", 
                "Campo vacío", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Muestra los resultados de la búsqueda en la interfaz.
     * 
     * @param resultados Lista de resultados de búsqueda
     */
    public void mostrarResultados(List<ResultadoBusqueda> resultados) {
        // Limpiar área de resultados
        txtResultados.setText("");
        
        // Mostrar cantidad de resultados
        txtResultados.append("Se encontraron " + resultados.size() + " resultados:\n\n");
        
        // Mostrar cada resultado
        for (ResultadoBusqueda resultado : resultados) {
            txtResultados.append("Título: " + resultado.getTitulo() + "\n");
            txtResultados.append("Fuente: " + resultado.getFuente() + "\n");
            txtResultados.append("Descripción: " + resultado.getDescripcion() + "\n");
            txtResultados.append("----------------------------------------\n");
        }
        
        // Actualizar estado
        lblEstado.setText("Búsqueda completada. " + resultados.size() + " resultados encontrados.");
    }
}
