package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT CASE WHEN ( COUNT(L.id) > 0) THEN TRUE ELSE FALSE END " +
            "FROM Loan L WHERE L.book = :book AND (L.returned = NULL OR L.returned = FALSE ) ")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

}
