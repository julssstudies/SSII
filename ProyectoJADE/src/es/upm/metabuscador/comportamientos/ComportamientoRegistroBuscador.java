package es.upm.metabuscador.comportamientos;

import es.upm.metabuscador.utils.Utils;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Comportamiento para registrar un agente buscador en el DF.
 * Este comportamiento se ejecuta una vez al inicio del agente.
 */
public class ComportamientoRegistroBuscador extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    
    private String tipo;
    private String nombre;
    
    /**
     * Constructor para registrar un servicio.
     * 
     * @param a El agente que registra el servicio
     * @param tipo El tipo de servicio a registrar
     * @param nombre El nombre del servicio
     */
    public ComportamientoRegistroBuscador(Agent a, String tipo, String nombre) {
        super(a);
        this.tipo = tipo;
        this.nombre = nombre;
    }

    @Override
    public void action() {
        // Registramos el servicio en el DF (Directory Facilitator)
        Utils.registrarServicio(myAgent, tipo, nombre);
        
        System.out.println("Agente " + myAgent.getLocalName() + " registrado como servicio " + 
                tipo + " con nombre " + nombre);
    }
}
