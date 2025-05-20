package es.upm.metabuscador;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import jade.content.lang.sl.SLCodec;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CyclicBehaviourBuscador extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;
	public static final int LIMITES = 50;
	String cadenaBuscar;
	List<String> respuesta = new ArrayList<String>();

	public void action() {
//El comportamiento espera a que le llegue un mensaje de tipo REQUEST al agente y lo hace en modo no bloqueante
		ACLMessage msg = this.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
//Espera a que llegue un nuevo mensaje
		if (msg != null) {
			try {
//Cuando llega un nuevo mensaje, se realiza la búsqueda
				System.out.println(msg.getSender().getName() + ":" + (String) msg.getContentObject());
				cadenaBuscar = (String) msg.getContentObject();
			} catch (UnreadableException e) {
// TODO Auto-generated catch block
				e.printStackTrace();
			}
//Creamos un comportamiento paralelo para lanzar los buscadores en hilos independientes
//El comportamiento espera a que finalicen todos los hilos con WHEN_ALL y cuando finalizan envía un mensaje
			ParallelBehaviour paralelo = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL) {
				public int onEnd() {
					try {
//Cuanto todos los hilos han finalizado, creamos un mensaje de respuesta al cliente
						ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
						aclMessage.addReceiver(msg.getSender());
						aclMessage.setOntology("ontologia");
						aclMessage.setLanguage(new SLCodec().getName());
						aclMessage.setEnvelope(new Envelope());
						aclMessage.getEnvelope().setPayloadEncoding("ISO8859_1");
						aclMessage.setContentObject((Serializable) respuesta);
						respuesta = new ArrayList<String>();
						this.myAgent.send(aclMessage);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return super.onEnd();
				}
			};
//Creamos un hilo independiente
			ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
//Creamos un comportamiento ComportamientoMUIA
			OneShotBehaviour b1 = new ComportamientoBing();
//Metemos el comportamiento en un hilo independiente como un subcomportamiento del comportamiento paralelo
			paralelo.addSubBehaviour(tbf.wrap(b1));
			ThreadedBehaviourFactory tbf2 = new ThreadedBehaviourFactory();
			OneShotBehaviour b2 = new ComportamientoGoogle();
			paralelo.addSubBehaviour(tbf2.wrap(b2));
//Añadimos el comportamiento paralelo al agente
			myAgent.addBehaviour(paralelo);
		} // if
//Cuando el comportamiento no recibe mensajes, se queda bloqueado esperando a que lleguen nuevos mensajes
		else {
			this.block();
		}
	} // action

	public class ComportamientoBing extends OneShotBehaviour {
		String sitio = "https://www.bing.com/";
		List<String> respuesta3 = new ArrayList<String>();

		public void action() {
			respuesta3 = buscarCadena(sitio);
			if (respuesta3.size() > 0)
				respuesta.addAll(respuesta3);
		}
	}

	public class ComportamientoGoogle extends OneShotBehaviour {
		String sitio = "https://www.google.com/";
		List<String> respuesta4 = new ArrayList<String>();

		public void action() {
			respuesta4 = buscarCadena(sitio);
			if (respuesta4.size() > 0)
				respuesta.addAll(respuesta4);
		}
	}

	public List<String> buscarCadena(String sitio) {
		Scanner scanner;
		String temp;
		List<String> respuestaBuscar = new ArrayList();
		try {
			
			URL url = new URL(sitio + "search?q=" + cadenaBuscar.replaceAll("\\s+", "+"));
			scanner = new Scanner(url.openStream());
			while (true) {
//a la izquierda tienes > o <\ y a la derecha > o <\
				temp = scanner.findWithinHorizon(Pattern.compile("[^><\"]*" + cadenaBuscar + "[^><\"]*"), 0);
				if (temp == null)
					break;
				respuestaBuscar.add(sitio + ": " + temp);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return respuestaBuscar;
	}
}