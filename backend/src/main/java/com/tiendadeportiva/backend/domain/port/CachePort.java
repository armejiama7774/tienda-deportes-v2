package com.tiendadeportiva.backend.domain.port;

import com.tiendadeportiva.backend.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del dominio para operaciones de cache.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Abstrae operaciones de cache del dominio
 * - Independiente de Redis, Caffeine o cualquier implementación
 * - Facilita testing sin dependencias externas
 */
public interface CachePort {
    
    /**
     * Almacena un producto en cache
     * @param key Clave para el cache
     * @param producto Producto a cachear
     */
    void put(String key, Producto producto);
    
    /**
     * Obtiene un producto del cache
     * @param key Clave del cache
     * @return Optional con el producto si existe en cache
     */
    Optional<Producto> get(String key);
    
    /**
     * Elimina un producto del cache
     * @param key Clave del cache
     */
    void evict(String key);
    
    /**
     * Limpia todo el cache de productos
     */
    void clear();
    
    /**
     * Almacena una lista de productos en cache
     * @param key Clave para el cache
     * @param productos Lista de productos
     */
    void putList(String key, List<Producto> productos);
    
    /**
     * Obtiene una lista de productos del cache
     * @param key Clave del cache
     * @return Optional con la lista si existe en cache
     */
    Optional<List<Producto>> getList(String key);
}