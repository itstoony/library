package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

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
        Book book = createValidBook();
        entityManager.persist(book);

        Loan loan = createLoan(book);
        entityManager.persist(loan);

        // execution
        boolean returned = repository.existsByBookAndNotReturned(loan.getBook());

        // verification
        assertThat(returned).isTrue();
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
                .costumer("Fulano")
                .book(book)
                .loanDate(LocalDate.now())
                .build();
    }
}
