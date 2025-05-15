package es.upm.metabuscador.comportamientos;

import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comportamiento para realizar búsquedas en un agente buscador.
 * Este comportamiento espera mensajes con parámetros de búsqueda,
 * realiza la búsqueda y devuelve los resultados.
 */
public class ComportamientoBusqueda extends Behaviour {
    private static final long serialVersionUID = 1L;
    
    private String nombreFuente;
    private String[] baseDatos;  // Simulación de base de datos con términos
    private boolean terminado = false;
    
    /**
     * Constructor del comportamiento de búsqueda.
     * 
     * @param agente El agente buscador
     * @param nombreFuente Nombre de la fuente de datos
     * @param baseDatos Array con datos de ejemplo para buscar
     */
    public ComportamientoBusqueda(Agent agente, String nombreFuente, String[] baseDatos) {
        super(agente);
        this.nombreFuente = nombreFuente;
        this.baseDatos = baseDatos;
    }
    
    @Override
    public void action() {
        // Crear un template para recibir solo mensajes de tipo REQUEST
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        
        // Esperar la recepción de un mensaje que cumpla con el template
        ACLMessage mensaje = myAgent.receive(template);
        
        if (mensaje != null) {
            System.out.println("Agente " + myAgent.getLocalName() + " recibió solicitud de búsqueda");
            
            try {
                // Extraer los parámetros de búsqueda del mensaje
                Object contenido = mensaje.getContentObject();
                
                if (contenido instanceof ParametrosBusqueda) {
                    ParametrosBusqueda parametros = (ParametrosBusqueda) contenido;                    // Realizar la búsqueda con los parámetros recibidos
                    List<ResultadoBusqueda> resultados = buscar(parametros.getTerminoBusqueda());
                    
                    // Preparar respuesta
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(mensaje.getSender());
                    
                    // Convertir la lista a un array para asegurar la serialización
                    ResultadoBusqueda[] resultadosArray = new ResultadoBusqueda[resultados.size()];
                    resultadosArray = resultados.toArray(resultadosArray);
                    respuesta.setContentObject(resultadosArray);
                    
                    // Enviar resultados
                    myAgent.send(respuesta);
                    
                    System.out.println("Agente " + myAgent.getLocalName() + " envió " 
                            + resultados.size() + " resultados de búsqueda");
                }
            } catch (UnreadableException e) {
                System.err.println("Error al leer el contenido del mensaje: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error al enviar los resultados: " + e.getMessage());
            }
        } else {
            // Si no hay mensajes, bloquear el comportamiento hasta que llegue uno
            block();
        }
    }
    
    /**
     * Método que simula una búsqueda en la base de datos.
     * 
     * @param termino Término a buscar
     * @return Lista de resultados encontrados
     */
    private List<ResultadoBusqueda> buscar(String termino) {
        List<ResultadoBusqueda> resultados = new ArrayList<>();
        
        // Convertir el término a minúsculas para búsqueda insensible a mayúsculas
        String terminoLower = termino.toLowerCase();
        
        // Simular retraso de búsqueda (entre 500ms y 2s)
        try {
            Thread.sleep((long) (Math.random() * 1500 + 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Buscar en la base de datos simulada
        for (int i = 0; i < baseDatos.length; i++) {
            if (baseDatos[i].toLowerCase().contains(terminoLower)) {
                // Crear un resultado con título, descripción y fuente
                ResultadoBusqueda resultado = new ResultadoBusqueda(
                        "Resultado " + (i+1) + " para '" + termino + "'",
                        baseDatos[i],
                        nombreFuente
                );
                
                resultados.add(resultado);
            }
        }
        
        return resultados;
    }
    
    @Override
    public boolean done() {
        return terminado;
    }
}
