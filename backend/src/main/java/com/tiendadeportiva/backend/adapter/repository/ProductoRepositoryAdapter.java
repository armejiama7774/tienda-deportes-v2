package com.tiendadeportiva.backend.adapter.repository;

import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa ProductoRepositoryPort usando Spring Data JPA.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Adaptador de infraestructura que implementa puerto de dominio
 * - Traduce llamadas del dominio a implementación específica (Spring Data)
 * - Aísla el dominio de detalles de persistencia
 * - Facilita cambio de proveedor de BD sin afectar dominio
 */
@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {
    
    private final ProductoRepository springDataRepository;
    
    public ProductoRepositoryAdapter(ProductoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }
    
    // =============================================
    // OPERACIONES BÁSICAS DE PERSISTENCIA
    // =============================================
    
    @Override
    public Producto save(Producto producto) {
        return springDataRepository.save(producto);
    }
    
    @Override
    public Optional<Producto> findById(Long id) {
        return springDataRepository.findById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return springDataRepository.existsById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
    
    // =============================================
    // CONSULTAS DE NEGOCIO ESPECÍFICAS
    // =============================================
    
    @Override
    public List<Producto> findAllActiveOrderByCreationDate() {
        return springDataRepository.findByActivoTrueOrderByFechaCreacionDesc();
    }
    
    @Override
    public boolean existsActiveByNameAndBrand(String nombre, String marca) {
        // Implementar método en ProductoRepository si no existe
        return springDataRepository.existsByNombreAndMarcaAndActivoTrue(nombre, marca);
    }
    
    @Override
    public List<Producto> findActiveByCategoryOrderByName(String categoria) {
        return springDataRepository.findByCategoriaAndActivoTrueOrderByNombre(categoria);
    }
    
    @Override
    public List<Producto> findActiveByBrandOrderByName(String marca) {
        return springDataRepository.findByMarcaAndActivoTrueOrderByNombre(marca);
    }
    
    @Override
    public List<Producto> findActiveByNameContainingIgnoreCase(String nombre) {
        return springDataRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }
    
    @Override
    public List<Producto> findActiveByPriceRangeOrderByPrice(BigDecimal precioMin, BigDecimal precioMax) {
        return springDataRepository.findByPrecioBetweenAndActivoTrueOrderByPrecio(precioMin, precioMax);
    }
    
    @Override
    public List<Producto> findActiveWithStock() {
        return springDataRepository.findByStockDisponibleGreaterThanAndActivoTrue(0);
    }
    
    @Override
    public List<String> findDistinctActiveCategories() {
        return springDataRepository.findDistinctCategoriaByActivoTrue();
    }
    
    @Override
    public List<String> findDistinctActiveBrands() {
        return springDataRepository.findDistinctMarcaByActivoTrue();
    }
}