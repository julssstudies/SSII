package es.upm.metabuscador;

import jade.core.Agent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class AgenteBuscador extends Agent {

	public void setup() {
		// Crear servicios proporcionados por el agente y registrarlos en la plataforma
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Buscador");
		// establezco el tipo del servicio “buscar” para poder localizarlo cuando haga
		// una busqueda
		sd.setType("buscar");
		sd.addOntologies("ontologia");
		sd.addLanguages(new SLCodec().getName());
		dfd.addServices(sd);
		try {
			// registro el servicio en el DF
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
		// añado un comportamiento cíclico para recibir mensajes que demandan búsquedas
		// y atenderlas
		addBehaviour(new CyclicBehaviourBuscador());
	}

}
