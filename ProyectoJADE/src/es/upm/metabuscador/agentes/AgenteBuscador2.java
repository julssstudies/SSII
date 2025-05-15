package es.upm.metabuscador.agentes;

import es.upm.metabuscador.comportamientos.ComportamientoBusqueda;
import es.upm.metabuscador.comportamientos.ComportamientoRegistroBuscador;
import es.upm.metabuscador.utils.Utils;

import jade.core.Agent;

/**
 * Agente buscador que simula búsquedas en una fuente de datos específica.
 * Este agente busca en una base de datos de libros y publicaciones académicas.
 */
public class AgenteBuscador2 extends Agent {
    private static final long serialVersionUID = 1L;
    
    // Constante para el tipo de servicio buscador
    private static final String TIPO_SERVICIO_BUSCADOR = "buscador";
    
    // Nombre de la fuente de datos
    private static final String NOMBRE_FUENTE = "Biblioteca Digital";
    
    // Base de datos simulada con algunos libros y publicaciones
    private static final String[] BASE_DATOS = {
        "Inteligencia Artificial: Un Enfoque Moderno (Russell & Norvig)",
        "Aprendizaje Automático: Una Introducción Práctica (Mitchell)",
        "Fundamentos de Sistemas Multiagente con JADE (Bellifemine et al.)",
        "Distributed Artificial Intelligence: Agent Technology and Applications (Sugawara)",
        "Comunicación entre Agentes: Protocolos y Ontologías",
        "Programación Orientada a Agentes: Teoría y Práctica",
        "Razonamiento en Sistemas Multiagente",
        "Algoritmos Genéticos y Computación Evolutiva",
        "La Web Semántica y los Sistemas Multiagente",
        "Agentes Conversacionales: Diseño e Implementación"
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
