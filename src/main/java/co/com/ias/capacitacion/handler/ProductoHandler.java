package co.com.ias.capacitacion.handler;

import co.com.ias.capacitacion.models.documents.Categoria;
import co.com.ias.capacitacion.models.documents.Producto;
import co.com.ias.capacitacion.models.services.ProductoService;
import com.mongodb.internal.connection.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;


import static org.springframework.web.reactive.function.BodyInserters.*;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class ProductoHandler {
    @Autowired
    ProductoService productoService;
    @Autowired
    Validator validator;

    @Value("${config.uploads.path}")
    String path;

    public Mono<ServerResponse> uploadImg(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoService.findById(id).flatMap(p -> {
                    p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "-")
                            .replace(":", "")
                            .replace("\\", ""));
                    return file.transferTo(new File(path + p.getFoto())).then(productoService.save(p));
                })).flatMap(producto -> ServerResponse.created((URI.create("/api/v2/productos"
                        .concat(producto.getId())))).contentType(MediaType.APPLICATION_JSON).body(fromObject(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);

    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return productoService.findById(id).flatMap(p -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(p))).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        return productoMono.flatMap(p -> {
            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors()).map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList().flatMap(list -> ServerResponse.badRequest().body(fromObject(list)));
            } else {
                if (p.getCreateAt() == null) {


                    p.setCreateAt(new Date());
                }
                return productoService.save(p).flatMap(pbd -> ServerResponse.created(URI.create("/api/v2/productos"
                        .concat(pbd.getId()))).contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(p)));
            }

        });
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest request) {
        Mono<Producto> productoMono = request.multipartData().map(multipart -> {
            FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("categoria");
            FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoriaId");
            FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoriaNombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });
        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoMono.flatMap(p -> {
                    p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "-")
                            .replace(":", "")
                            .replace("\\", ""));
                    p.setCreateAt(new Date());
                    return file.transferTo(new File(path + p.getFoto())).then(productoService.save(p));
                })).flatMap(producto -> ServerResponse.created((URI.create("/api/v2/productos"
                        .concat(producto.getId())))).contentType(MediaType.APPLICATION_JSON).body(fromObject(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);

        String id = request.pathVariable("id");

        Mono<Producto> producto = productoService.findById(id);
        return producto.zipWith(producto, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategoria(req.getCategoria());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("api/v2/productos".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON).body(productoService.save(p), Producto.class));

    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Producto> producto = productoService.findById(id);
        return producto.flatMap(p -> productoService.delete(p)
                .then(ServerResponse
                        .noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
