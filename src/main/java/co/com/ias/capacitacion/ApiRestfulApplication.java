package co.com.ias.capacitacion;

import co.com.ias.capacitacion.models.documents.Categoria;
import co.com.ias.capacitacion.models.documents.Producto;
import co.com.ias.capacitacion.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class ApiRestfulApplication implements CommandLineRunner {
	private static final Logger log= LoggerFactory.getLogger(ApiRestfulApplication.class);
	@Autowired
	private ProductoService service;


	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;
	public static void main(String[] args) {
		SpringApplication.run(ApiRestfulApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		reactiveMongoTemplate.dropCollection("productos").subscribe();
		reactiveMongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion, muebles)
				.flatMap(service::saveCategoria)
				.doOnNext(c ->{
					log.info("Categoria creada: " + c.getNombre() + ", Id: " + c.getId());
				}).thenMany(
				Flux.just(new Producto("TV Panasonic Pantalla LCD", null,456.89, electronico),
						new Producto("Sony Camara HD Digital", null,177.89, electronico),
						new Producto("Apple iPod",null, 46.89, electronico),
						new Producto("Sony Notebook",null, 846.89, computacion),
						new Producto("Hewlett Packard Multifuncional", null,200.89, computacion),
						new Producto("Bianchi Bicicleta", null,70.89, deporte),
						new Producto("HP Notebook Omen 17", null,2500.89, computacion),
						new Producto("Mica Cómoda 5 Cajones", null,150.89, muebles),
						new Producto("TV Sony Bravia OLED 4K Ultra HD", null, 2255.89, electronico)
				)
						.flatMap(producto -> {
							producto.setCreateAt(new Date());
							return service.save(producto);
						})
		)
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));

	}
}
