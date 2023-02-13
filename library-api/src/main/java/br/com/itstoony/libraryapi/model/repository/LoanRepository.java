package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT CASE WHEN ( COUNT(L.id) > 0) THEN TRUE ELSE FALSE END " +
            "FROM Loan L WHERE L.book = :book AND (L.returned = NULL OR L.returned = FALSE ) ")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    @Query(value = "SELECT l FROM Loan l join l.book b WHERE b.isbn = :isbn or l.costumer = :costumer ")
    Page<Loan> findByBookIsbnOrCostumer(@Param("isbn") String isbn, @Param("costumer") String costumer, Pageable pageRequest);

    Page<Loan> findByBook( Book book, Pageable pageable );
}
