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
 * Comportamiento para realizar búsquedas utilizando la API de SerpStack.
 */
public class ComportamientoBusqueda extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    private String nombreFuente;
    private static final String SERPSTACK_API_KEY = [API KEY AQUÍ]; // Reemplazar con la clave de API real
    private static final String SERPSTACK_API_URL = "http://api.serpstack.com/search";

    /**
     * Constructor del comportamiento de búsqueda.
     * 
     * @param agente El agente buscador
     * @param nombreFuente Nombre de la fuente de datos (SerpStack)
     */
    public ComportamientoBusqueda(Agent agente, String nombreFuente) {
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
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") recibió solicitud de búsqueda");
            
            try {
                // Extraer los parámetros de búsqueda del mensaje
                Object contenido = mensaje.getContentObject();
                
                if (contenido instanceof ParametrosBusqueda) {
                    ParametrosBusqueda parametros = (ParametrosBusqueda) contenido;                    
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
                    
                    System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") envió " 
                            + resultados.size() + " resultados de búsqueda");
                }
            } catch (UnreadableException e) {
                System.err.println("Error al leer el contenido del mensaje en " + myAgent.getLocalName() + ": " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error al enviar los resultados desde " + myAgent.getLocalName() + ": " + e.getMessage());
            }
        } else {
            // Si no hay mensajes, bloquear el comportamiento hasta que llegue uno
            block();
        }
    }
      /**
     * Método que realiza una búsqueda en la API de SerpStack.
     * 
     * @param termino Término a buscar
     * @return Lista de resultados encontrados (máximo 3)
     */    private List<ResultadoBusqueda> buscar(String termino) {
        List<ResultadoBusqueda> resultados = new ArrayList<>();
        HttpURLConnection con = null;
        System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Buscando: " + termino);

        try {
            String encodedQuery = URLEncoder.encode(termino, "UTF-8");            // Creamos la URL correcta para SerpStack
            String urlString = SERPSTACK_API_URL + 
                "?access_key=" + SERPSTACK_API_KEY + 
                "&query=" + encodedQuery + 
                "&output=json";
            
            System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - URL API: " + urlString);
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(15000); // 15 segundos tiempo de espera para conexión
            con.setReadTimeout(20000);    // 20 segundos tiempo de espera para lectura

            int status = con.getResponseCode();
            System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Status Code: " + status);

            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                  String responseContent = content.toString();
                System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Respuesta recibida:");
                System.out.println("----------- INICIO RESPUESTA API -----------");
                System.out.println(responseContent); // Útil para depuración
                System.out.println("------------ FIN RESPUESTA API ------------");
                
                JSONObject jsonResponse = new JSONObject(responseContent);
                
                // Verificar si hay un error explícito en la respuesta de la API
                if (jsonResponse.has("error")) {
                    JSONObject errorObj = jsonResponse.getJSONObject("error");
                    String errorInfo = errorObj.optString("info", "Error desconocido de SerpStack");
                    
                    System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - API Error: " + errorInfo);
                    resultados.add(new ResultadoBusqueda("Error API SerpStack", errorInfo, nombreFuente));
                    return resultados;
                }

                // Procesar los resultados orgánicos
                if (jsonResponse.has("organic_results")) {
                    JSONArray organicResults = jsonResponse.getJSONArray("organic_results");
                    System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Encontrados " + 
                        organicResults.length() + " resultados orgánicos");
                    
                    // Limitar a un máximo de 3 resultados
                    int numResults = Math.min(organicResults.length(), 3);
                    
                    for (int i = 0; i < numResults; i++) {
                        JSONObject result = organicResults.getJSONObject(i);
                          // Extraer campos relevantes: posición, título y URL
                        int position = result.optInt("position", i+1);
                        String titulo = result.optString("title", "Sin título");
                        String resultUrl = result.optString("url", "#");
                          // Crear un resultado simplificado con formato mejorado
                        StringBuilder descripcion = new StringBuilder();
                        descripcion.append("Posición: ").append(position).append("\n");
                        descripcion.append("URL: ").append(resultUrl);
                        
                        resultados.add(new ResultadoBusqueda(
                            titulo,
                            descripcion.toString(),
                            nombreFuente
                        ));
                          // Imprimir el resultado para depuración
                        System.out.println("Resultado #" + (i+1) + ":");
                        System.out.println("  Posición: " + position);
                        System.out.println("  Título: " + titulo);
                        System.out.println("  URL: " + resultUrl);
                    }
                      if (organicResults.length() == 0) {
                        System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - 'organic_results' está vacío.");
                        
                        // Intentar obtener resultados locales si existen
                        if (jsonResponse.has("local_results") && jsonResponse.getJSONArray("local_results").length() > 0) {
                            System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Se encontraron resultados locales alternativos");
                            JSONArray localResults = jsonResponse.getJSONArray("local_results");
                            
                            // Limitar a un máximo de 3 resultados locales
                            int numLocalResults = Math.min(localResults.length(), 3);
                            
                            for (int i = 0; i < numLocalResults; i++) {
                                JSONObject result = localResults.getJSONObject(i);
                                
                                int position = result.optInt("position", i+1);
                                String titulo = result.optString("title", "Sin título");
                                String rating = "Valoración: " + result.optString("rating", "N/A");
                                String reviews = "Reseñas: " + result.optString("reviews", "0");
                                String price = result.optString("price", "");
                                  // Crear una descripción detallada para resultados locales
                                StringBuilder descripcion = new StringBuilder();
                                descripcion.append("Resultado local #").append(position).append("\n");
                                if (!price.isEmpty()) {
                                    descripcion.append("Precio: ").append(price).append("\n");
                                }
                                descripcion.append(rating).append(" (").append(reviews).append(" reseñas)");
                                
                                resultados.add(new ResultadoBusqueda(
                                    titulo,
                                    descripcion.toString(),
                                    nombreFuente
                                ));
                                
                                System.out.println("Resultado Local #" + (i+1) + ":");
                                System.out.println("  Posición: " + position);
                                System.out.println("  Título: " + titulo);
                                System.out.println("  Detalles: " + descripcion);
                            }
                        } 
                        // Intentar obtener preguntas relacionadas si no hay resultados locales
                        else if (jsonResponse.has("related_questions") && jsonResponse.getJSONArray("related_questions").length() > 0) {
                            System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Se encontraron preguntas relacionadas");
                            JSONArray questions = jsonResponse.getJSONArray("related_questions");
                            
                            int numQuestions = Math.min(questions.length(), 3);
                              for (int i = 0; i < numQuestions; i++) {
                                JSONObject question = questions.getJSONObject(i);
                                String questionText = question.optString("question", "Sin pregunta");
                                System.out.println("Original questionText from API: " + questionText); // DEBUG
                                String displayedUrl = question.optString("displayed_url", "");
                                
                                // Limpiar texto repetido en la pregunta (si aplica)
                                int firstQuestionMarkIndex = questionText.indexOf('?');
                                if (firstQuestionMarkIndex != -1) {
                                    questionText = questionText.substring(0, firstQuestionMarkIndex + 1);
                                }
                                // Si no hay '?', se usa questionText tal cual (podría ser "Sin pregunta" o algo inesperado)
                                System.out.println("Processed questionText: " + questionText); // DEBUG
                                
                                // Crear una URL de búsqueda de Google para la pregunta
                                String searchUrl = "";
                                try {
                                    searchUrl = "https://www.google.com/search?q=" + URLEncoder.encode(questionText, "UTF-8");
                                } catch (Exception e) {
                                    searchUrl = "https://www.google.com";
                                }
                                
                                // Crear una descripción detallada con la URL de búsqueda
                                StringBuilder descripcion = new StringBuilder();
                                descripcion.append(questionText).append("\n\n");
                                descripcion.append("URL: ").append(searchUrl);
                                
                                resultados.add(new ResultadoBusqueda(
                                    "Pregunta relacionada #" + (i+1),
                                    descripcion.toString(),
                                    nombreFuente
                                ));
                            }
                        }
                        // Si no hay resultados alternativos
                        else {
                            resultados.add(new ResultadoBusqueda(
                                "Sin resultados para: " + termino, 
                                "La búsqueda no produjo ningún resultado orgánico.", 
                                nombreFuente
                            ));
                        }
                    }
                } else {
                    System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - No se encontraron 'organic_results' en la respuesta JSON.");
                    
                    // Verificar si hay otra información útil en la respuesta
                    boolean resultadosAlternativos = false;
                    
                    // Intentar obtener información de búsqueda
                    if (jsonResponse.has("search_information")) {
                        JSONObject searchInfo = jsonResponse.getJSONObject("search_information");
                        String totalResults = searchInfo.optString("total_results", "");
                        
                        if (!totalResults.isEmpty()) {
                            resultados.add(new ResultadoBusqueda(
                                "Información de búsqueda",
                                "Total de resultados aproximados: " + totalResults,
                                nombreFuente
                            ));
                            resultadosAlternativos = true;
                        }
                    }
                    
                    // Si no se encontró ninguna información alternativa
                    if (!resultadosAlternativos) {
                        resultados.add(new ResultadoBusqueda(
                            "Sin resultados para: " + termino, 
                            "La API no devolvió resultados orgánicos para esta búsqueda.", 
                            nombreFuente
                        ));
                    }
                }
            } else {
                System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Error HTTP: " + status);
                if (con.getErrorStream() != null) {
                    BufferedReader errIn = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String errLine;
                    StringBuilder errContent = new StringBuilder();
                    while ((errLine = errIn.readLine()) != null) {
                        errContent.append(errLine);
                    }
                    errIn.close();
                    System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Error Body: " + errContent.toString());
                    resultados.add(new ResultadoBusqueda("Error HTTP " + status, errContent.toString(), nombreFuente));
                } else {
                    resultados.add(new ResultadoBusqueda("Error HTTP " + status, "No se pudo obtener respuesta del servidor.", nombreFuente));
                }
            }

        } catch (UnsupportedEncodingException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Error de codificación URL: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Codificación", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Error de E/S al llamar a la API: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Red/IO", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Agente " + myAgent.getLocalName() + " (SerpStack) - Error al parsear JSON de la API: " + e.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Formato JSON", e.getMessage(), nombreFuente));
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        
        if (resultados.isEmpty()) {
            System.out.println("Agente " + myAgent.getLocalName() + " (SerpStack) - No se obtuvieron resultados para: " + termino);
            resultados.add(new ResultadoBusqueda("Sin resultados", "No se encontraron resultados para: " + termino, nombreFuente));        }
        return resultados;
    }
}
