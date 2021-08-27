package co.com.ias.capacitacion.models.repository;

import co.com.ias.capacitacion.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {

        }
