package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    //busca item pelo número da nota
    List<Item> findByNotaNumeroNota(Integer numeroNota);
}
