package es.upm.metabuscador.comportamientos;

import es.upm.metabuscador.modelo.ParametrosBusqueda;
import es.upm.metabuscador.modelo.ResultadoBusqueda;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Comportamiento para realizar búsquedas utilizando la API de Wikipedia/MediaWiki.
 */
public class ComportamientoBusquedaWiki extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    private String nombreFuente;
    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";

    /**
     * Constructor del comportamiento de búsqueda en Wikipedia.
     * 
     * @param agente El agente buscador
     * @param nombreFuente Nombre de la fuente de datos (Wikipedia)
     */
    public ComportamientoBusquedaWiki(Agent agente, String nombreFuente) {
        super(agente);
        this.nombreFuente = nombreFuente;
    }
      @Override
    public void action() {
        // Crear una plantilla para recibir solo mensajes de tipo REQUEST
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        
        // Esperar la recepción de un mensaje que cumpla con la plantilla
        ACLMessage mensaje = myAgent.receive(template);
        
        if (mensaje != null) {
            System.out.println("*** WIKIPEDIA *** Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") recibió solicitud de búsqueda");
            
            try {
                // Extraer los parámetros de búsqueda del mensaje
                Object contenido = mensaje.getContentObject();
                
                if (contenido instanceof ParametrosBusqueda) {
                    ParametrosBusqueda parametros = (ParametrosBusqueda) contenido;
                    System.out.println("*** WIKIPEDIA *** Buscando término: " + parametros.getTerminoBusqueda());
                    
                    // Realizar la búsqueda con los parámetros recibidos
                    List<ResultadoBusqueda> resultados = buscar(parametros.getTerminoBusqueda());
                    
                    // Preparar respuesta
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(mensaje.getSender());
                    
                    // Convertir la lista a un array para asegurar la serialización
                    ResultadoBusqueda[] resultadosArray = new ResultadoBusqueda[resultados.size()];
                    resultadosArray = resultados.toArray(resultadosArray);
                    respuesta.setContentObject(resultadosArray);
                    
                    // Enviar resultados
                    myAgent.send(respuesta);
                    
                    System.out.println("*** WIKIPEDIA *** Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") envió " 
                            + resultados.size() + " resultados de búsqueda");
                } else {
                    System.err.println("*** WIKIPEDIA *** Contenido no es ParametrosBusqueda: " + contenido.getClass().getName());
                }
            } catch (UnreadableException e) {
                System.err.println("*** WIKIPEDIA *** Error al leer el contenido del mensaje en " + myAgent.getLocalName() + ": " + e.getMessage());
            } catch (IOException e) {
                System.err.println("*** WIKIPEDIA *** Error al enviar los resultados desde " + myAgent.getLocalName() + ": " + e.getMessage());
            }
        } else {
            // Si no hay mensajes, bloquear el comportamiento hasta que llegue uno
            block();
        }
    }
    
    /**
     * Método que realiza una búsqueda en la API de Wikipedia.
     * 
     * @param termino Término a buscar
     * @return Lista de resultados encontrados (máximo 3)
     */
    private List<ResultadoBusqueda> buscar(String termino) {
        List<ResultadoBusqueda> resultados = new ArrayList<>();
        HttpURLConnection con = null;
        BufferedReader in = null;
        
        System.out.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - Buscando: " + termino);
        
        try {
            // Codificar el término de búsqueda
            String terminoCodificado = URLEncoder.encode(termino, "UTF-8");
            
            // Construir la URL de la API de Wikipedia
            String urlStr = WIKIPEDIA_API_URL + 
                    "?action=query" +
                    "&list=search" +
                    "&srsearch=" + terminoCodificado +
                    "&srlimit=3" +
                    "&format=json";
            
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "MetabuscadorJADE/1.0 (https://example.com/contact)");
            
            int status = con.getResponseCode();
            
            if (status == 200) {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                
                // Analizar la respuesta JSON
                JSONObject jsonResponse = new JSONObject(content.toString());
                
                if (jsonResponse.has("query") && jsonResponse.getJSONObject("query").has("search")) {
                    JSONArray searchResults = jsonResponse.getJSONObject("query").getJSONArray("search");
                    
                    for (int i = 0; i < searchResults.length(); i++) {
                        JSONObject result = searchResults.getJSONObject(i);
                        
                        String titulo = result.getString("title");
                        String snippet = result.optString("snippet", "");
                        
                        // Limpiar el fragmento de etiquetas HTML
                        snippet = snippet.replaceAll("<[^>]*>", "");
                        
                        // Construir la URL del artículo
                        String urlArticulo = "https://es.wikipedia.org/wiki/" + 
                                URLEncoder.encode(titulo.replace(" ", "_"), "UTF-8");
                        
                        StringBuilder descripcion = new StringBuilder();
                        descripcion.append(snippet);
                        if (!snippet.isEmpty()) {
                            descripcion.append("\n\n");
                        }
                        descripcion.append("URL: ").append(urlArticulo);
                        
                        resultados.add(new ResultadoBusqueda(
                            titulo,
                            descripcion.toString(),
                            nombreFuente
                        ));
                        
                        // Imprimir el resultado para depuración
                        System.out.println("Resultado Wikipedia #" + (i+1) + ":");
                        System.out.println("  Título: " + titulo);
                        System.out.println("  Snippet: " + snippet);
                        System.out.println("  URL: " + urlArticulo);
                    }
                } else {
                    System.out.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - No se encontraron resultados para: " + termino);
                    resultados.add(new ResultadoBusqueda(
                        "Sin resultados para: " + termino, 
                        "La búsqueda en Wikipedia no produjo ningún resultado.", 
                        nombreFuente
                    ));
                }
            } else {
                System.err.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - Error HTTP: " + status);
                resultados.add(new ResultadoBusqueda(
                    "Error HTTP " + status, 
                    "No se pudo conectar con la API de Wikipedia.", 
                    nombreFuente
                ));
            }

        } catch (UnsupportedEncodingException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - Error de codificación URL: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Codificación", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - Error de E/S al llamar a la API: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Red/IO", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - Error al parsear JSON de la API: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Formato JSON", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar BufferedReader: " + e.getMessage());
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }
        
        if (resultados.isEmpty()) {
            System.out.println("Agente " + myAgent.getLocalName() + " (Wikipedia) - No se obtuvieron resultados para: " + termino);
            resultados.add(new ResultadoBusqueda("Sin resultados", "No se encontraron resultados para: " + termino, nombreFuente));
        }        
        return resultados;
    }
}
