package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

}
