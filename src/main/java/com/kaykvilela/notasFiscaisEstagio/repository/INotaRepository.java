package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotaRepository extends JpaRepository<NotaFiscal, Integer> {
    List<NotaFiscal> findByClienteCodigo(Integer codigoCliente);
}
