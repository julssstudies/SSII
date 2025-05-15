package es.upm.metabuscador.comportamientos;

import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;
import es.upm.metabuscador.utils.Utils;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comportamiento cíclico para recibir solicitudes de búsqueda,
 * distribuirlas a los agentes buscadores y agregar los resultados.
 */
public class ComportamientoRecepcionBusqueda extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    // Mapa para almacenar las búsquedas en curso (solicitante -> respuestas pendientes)
    private Map<AID, BusquedaEnCurso> busquedasEnCurso = new HashMap<>();
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    /**
     * Constructor del comportamiento.
     * 
     * @param agente El agente coordinador
     */
    public ComportamientoRecepcionBusqueda(Agent agente) {
        super(agente);
    }
    
    @Override
    public void action() {
        // Primero procesamos solicitudes de búsqueda entrantes
        procesarSolicitudesBusqueda();
        
        // Luego procesamos resultados de búsqueda entrantes
        procesarResultadosBusqueda();
    }
    
    /**
     * Procesa las solicitudes de búsqueda recibidas del agente de interfaz.
     */
    private void procesarSolicitudesBusqueda() {
        // Template para mensajes de tipo REQUEST
        MessageTemplate templateSolicitud = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage mensajeSolicitud = myAgent.receive(templateSolicitud);
        
        if (mensajeSolicitud != null) {
            try {
                Object contenido = mensajeSolicitud.getContentObject();
                
                if (contenido instanceof ParametrosBusqueda) {
                    ParametrosBusqueda parametros = (ParametrosBusqueda) contenido;
                    
                    System.out.println("Agente " + myAgent.getLocalName() + 
                            " recibió solicitud para buscar: " + parametros.getTerminoBusqueda());
                    
                    // Buscar todos los agentes buscadores disponibles
                    DFAgentDescription[] buscadores = Utils.buscarAgentes(myAgent, TIPO_SERVICIO_BUSCADOR);
                    
                    if (buscadores.length > 0) {
                        // Almacenar información de la búsqueda en curso
                        BusquedaEnCurso busqueda = new BusquedaEnCurso(
                                mensajeSolicitud.getSender(), 
                                buscadores.length,
                                parametros.getTerminoBusqueda()
                        );
                        busquedasEnCurso.put(mensajeSolicitud.getSender(), busqueda);
                        
                        // Distribuir la búsqueda a todos los buscadores
                        for (DFAgentDescription buscador : buscadores) {
                            ACLMessage solicitudBuscador = new ACLMessage(ACLMessage.REQUEST);
                            solicitudBuscador.addReceiver(buscador.getName());
                            solicitudBuscador.setContentObject(parametros);
                            myAgent.send(solicitudBuscador);
                            
                            System.out.println("Enviando solicitud a: " + buscador.getName().getLocalName());
                        }
                    } else {
                        // Si no hay buscadores disponibles, enviar respuesta vacía
                        ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                        respuesta.addReceiver(mensajeSolicitud.getSender());
                        respuesta.setContentObject(new ArrayList<ResultadoBusqueda>());
                        myAgent.send(respuesta);
                        
                        System.out.println("No se encontraron agentes buscadores disponibles");
                    }
                }
            } catch (UnreadableException e) {
                System.err.println("Error al leer la solicitud: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error al enviar la solicitud a los buscadores: " + e.getMessage());
            }
        }
    }
    
    /**
     * Procesa los resultados recibidos de los agentes buscadores.
     */
    private void procesarResultadosBusqueda() {
        // Template para mensajes de tipo INFORM (resultados)
        MessageTemplate templateResultado = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage mensajeResultado = myAgent.receive(templateResultado);
        
        if (mensajeResultado != null) {
            // Buscar a qué búsqueda corresponde este resultado
            for (Map.Entry<AID, BusquedaEnCurso> entry : busquedasEnCurso.entrySet()) {
                BusquedaEnCurso busqueda = entry.getValue();
                  try {
                    Object contenido = mensajeResultado.getContentObject();
                    
                    if (contenido instanceof ResultadoBusqueda[]) {
                        ResultadoBusqueda[] resultadosArray = (ResultadoBusqueda[]) contenido;
                        List<ResultadoBusqueda> resultados = new ArrayList<>();
                        for (ResultadoBusqueda resultado : resultadosArray) {
                            resultados.add(resultado);
                        }
                        
                        // Añadir los resultados recibidos
                        busqueda.agregarResultados(resultados);
                        busqueda.decrementarPendientes();
                        
                        System.out.println("Recibidos " + resultados.size() + 
                                " resultados de " + mensajeResultado.getSender().getLocalName() + 
                                ". Quedan " + busqueda.getPendientes() + " agentes por responder.");
                          // Si ya hemos recibido todas las respuestas, enviar los resultados agregados
                        if (busqueda.isCompleta()) {                            // Preparar mensaje con todos los resultados
                            ACLMessage respuestaFinal = new ACLMessage(ACLMessage.INFORM);
                            respuestaFinal.addReceiver(entry.getKey());
                            
                            // Convertir la lista a un array para asegurar la serialización
                            List<ResultadoBusqueda> listaResultados = busqueda.getResultados();
                            ResultadoBusqueda[] finalResultadosArray = listaResultados.toArray(new ResultadoBusqueda[listaResultados.size()]);
                            respuestaFinal.setContentObject(finalResultadosArray);
                            
                            myAgent.send(respuestaFinal);
                            
                            System.out.println("Enviados " + busqueda.getResultados().size() + 
                                    " resultados a " + entry.getKey().getLocalName());
                            
                            // Eliminar esta búsqueda del mapa
                            busquedasEnCurso.remove(entry.getKey());
                            break;
                        }
                    }
                } catch (UnreadableException e) {
                    System.err.println("Error al leer los resultados: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Error al enviar los resultados agregados: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Clase interna para mantener el estado de una búsqueda en curso.
     */
    private class BusquedaEnCurso {
        private AID solicitante;
        private int pendientes;
        private String terminoBusqueda;
        private List<ResultadoBusqueda> resultados = new ArrayList<>();
        
        /**
         * Constructor.
         * 
         * @param solicitante Agente que solicitó la búsqueda
         * @param pendientes Número de respuestas pendientes
         * @param terminoBusqueda Término que se está buscando
         */
        public BusquedaEnCurso(AID solicitante, int pendientes, String terminoBusqueda) {
            this.solicitante = solicitante;
            this.pendientes = pendientes;
            this.terminoBusqueda = terminoBusqueda;
        }
        
        /**
         * Agrega resultados a la lista.
         * 
         * @param nuevosResultados Resultados a agregar
         */
        public void agregarResultados(List<ResultadoBusqueda> nuevosResultados) {
            resultados.addAll(nuevosResultados);
        }
        
        /**
         * Decrementa el contador de respuestas pendientes.
         */
        public void decrementarPendientes() {
            pendientes--;
        }
        
        /**
         * Comprueba si la búsqueda está completa.
         * 
         * @return true si no quedan respuestas pendientes
         */
        public boolean isCompleta() {
            return pendientes <= 0;
        }
        
        /**
         * Obtiene los resultados acumulados.
         * 
         * @return Lista de resultados
         */
        public List<ResultadoBusqueda> getResultados() {
            return resultados;
        }
        
        /**
         * Obtiene el número de respuestas pendientes.
         * 
         * @return Número de respuestas pendientes
         */
        public int getPendientes() {
            return pendientes;
        }
    }
}
