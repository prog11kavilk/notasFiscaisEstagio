package com.kaykvilela.notasFiscaisEstagio.repository;

import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByCpf(String cpf);

}
