package com.tiendadeportiva.backend.factory;

/**
 * Excepción específica para errores en la creación de productos usando Factory Pattern.
 * 
 * MANEJO DE ERRORES EN FACTORY PATTERN:
 * Proporciona información detallada sobre errores específicos de creación,
 * permitiendo diferentes estrategias de recuperación según el tipo de error.
 */
public class ProductoCreationException extends RuntimeException {
    
    private final String factoryType;
    private final String productType;
    private final String errorCode;
    
    public ProductoCreationException(String factoryType, String productType, String errorCode, String message) {
        super(message);
        this.factoryType = factoryType;
        this.productType = productType;
        this.errorCode = errorCode;
    }
    
    public ProductoCreationException(String factoryType, String productType, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.factoryType = factoryType;
        this.productType = productType;
        this.errorCode = errorCode;
    }
    
    public String getFactoryType() { return factoryType; }
    public String getProductType() { return productType; }
    public String getErrorCode() { return errorCode; }
    
    @Override
    public String toString() {
        return String.format("ProductoCreationException{factory='%s', product='%s', error='%s', message='%s'}", 
                           factoryType, productType, errorCode, getMessage());
    }
}

/**
 * Excepción para errores de validación específicos del Factory Pattern.
 */
class ProductoValidationException extends ProductoCreationException {
    
    private final String campoInvalido;
    private final Object valorRecibido;
    
    public ProductoValidationException(String factoryType, String productType, String campoInvalido, 
                                     Object valorRecibido, String message) {
        super(factoryType, productType, "VALIDATION_ERROR", message);
        this.campoInvalido = campoInvalido;
        this.valorRecibido = valorRecibido;
    }
    
    public String getCampoInvalido() { return campoInvalido; }
    public Object getValorRecibido() { return valorRecibido; }
}
