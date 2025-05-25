package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoBusquedaWiki;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;
import jade.core.Agent;

/**
 * Agente buscador que realiza búsquedas utilizando la API de Wikipedia/MediaWiki.
 */
public class AgenteBuscadorWiki extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    // Nombre de la fuente de datos
    private static final String NOMBRE_FUENTE = "Wikipedia";
      @Override
    protected void setup() {
        System.out.println("*** WIKIPEDIA *** Agente Buscador Wikipedia " + getLocalName() + " iniciado (Fuente: " + NOMBRE_FUENTE + ")");
        
        // Registrar el servicio en el DF
        System.out.println("*** WIKIPEDIA *** Registrando servicio en DF...");
        addBehaviour(new ComportamientoRegistroBuscador(this, TIPO_SERVICIO_BUSCADOR, NOMBRE_FUENTE));
        
        // Añadir el comportamiento para buscar en Wikipedia
        System.out.println("*** WIKIPEDIA *** Añadiendo comportamiento de búsqueda...");
        addBehaviour(new ComportamientoBusquedaWiki(this, NOMBRE_FUENTE)); 
        
        System.out.println("*** WIKIPEDIA *** Agente completamente iniciado y listo para recibir búsquedas");
    }
    
    @Override
    protected void takeDown() {
        try {
            // Desregistrar del DF
            Utils.desregistrarServicio(this);
        } catch (Exception e) {
            System.err.println("Error al desregistrar el agente: " + e.getMessage());
        }
        
        System.out.println("Agente Buscador Wikipedia " + getLocalName() + " terminado");
    }
}
