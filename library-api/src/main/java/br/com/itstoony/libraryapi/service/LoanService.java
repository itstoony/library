package br.com.itstoony.libraryapi.service;

import br.com.itstoony.libraryapi.api.model.entity.Loan;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);
}
