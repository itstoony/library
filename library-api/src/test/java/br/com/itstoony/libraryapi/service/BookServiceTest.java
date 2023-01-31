package br.com.itstoony.libraryapi.service;

import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.model.repository.BookRepository;
import br.com.itstoony.libraryapi.service.imp.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test ")
@DataJpaTest
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("should save a book")
    public void saveBookTest() {
        // scenary
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(
                Book.builder().id(1L)
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build());

        // execution
        var savedBook = service.save(book);

        // verification
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    @Test
    @DisplayName("Deve lançar exceção caso tente cadastrar um livro com ISBN duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        // scenary
        var book = createValidBook();
        String mensagemErro = "Isbn já cadastrado";
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execution
        var exception = catchThrowable(() -> service.save(book));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage(mensagemErro);

        verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Should get a book by id")
    public void getByIdTest() {
        // scenary
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        // execution
        Optional<Book> foundBook = service.getById(id);

        // verification
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

    }

    @Test
    @DisplayName("Should return an empty optional of book when trying to findById with an invalid id ")
    public void getByIdEmptyTest() {
        // scenary
        Long id = 1L;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // execution
        Optional<Book> foundBook = service.getById(id);

        // verification
        assertThat(foundBook.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Should delete a book from database")
    public void deleteTest() {
        // scenary
        Book book = Book.builder().id(1L).build();

        // execution
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(book));

        // verification
        verify(repository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Should throw an exception when trying to delete an unsaved book")
    public void deleteUnsavedBookTest() {
        // scenary
        Book book = createValidBook();
        String erro = "Can't delete an unsaved book";

        // execution
        Throwable ex = catchThrowable( () -> service.delete(book));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex).hasMessage(erro);
        verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Should update a book")
    public void updateBookTest() {
        // scenary
        Long id = 1L;
        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        // execution
        Book book = service.update(updatingBook);

        // verification
        verify(repository, Mockito.times(1)).save(updatingBook);
        assertThat(book.getId()).isEqualTo(id);
        assertThat(book.getIsbn()).isEqualTo(createValidBook().getIsbn());
        assertThat(book.getAuthor()).isEqualTo(createValidBook().getAuthor());
        assertThat(book.getTitle()).isEqualTo(createValidBook().getTitle());

    }

    @Test
    @DisplayName("Should throw an exception when trying to update an unsaved book")
    public void updateUnsavedBookTest() {
        // scenary
        Book book = createValidBook();
        String erro = "Can't update an unsaved book";

        // execution
        verify(repository, Mockito.never()).save(book);
        Throwable ex = catchThrowable( () -> service.update(book));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex).hasMessage(erro);
    }

    private static Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build();
    }
}
