package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoBusqueda;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;
import jade.core.Agent;

/**
 * Agente buscador que realiza búsquedas utilizando la API de SerpStack.
 */
public class AgenteBuscador extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    // Nombre de la fuente de datos
    private static final String NOMBRE_FUENTE = "SerpStack"; // Actualizado
    
    @Override
    protected void setup() {
        System.out.println("Agente Buscador " + getLocalName() + " iniciado (Fuente: " + NOMBRE_FUENTE + ")");
        
        // Registrar el servicio en el DF
        addBehaviour(new ComportamientoRegistroBuscador(this, TIPO_SERVICIO_BUSCADOR, NOMBRE_FUENTE));
        
        // Añadir el comportamiento para buscar
        addBehaviour(new ComportamientoBusqueda(this, NOMBRE_FUENTE)); 
    }
    
    @Override
    protected void takeDown() {
        try {
            // Desregistrar del DF
            Utils.desregistrarServicio(this);
        } catch (Exception e) {
            System.err.println("Error al desregistrar el agente: " + e.getMessage());
        }
        
        System.out.println("Agente Buscador " + getLocalName() + " terminado");
    }
}
