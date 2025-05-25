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
        setSize(800, 600); // Aumentamos un poco el tamaño por defecto
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Colores y Fuentes
        Color colorFondoPrincipal = new Color(240, 240, 245); // Un gris claro
        Color colorPanelBusqueda = new Color(220, 220, 225);
        Color colorBoton = new Color(70, 130, 180); // Azul acero
        Color colorTextoBoton = Color.WHITE;
        Font fuenteEtiquetas = new Font("Arial", Font.BOLD, 14);
        Font fuenteTexto = new Font("Arial", Font.PLAIN, 12);
        Font fuenteResultados = new Font("Verdana", Font.PLAIN, 12);

        // Panel principal
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15)); // Más relleno
        panelPrincipal.setBackground(colorFondoPrincipal);

        // Panel superior (búsqueda)
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0)); // Aumentamos espacio horizontal
        panelBusqueda.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelBusqueda.setBackground(colorPanelBusqueda);

        JLabel lblBusqueda = new JLabel("Término de búsqueda:");
        lblBusqueda.setFont(fuenteEtiquetas);
        txtBusqueda = new JTextField(30); // Un poco más ancho
        txtBusqueda.setFont(fuenteTexto);
        btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(fuenteEtiquetas);
        btnBuscar.setBackground(colorBoton);
        btnBuscar.setForeground(colorTextoBoton);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Aumentamos espacio horizontal
        panelBotones.setOpaque(false); // Hacemos transparente para que tome el color del panelBusqueda
        panelBotones.add(btnBuscar);

        JButton btnAbrirUrl = new JButton("Abrir URL");
        btnAbrirUrl.setToolTipText("Abrir URL seleccionada en el navegador");
        btnAbrirUrl.setFont(fuenteEtiquetas);
        btnAbrirUrl.setBackground(colorBoton);
        btnAbrirUrl.setForeground(colorTextoBoton);
        btnAbrirUrl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirUrlSeleccionada();
            }
        });
        panelBotones.add(btnAbrirUrl);

        panelBusqueda.add(lblBusqueda, BorderLayout.WEST);
        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(panelBotones, BorderLayout.EAST);

        // Panel de resultados usando JTextArea con manejo de URLs
        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        txtResultados.setLineWrap(true);
        txtResultados.setWrapStyleWord(true);
        txtResultados.setFont(fuenteResultados); // Nueva fuente para resultados
        txtResultados.setMargin(new Insets(5, 5, 5, 5)); // Margen interno

        JScrollPane scrollPane = new JScrollPane(txtResultados);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Resultados de la Búsqueda"),
            new EmptyBorder(5,5,5,5)
        ));


        // Panel de estado
        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.setOpaque(false); // Hacemos transparente
        lblEstado = new JLabel("Listo para buscar");
        lblEstado.setFont(new Font("Arial", Font.ITALIC, 12));
        lblEstado.setBorder(new EmptyBorder(5,0,0,0)); // Espacio superior
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
            txtResultados.append("TÍTULO: " + (resultado.getTitulo() != null ? resultado.getTitulo() : "N/A") + "\n");
            txtResultados.append("FUENTE: " + (resultado.getFuente() != null ? resultado.getFuente() : "N/A") + "\n");
            
            txtResultados.append("DESCRIPCIÓN:\n");
            String descripcion = resultado.getDescripcion();
            String urlParaAbrir = null; 

            if (descripcion != null && !descripcion.isEmpty()) {
                String[] lineasDesc = descripcion.split("\\n");
                for (String lineaD : lineasDesc) {
                    txtResultados.append("  " + lineaD + "\n"); // Indentamos cada línea de la descripción
                    // Extraer la URL si la línea comienza con "URL:"
                    if (lineaD.trim().startsWith("URL:")) { 
                        urlParaAbrir = lineaD.substring(lineaD.indexOf(":") + 1).trim();
                    }
                    // También verificamos si la línea es una URL directa 
                    // y si aún no hemos encontrado una URL con el prefijo "URL:"
                    else if (urlParaAbrir == null && (lineaD.trim().startsWith("http://") || lineaD.trim().startsWith("https://"))) {
                        urlParaAbrir = lineaD.trim();
                    }
                }
            } else {
                txtResultados.append("  (Descripción no disponible)\n");
            }

            // Añadimos el texto de ayuda para abrir la URL si se determinó alguna
            if (urlParaAbrir != null && !urlParaAbrir.isEmpty()) {
                txtResultados.append("\n  * URL para abrir en navegador: " + urlParaAbrir + "\n");
                txtResultados.append("    (Selecciona esta URL y haz clic en el botón 'Abrir URL')\n");
            }
            txtResultados.append("----------------------------------------\n"); // Separador entre resultados
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
