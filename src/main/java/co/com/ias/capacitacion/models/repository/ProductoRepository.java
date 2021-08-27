package co.com.ias.capacitacion.models.repository;

import co.com.ias.capacitacion.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoRepository extends ReactiveMongoRepository<Producto,String> {

}
