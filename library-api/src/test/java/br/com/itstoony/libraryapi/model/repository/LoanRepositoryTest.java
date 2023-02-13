package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    @Test
    @DisplayName("Should return true when loan exists in database")
    public void existsByBookAndNotReturnedTest() {
        // scenery
        Loan loan = createAndPersistLoanAndBook(LocalDate.now());

        // execution
        boolean returned = repository.existsByBookAndNotReturned(loan.getBook());

        // verification
        assertThat(returned).isTrue();
    }

    @Test
    @DisplayName("Should find loan by book's isbn or costumer")
    public void findByBookIsbnOrCostumerTest() {
        // scenery
        Loan loan = createAndPersistLoanAndBook(LocalDate.now());

        // execution
        Page<Loan> result = repository.findByBookIsbnOrCostumer("123", "Fulano", PageRequest.of(0, 10));

        // verification
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Should get loans where loan date is less ir equal to three days ago and not returned")
    public void findByLoanDateLessThanAndNotReturnedTest() {
        // scenery
        Loan loan = createAndPersistLoanAndBook(LocalDate.now().minusDays(5));

        // execution
        List<Loan> result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        // verification
        assertThat( result ).hasSize(1).contains(loan);

    }

    @Test
    @DisplayName("Should return empty when there are no late loans")
    public void notFoundByLoanDateLessThanAndNotReturnedTest() {
        // scenery
        Loan loan = createAndPersistLoanAndBook( LocalDate.now() );

        // execution
        List<Loan> result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        // verification
        assertThat( result ).isEmpty();

    }

    private static Book createValidBook() {
        return Book.builder()
                .author("Arthur")
                .title("As aventuras")
                .isbn("123")
                .build();
    }

    private static Loan createLoan(Book book) {
        return Loan.builder()
                .customer("Fulano")
                .book(book)
                .loanDate(LocalDate.now())
                .build();
    }

    private Loan createAndPersistLoanAndBook(LocalDate loanDate) {
        Book book = createValidBook();
        entityManager.persist(book);

        Loan loan = createLoan(book);
        loan.setLoanDate(loanDate);
        entityManager.persist(loan);

        return loan;
    }
}
