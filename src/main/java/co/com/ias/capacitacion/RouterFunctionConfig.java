package co.com.ias.capacitacion;

import co.com.ias.capacitacion.handler.ProductoHandler;
import co.com.ias.capacitacion.models.documents.Producto;
import co.com.ias.capacitacion.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import  org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {


    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")),handler::listar)
                .andRoute(GET("/api/v2/productos/{id}"),handler::ver)
                .andRoute(POST("/api/v2/productos/"),handler::crear)
                .andRoute(PUT("/api/v2/productos/{id}"),handler::editar)
                .andRoute(DELETE("/api/v2/productos/{id}"),handler::eliminar)
                .andRoute(POST("/api/v2/productos/upload/{id}"),handler::uploadImg);

    }
}
