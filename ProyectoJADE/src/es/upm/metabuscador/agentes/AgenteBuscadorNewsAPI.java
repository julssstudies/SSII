package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoBusquedaNewsAPI;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;
import jade.core.Agent;

/**
 * Agente buscador que realiza búsquedas utilizando la API de NewsAPI.ai.
 * Se especializa en noticias relacionadas con energías renovables.
 */
public class AgenteBuscadorNewsAPI extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    // Nombre de la fuente de datos
    private static final String NOMBRE_FUENTE = "NewsAPI.ai"; 
    
    @Override
    protected void setup() {
        System.out.println("Agente Buscador NewsAPI.ai " + getLocalName() + " iniciado (Fuente: " + NOMBRE_FUENTE + ")");
        
        // Registrar el servicio en el DF
        addBehaviour(new ComportamientoRegistroBuscador(this, TIPO_SERVICIO_BUSCADOR, NOMBRE_FUENTE));
        
        // Añadir el comportamiento para buscar noticias
        addBehaviour(new ComportamientoBusquedaNewsAPI(this, NOMBRE_FUENTE)); 
    }
    
    @Override
    protected void takeDown() {
        try {
            // Desregistrar del DF
            Utils.desregistrarServicio(this);
        } catch (Exception e) {
            System.err.println("Error al desregistrar el agente: " + e.getMessage());
        }
        
        System.out.println("Agente Buscador NewsAPI.ai " + getLocalName() + " terminado");
    }
}
