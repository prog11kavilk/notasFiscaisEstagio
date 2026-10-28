package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    //busca os itens pelo código da nota
    List<Item> findByNotaCodigoNota(Integer codigoNota);

    boolean existsByNotaCodigoNotaAndItem(Integer codigoNota, Integer item);

    Optional<Item> findByNotaCodigoNotaAndItem(Integer codigoNota, Integer item);
}
