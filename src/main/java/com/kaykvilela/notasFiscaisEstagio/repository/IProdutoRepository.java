package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProdutoRepository extends JpaRepository<Produto, Integer> {
}
