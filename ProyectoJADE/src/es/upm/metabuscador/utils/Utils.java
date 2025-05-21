package es.upm.metabuscador.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidades para la plataforma JADE.
 * Proporciona métodos para buscar agentes, enviar mensajes, etc.
 */
public class Utils {
    
    /**
     * Busca todos los agentes que proporcionan un servicio específico.
     * 
     * @param agent El agente que realiza la búsqueda
     * @param tipo El tipo de servicio buscado
     * @return Array con las descripciones de los agentes encontrados
     */    public static DFAgentDescription[] buscarAgentes(Agent agent, String tipo) {
        // Creo el template para buscar servicios de un tipo específico
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(tipo);
        template.addServices(sd);
        
        try {
            // Configurar un tiempo de espera más largo (10 segundos)
            // Usar SearchConstraints para evitar timeout
            jade.domain.FIPAAgentManagement.SearchConstraints sc = new jade.domain.FIPAAgentManagement.SearchConstraints();
            sc.setMaxResults(Long.valueOf(20)); // Aumentar máximo de resultados
            sc.setMaxDepth(Long.valueOf(3));    // Aumentar profundidad de búsqueda
            
            DFAgentDescription[] result = DFService.search(agent, template, sc);
            
            if (result != null && result.length > 0) {
                System.out.println("Utils: Encontrados " + result.length + " agentes de tipo '" + tipo + "'");
                for (DFAgentDescription agente : result) {
                    System.out.println("  - " + agente.getName().getLocalName());
                }
            } else {
                System.out.println("Utils: No se encontraron agentes de tipo '" + tipo + "'");
            }
            
            return result;
        } catch (FIPAException e) {
            System.err.println("Utils: Error buscando agentes de tipo '" + tipo + "': " + e.getMessage());
            e.printStackTrace();
        }
        
        return new DFAgentDescription[0];
    }
    
    /**
     * Busca el primer agente que proporciona un servicio específico.
     * 
     * @param agent El agente que realiza la búsqueda
     * @param tipo El tipo de servicio buscado
     * @return La descripción del agente encontrado o null si no se encuentra
     */
    public static DFAgentDescription buscarAgente(Agent agent, String tipo) {
        DFAgentDescription[] agentes = buscarAgentes(agent, tipo);
        
        if (agentes.length > 0) {
            return agentes[0];
        }
        
        return null;
    }
    
    /**
     * Envía un objeto como contenido de un mensaje ACL a un agente específico.
     * 
     * @param agent El agente que envía el mensaje
     * @param receiver El agente destinatario
     * @param contenido El objeto a enviar
     * @param performativa El tipo de performativa (REQUEST, INFORM, etc.)
     */
    public static void enviarMensaje(Agent agent, AID receiver, Serializable contenido, int performativa) {
        ACLMessage msg = new ACLMessage(performativa);
        msg.addReceiver(receiver);
        msg.setLanguage("Serializable");
        
        try {
            msg.setContentObject(contenido);
            agent.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Envía un objeto como contenido de un mensaje ACL a todos los agentes que proporcionan un servicio específico.
     * 
     * @param agent El agente que envía el mensaje
     * @param tipo El tipo de servicio que deben proporcionar los destinatarios
     * @param contenido El objeto a enviar
     * @param performativa El tipo de performativa (REQUEST, INFORM, etc.)
     * @return Lista de AID de los agentes a los que se ha enviado el mensaje
     */
    public static List<AID> enviarMensajeAServicio(Agent agent, String tipo, Serializable contenido, int performativa) {
        List<AID> receptores = new ArrayList<>();
        DFAgentDescription[] agentes = buscarAgentes(agent, tipo);
        
        if (agentes.length > 0) {
            ACLMessage msg = new ACLMessage(performativa);
            
            for (DFAgentDescription agente : agentes) {
                msg.addReceiver(agente.getName());
                receptores.add(agente.getName());
            }
            
            msg.setLanguage("Serializable");
            
            try {
                msg.setContentObject(contenido);
                agent.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return receptores;
    }
      /**
     * Registra un agente como proveedor de un servicio específico.
     * 
     * @param agent El agente a registrar
     * @param tipo El tipo de servicio
     * @param nombre El nombre del servicio
     */
    public static void registrarServicio(Agent agent, String tipo, String nombre) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType(tipo);
        sd.setName(nombre);
        
        dfd.addServices(sd);
        
        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Desregistra un agente del Directory Facilitator.
     * 
     * @param agent El agente a desregistrar
     * @throws FIPAException Si ocurre un error al desregistrar
     */
    public static void desregistrarServicio(Agent agent) throws FIPAException {
        DFService.deregister(agent);
    }
}
