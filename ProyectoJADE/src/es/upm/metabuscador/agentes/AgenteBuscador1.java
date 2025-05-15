package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoBusqueda;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;

import jade.core.Agent;

/**
 * Agente buscador que simula búsquedas en una fuente de datos específica.
 * Este agente busca en una base de datos de noticias tecnológicas.
 */
public class AgenteBuscador1 extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    // Nombre de la fuente de datos
    private static final String NOMBRE_FUENTE = "Noticias Tecnológicas";
    
    // Base de datos simulada con algunos artículos tecnológicos
    private static final String[] BASE_DATOS = {
        "Nuevos avances en inteligencia artificial permiten detectar enfermedades con mayor precisión",
        "La realidad virtual revoluciona la forma de entrenar a profesionales médicos",
        "Investigadores desarrollan un nuevo algoritmo capaz de predecir tendencias del mercado",
        "El machine learning mejora la eficiencia en la producción industrial",
        "Grandes empresas tecnológicas invierten en el desarrollo de energías renovables",
        "Nueva generación de robots asistenciales para el cuidado de personas mayores",
        "La computación cuántica promete revolucionar la criptografía actual",
        "Dispositivos inteligentes que permiten monitorizar la salud en tiempo real",
        "El internet de las cosas mejora la gestión de ciudades inteligentes",
        "Avances en el desarrollo de coches autónomos reducen los accidentes de tráfico"
    };
    
    @Override
    protected void setup() {
        System.out.println("Agente Buscador " + getLocalName() + " iniciado (Fuente: " + NOMBRE_FUENTE + ")");
        
        // Registrar el servicio en el DF
        addBehaviour(new ComportamientoRegistroBuscador(this, TIPO_SERVICIO_BUSCADOR, NOMBRE_FUENTE));
        
        // Añadir el comportamiento para buscar
        addBehaviour(new ComportamientoBusqueda(this, NOMBRE_FUENTE, BASE_DATOS));
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
