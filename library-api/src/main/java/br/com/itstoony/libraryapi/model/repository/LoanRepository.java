package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LoanRepository extends JpaRepository<Loan, Long> {

//    @Query("SELECT Loan FROM Loan WHERE Loan.costumer = :costumer AND Loan.book = :book AND Loan.loanDate = :loanDate")
    boolean existsByCostumerAndBookAndLoanDate(@Param("costumer") String costumer, @Param("Book") Book book, @Param("loanDate") LocalDate loanDate);

}
