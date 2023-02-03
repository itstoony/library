package br.com.itstoony.libraryapi.model.repository;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o ISBN informado")
    public void returnTrueWhenIsbnExists() {
        // scenary
        String isbn = "123";
        entityManager.persist(createValidBook());

        // execution
        boolean exists = repository.existsByIsbn(isbn);

        // verification
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o ISBN informado")
    public void returnTrueWhenIsbnDoesntExists() {
        // scenary
        String isbn = "123";

        // execution
        boolean exists = repository.existsByIsbn(isbn);

        // verification
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return an optional with a valid book by it's id")
    public void findByIdTest() {
        // scenary
        Book book = createValidBook();

        entityManager.persist(book);

        // execution
        Optional<Book> foundBook = repository.findById(book.getId());

        // verification
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

    }

    @Test
    @DisplayName("Should return an empty optional when book doesn't exist")
    public void findByIdEmptyTest() {
        // scenary
        Long id = 1L;

        // execution
        Optional<Book> foundBook = repository.findById(id);

        // validation
        assertThat(foundBook.isEmpty()).isTrue();

    }

    @Test
    @DisplayName("Should delete a book from database")
    public void deleteTest() {
        // scenary
        Book book = createValidBook();

        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        // execution
        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());

        // validation
        assertThat(deletedBook).isNull();

    }

    @Test
    @DisplayName("Should save a book in database")
    public void saveBookTest() {
        // scenary
        Book book = createValidBook();

        // execution
        Book savedBook = repository.save(book);

        // validation
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(createValidBook().getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(createValidBook().getAuthor());
        assertThat(savedBook.getIsbn()).isEqualTo(createValidBook().getIsbn());

    }

    @Test
    @DisplayName("Should find a book by it's isbn")
    public void findByIsbnTest() {
        // scenery
        String isbn = createValidBook().getIsbn();

        Book book = createValidBook();

        entityManager.persist(book);

        // execution
        Optional<Book> foundBook = repository.findByIsbn(isbn);

        // validation
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getIsbn()).isEqualTo(isbn);
    }

    @Test
    @DisplayName("Should not find a book with invalid isbn")
    public void findByInvalidIsbnTest() {
        // scenery
        String isbn = createValidBook().getIsbn();

        // execution
        Optional<Book> foundBook = repository.findByIsbn(isbn);

        // validation
        assertThat( foundBook.isEmpty()).isTrue();
    }


    private static Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build();
    }
}
