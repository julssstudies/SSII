package es.upm.metabuscador.modelo;

import java.io.Serializable;

/**
 * Clase que representa los parámetros de una búsqueda.
 * Incluye los términos de búsqueda y cualquier otro parámetro necesario.
 */
public class ParametrosBusqueda implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String terminoBusqueda;
    
    /**
     * Constructor con el término de búsqueda.
     * 
     * @param terminoBusqueda Término a buscar
     */
    public ParametrosBusqueda(String terminoBusqueda) {
        this.terminoBusqueda = terminoBusqueda;
    }
    
    /**
     * Obtiene el término de búsqueda.
     * 
     * @return El término de búsqueda
     */
    public String getTerminoBusqueda() {
        return terminoBusqueda;
    }
    
    /**
     * Establece el término de búsqueda.
     * 
     * @param terminoBusqueda El término de búsqueda
     */
    public void setTerminoBusqueda(String terminoBusqueda) {
        this.terminoBusqueda = terminoBusqueda;
    }
    
    @Override
    public String toString() {
        return "Búsqueda: " + terminoBusqueda;
    }
}
