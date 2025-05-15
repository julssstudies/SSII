package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoRecepcionBusqueda;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;

import jade.core.Agent;

/**
 * Agente coordinador que gestiona la distribución de búsquedas a los agentes buscadores
 * y la agregación de resultados para devolverlos al agente de interfaz.
 */
public class AgenteCoordinador extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio coordinador
    private static final String TIPO_SERVICIO_COORDINADOR = "coordinador";
    
    @Override
    protected void setup() {
        System.out.println("Agente Coordinador " + getLocalName() + " iniciado");
        
        // Registrar el servicio en el DF
        addBehaviour(new ComportamientoRegistroBuscador(this, TIPO_SERVICIO_COORDINADOR, "CoordinadorMetabuscador"));
        
        // Añadir el comportamiento para recibir solicitudes de búsqueda y distribuirlas
        addBehaviour(new ComportamientoRecepcionBusqueda(this));
    }
    
    @Override
    protected void takeDown() {
        try {
            // Desregistrar del DF
            Utils.desregistrarServicio(this);
        } catch (Exception e) {
            System.err.println("Error al desregistrar el agente: " + e.getMessage());
        }
        
        System.out.println("Agente Coordinador " + getLocalName() + " terminado");
    }
}
