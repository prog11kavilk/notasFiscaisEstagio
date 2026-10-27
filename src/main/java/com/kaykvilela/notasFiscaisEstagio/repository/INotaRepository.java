package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INotaRepository extends JpaRepository<NotaFiscal, Integer> {
    boolean existsByCodigoNota(Integer codigoNota);

    Optional<NotaFiscal> findByCodigoNota(Integer codigoNota);

    List<NotaFiscal> findAllByOrderByCodigoNota();

    List<NotaFiscal> findByClienteCodigoOrderByCodigoNota(String codigo);

    List<NotaFiscal> findByClienteNomeContainingIgnoreCaseOrderByCodigoNota(String nome);
}
