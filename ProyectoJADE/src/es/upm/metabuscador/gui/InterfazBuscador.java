package es.upm.metabuscador.gui;

import es.upm.metabuscador.agentes.AgenteInterfaz;
import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotones.add(btnBuscar);
        
        JButton btnAbrirUrl = new JButton("Abrir URL");
        btnAbrirUrl.setToolTipText("Abrir URL seleccionada en el navegador");
        btnAbrirUrl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirUrlSeleccionada();
            }
        });
        panelBotones.add(btnAbrirUrl);
        
        panelBusqueda.add(lblBusqueda, BorderLayout.WEST);
        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(panelBotones, BorderLayout.EAST);// Panel de resultados usando JTextArea con manejo de URLs
        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        txtResultados.setLineWrap(true);
        txtResultados.setWrapStyleWord(true);
        txtResultados.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
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
    }    /**
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
    }    /**
     * Muestra los resultados de la búsqueda en la interfaz.
     * 
     * @param resultados Lista de resultados de búsqueda
     */    public void mostrarResultados(List<ResultadoBusqueda> resultados) {
        // Limpiar área de resultados
        txtResultados.setText("");
        
        // Mostrar cantidad de resultados
        txtResultados.append("Se encontraron " + resultados.size() + " resultados:\n\n");
        
        // Mostrar cada resultado
        for (ResultadoBusqueda resultado : resultados) {
            txtResultados.append("Título: " + resultado.getTitulo() + "\n");
            txtResultados.append("Fuente: " + resultado.getFuente() + "\n");
            txtResultados.append("Descripción: " + resultado.getDescripcion() + "\n");
            
            // Si hay URLs en la descripción, extraer y mostrar mensaje informativo
            if (resultado.getDescripcion().contains("URL:")) {
                String[] lineas = resultado.getDescripcion().split("\n");
                for (String linea : lineas) {
                    if (linea.trim().startsWith("URL:")) {
                        String url = linea.substring(linea.indexOf(":") + 1).trim();
                        txtResultados.append("\n* URL para abrir en navegador: " + url + "\n");
                        txtResultados.append("  (Selecciona esta URL y haz clic en el botón 'Abrir URL')\n");
                    }
                }
            }
            
            txtResultados.append("----------------------------------------\n");
        }
        
        // Actualizar estado
        lblEstado.setText("Búsqueda completada. " + resultados.size() + " resultados encontrados.");
        
        // Hacer que el texto vuelva al inicio
        txtResultados.setCaretPosition(0);
    }
    
    /**
     * Método para abrir una URL seleccionada en el área de resultados
     */
    private void abrirUrlSeleccionada() {
        String seleccionado = txtResultados.getSelectedText();
        
        if (seleccionado != null && !seleccionado.trim().isEmpty()) {
            // Intentar extraer una URL válida de la selección
            String urlPosible = seleccionado.trim();
            
            // Asegurarse de que comienza con http:// o https://
            if (urlPosible.startsWith("http://") || urlPosible.startsWith("https://")) {
                abrirUrl(urlPosible);
            } else if (urlPosible.startsWith("www.")) {
                abrirUrl("http://" + urlPosible);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se ha detectado una URL válida en el texto seleccionado.\n" +
                    "Por favor, seleccione una URL que empiece con http://, https:// o www.", 
                    "URL no válida", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione primero una URL en los resultados", 
                "Selección vacía", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Método para abrir una URL en el navegador predeterminado
     */
    private void abrirUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                lblEstado.setText("Abriendo URL en navegador: " + url);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Su sistema no soporta la apertura automática de URLs.\n" +
                    "Por favor, copie y pegue la URL en su navegador: " + url, 
                    "No se puede abrir URL", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al abrir la URL: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
