package es.upm.metabuscador.agentes;

import es.upm.metabuscador.gui.InterfazBuscador;
import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;
import es.upm.metabuscador.utils.Utils;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Agente de interfaz que proporciona una interfaz gráfica al usuario y
 * se comunica con el agente coordinador para realizar búsquedas.
 */
public class AgenteInterfaz extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio coordinador
    private static final String TIPO_SERVICIO_COORDINADOR = "coordinador";
      private InterfazBuscador gui;
    
    @Override
    protected void setup() {
        System.out.println("Agente " + getLocalName() + " iniciado");
        
        try {
            // Crear e inicializar la interfaz gráfica con soporte para EDT de Swing
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        gui = new InterfazBuscador(AgenteInterfaz.this);
                        gui.setVisible(true);
                        System.out.println("Interfaz gráfica iniciada correctamente");
                    } catch (Exception e) {
                        System.err.println("Error al iniciar la interfaz gráfica: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error crítico al iniciar la interfaz gráfica: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Añadir comportamiento para recibir resultados de búsqueda
        addBehaviour(new ComportamientoRecepcionResultados());
    }
    
    @Override
    protected void takeDown() {
        // Cerrar la interfaz gráfica
        if (gui != null) {
            gui.setVisible(false);
            gui.dispose();
        }
        
        System.out.println("Agente " + getLocalName() + " terminado");
    }
    
    /**
     * Método para iniciar una búsqueda.
     * Llamado desde la interfaz gráfica.
     *     * @param parametros Parámetros de la búsqueda
     */
    public void realizarBusqueda(ParametrosBusqueda parametros) {
        System.out.println("Iniciando búsqueda: " + parametros.getTerminoBusqueda());
        
        try {
            // Buscar el agente coordinador
            DFAgentDescription coordinador = Utils.buscarAgente(this, TIPO_SERVICIO_COORDINADOR);
            
            if (coordinador != null) {
                try {
                    // Crear mensaje para el coordinador
                    ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
                    mensaje.addReceiver(coordinador.getName());
                    mensaje.setContentObject(parametros);
                    
                    // Enviar la solicitud
                    send(mensaje);
                    
                    System.out.println("Solicitud de búsqueda enviada al coordinador: " + 
                            coordinador.getName().getLocalName());
                    
                    // Esperar un momento para que se procese el mensaje antes de comprobar resultados
                    try {
                        Thread.sleep(500); // Esperar 500ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    
                } catch (IOException e) {
                    System.err.println("Error al enviar solicitud de búsqueda: " + e.getMessage());
                    mostrarErrorEnInterfaz("Error al enviar la solicitud: " + e.getMessage());
                }
            } else {
                System.err.println("No se encontró un agente coordinador");
                mostrarErrorEnInterfaz("No se encontró un agente coordinador. Compruebe que todos los agentes están en ejecución.");
            }
        } catch (Exception e) {
            System.err.println("Error inesperado en realizarBusqueda: " + e.getMessage());
            e.printStackTrace();
            mostrarErrorEnInterfaz("Error inesperado: " + e.getMessage());
        }    }
    
    /**
     * Método auxiliar para mostrar errores en la interfaz
     */
    private void mostrarErrorEnInterfaz(final String mensajeError) {
        if (gui != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    List<ResultadoBusqueda> listaError = new ArrayList<>();
                    listaError.add(new ResultadoBusqueda(
                        "Error en la búsqueda", 
                        mensajeError + "\n\nCompruebe la consola para más detalles.", 
                        "Sistema"
                    ));
                    gui.mostrarResultados(listaError);
                }
            });
        }
    }
    
    /**
     * Comportamiento para recibir los resultados de la búsqueda.
     */
    private class ComportamientoRecepcionResultados extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;
        
        @Override
        public void action() {
            // Template para mensajes de tipo INFORM (resultados)
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage mensaje = myAgent.receive(template);
            
            if (mensaje != null) {
                try {
                    Object contenido = mensaje.getContentObject();
                    
                    if (contenido instanceof ResultadoBusqueda[]) {
                        ResultadoBusqueda[] resultadosArray = (ResultadoBusqueda[]) contenido;
                        final List<ResultadoBusqueda> resultados = new ArrayList<>();
                        for (ResultadoBusqueda resultado : resultadosArray) {
                            resultados.add(resultado);
                        }
                        
                        System.out.println("Recibidos " + resultados.size() + " resultados de búsqueda");
                        
                        // Actualizar la interfaz con los resultados
                        if (gui != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    gui.mostrarResultados(resultados);
                                }
                            });
                        }
                    } else {
                        System.err.println("Contenido recibido no es de tipo ResultadoBusqueda[]: " + 
                            (contenido != null ? contenido.getClass().getName() : "null"));
                        mostrarErrorEnInterfaz("Formato de respuesta incorrecto");
                    }
                } catch (UnreadableException e) {
                    System.err.println("Error al leer los resultados: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErrorEnInterfaz("Error al procesar resultados: " + e.getMessage());
                }
            } else {
                block();
            }
        }
    }
}
