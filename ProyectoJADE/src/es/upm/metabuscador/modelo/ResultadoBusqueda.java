package es.upm.metabuscador.modelo;

import java.io.Serializable;

/**
 * Clase que representa un resultado de búsqueda.
 * Incluye el título, la descripción y la fuente del resultado.
 */
public class ResultadoBusqueda implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String titulo;
    private String descripcion;
    private String fuente;
    
    /**
     * Constructor con todos los campos.
     * 
     * @param titulo Título del resultado
     * @param descripcion Descripción o contenido del resultado
     * @param fuente Nombre de la fuente que proporciona el resultado
     */
    public ResultadoBusqueda(String titulo, String descripcion, String fuente) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuente = fuente;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getFuente() {
        return fuente;
    }
    
    public void setFuente(String fuente) {
        this.fuente = fuente;
    }
    
    @Override
    public String toString() {
        return "[" + fuente + "] " + titulo + ": " + descripcion;
    }
}
