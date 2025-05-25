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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Comportamiento para realizar búsquedas utilizando la API de NewsAPI.ai (EventRegistry).
 * Busca noticias basadas en el término proporcionado por el usuario.
 */
public class ComportamientoBusquedaNewsAPI extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    private String nombreFuente;
    
    // Configuración de la API EventRegistry (anteriormente NewsAPI.ai)
    private static final String NEWSAPI_API_KEY = "3658dc14-0f61-45ae-bba9-20f8b2acc499";
    // URL para buscar artículos por palabra clave
    private static final String NEWSAPI_API_URL = "https://eventregistry.org/api/v1/article/getArticles";

    /**
     * Constructor del comportamiento de búsqueda.
     * 
     * @param agente El agente buscador
     * @param nombreFuente Nombre de la fuente de datos (NewsAPI.ai / EventRegistry)
     */
    public ComportamientoBusquedaNewsAPI(Agent agente, String nombreFuente) {
        super(agente);
        this.nombreFuente = nombreFuente;
    }
    
    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage mensaje = myAgent.receive(template);
        
        if (mensaje != null) {
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") recibió solicitud de búsqueda");
            
            try {
                Object contenido = mensaje.getContentObject();
                
                if (contenido instanceof ParametrosBusqueda) {
                    ParametrosBusqueda parametros = (ParametrosBusqueda) contenido;                    
                    List<ResultadoBusqueda> resultados = buscarNoticias(parametros.getTerminoBusqueda());
                    
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(mensaje.getSender());
                    ResultadoBusqueda[] resultadosArray = resultados.toArray(new ResultadoBusqueda[0]);
                    respuesta.setContentObject(resultadosArray);
                    myAgent.send(respuesta);
                    
                    System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") envió " 
                            + resultados.size() + " resultados de búsqueda para el término: " + parametros.getTerminoBusqueda());
                }
            } catch (UnreadableException e) {
                System.err.println("Error al leer el contenido del mensaje en " + myAgent.getLocalName() + ": " + e.getMessage());
                e.printStackTrace();
                enviarErrorAlCoordinador(mensaje, "Error interno al procesar solicitud: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error al enviar los resultados desde " + myAgent.getLocalName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private void enviarErrorAlCoordinador(ACLMessage solicitudOriginal, String mensajeError) {
        try {
            ACLMessage respuestaError = new ACLMessage(ACLMessage.INFORM);
            respuestaError.addReceiver(solicitudOriginal.getSender());
            List<ResultadoBusqueda> listaError = new ArrayList<>();
            listaError.add(new ResultadoBusqueda("Error en " + nombreFuente, mensajeError, nombreFuente));
            respuestaError.setContentObject(listaError.toArray(new ResultadoBusqueda[0]));
            myAgent.send(respuestaError);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje de error al coordinador: " + e.getMessage());
        }
    }
    
    /**
     * Método que realiza una búsqueda en la API de EventRegistry usando getArticles.
     * Busca noticias relacionadas con el término de búsqueda proporcionado.
     * 
     * @param termino Término de búsqueda original
     * @return Lista de resultados encontrados (máximo 3)
     */
    private List<ResultadoBusqueda> buscarNoticias(String termino) {
        List<ResultadoBusqueda> resultados = new ArrayList<>();
        HttpURLConnection con = null;
        System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Buscando noticias con término: " + termino);

        try {
            URL url = new URL(NEWSAPI_API_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setConnectTimeout(15000); 
            con.setReadTimeout(20000);     

            JSONObject requestBody = new JSONObject();
            requestBody.put("keyword", termino); // Usar el término de búsqueda como palabra clave
            requestBody.put("resultType", "articles"); 
            requestBody.put("articlesSortBy", "rel"); 
            requestBody.put("articlesCount", 3); 
            requestBody.put("apiKey", NEWSAPI_API_KEY);
            
            
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Enviando petición a: " + NEWSAPI_API_URL);
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Cuerpo de la petición: " + requestBody.toString());

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = con.getResponseCode();
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Status Code: " + status);

            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = inReader.readLine()) != null) {
                    content.append(inputLine);
                }
                inReader.close();

                String responseContent = content.toString();
                System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Respuesta recibida:");
                // System.out.println("----------- INICIO RESPUESTA API -----------\");
                // System.out.println(responseContent); // Descomentar para depuración detallada de la respuesta
                // System.out.println("------------ FIN RESPUESTA API ------------\");
                JSONObject jsonResponse = new JSONObject(responseContent);
                
                if (jsonResponse.has("error")) {
                    Object errorObj = jsonResponse.get("error");
                    String errorInfo;
                    if (errorObj instanceof JSONObject) {
                        errorInfo = ((JSONObject) errorObj).optString("message", errorObj.toString());
                    } else {
                        errorInfo = errorObj.toString();
                    }
                    System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - API Error: " + errorInfo);
                    resultados.add(new ResultadoBusqueda("Error API " + nombreFuente, errorInfo, nombreFuente));
                    return resultados;
                }

                if (jsonResponse.has("articles") && jsonResponse.getJSONObject("articles").has("results")) {
                    JSONArray articlesArray = jsonResponse.getJSONObject("articles").getJSONArray("results");
                    System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Encontrados " + 
                        articlesArray.length() + " artículos para el término: " + termino);
                    
                    for (int i = 0; i < articlesArray.length(); i++) {
                        JSONObject article = articlesArray.getJSONObject(i);
                        
                        String tituloArticulo = article.optString("title", "Sin título");
                        String cuerpoArticulo = article.optString("body", "No hay descripción disponible.");
                        String urlArticulo = article.optString("url", "#");
                        String fechaArticuloStr = article.optString("dateTime", "");
                        
                        String nombreFuenteArticulo = "Desconocida";
                        if (article.has("source") && article.getJSONObject("source").has("title")) {
                            nombreFuenteArticulo = article.getJSONObject("source").optString("title", "Desconocida");
                        }
                        
                        String fechaFormateada = formatearFecha(fechaArticuloStr);
                        
                        StringBuilder descripcionBuilder = new StringBuilder();
                        descripcionBuilder.append("Fuente Original: ").append(nombreFuenteArticulo).append("");
                        if (!fechaFormateada.isEmpty()) {
                            descripcionBuilder.append("Fecha: ").append(fechaFormateada).append("");
                        }
                        String resumenCuerpo = cuerpoArticulo.length() > 250 ? cuerpoArticulo.substring(0, 250) + "..." : cuerpoArticulo;
                        descripcionBuilder.append("Descripción: ").append(resumenCuerpo).append("");
                        descripcionBuilder.append("URL: ").append(urlArticulo);
                        
                        resultados.add(new ResultadoBusqueda(
                            tituloArticulo,
                            descripcionBuilder.toString(),
                            nombreFuente // Nombre del agente/API, no de la fuente individual del artículo
                        ));
                        System.out.println("Artículo procesado #" + (i+1) + ": " + tituloArticulo);
                    }
                } else {
                    System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - No se encontraron 'articles.results' en la respuesta JSON para el término: " + termino);
                }
            } else {
                System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Error HTTP: " + status);
                String errorBody = "No se pudo obtener detalle del error del servidor.";
                if (con.getErrorStream() != null) {
                    try (BufferedReader errIn = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                        String errLine;
                        StringBuilder errContent = new StringBuilder();
                        while ((errLine = errIn.readLine()) != null) {
                            errContent.append(errLine);
                        }
                        errorBody = errContent.toString();
                    } catch (IOException e_stream) { // Renombrar variable para evitar conflicto de nombres
                        System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Error al leer error stream: " + e_stream.getMessage());
                    }
                }
                System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Error Body: " + errorBody);
                resultados.add(new ResultadoBusqueda("Error HTTP " + status, errorBody, nombreFuente));
            }

        } catch (IOException e_io) { // Renombrar para evitar conflicto
            System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Error de E/S al llamar a la API: " + e_io.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Red/IO", e_io.getMessage(), nombreFuente));
            e_io.printStackTrace();
        } catch (JSONException e_json) { // Renombrar variable para evitar conflicto de nombres
            System.err.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - Error al parsear JSON de la API: " + e_json.getMessage());
            resultados.add(new ResultadoBusqueda("Error de Formato JSON", e_json.getMessage(), nombreFuente));
            e_json.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        
        if (resultados.isEmpty()) {
            System.out.println("Agente " + myAgent.getLocalName() + " (" + nombreFuente + ") - No se obtuvieron resultados para: " + termino);
            resultados.add(new ResultadoBusqueda("Sin resultados para '" + termino + "'", "La API no devolvió artículos para el término de búsqueda.", nombreFuente));
        }
        
        return resultados;
    }
    
    private String formatearFecha(String dateStr) {
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            }        } catch (Exception e_date) { // Renombrar variable para evitar conflicto de nombres
            System.err.println("Error al formatear fecha: " + dateStr + " - " + e_date.getMessage());
        }
        return "";
    }
}
