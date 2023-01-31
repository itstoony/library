package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.BookDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBookDTO();

        var savedBook = Book.builder()
                .id(10L).author("Arthur")
                .title("As aventuras")
                .isbn("001")
                .build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        var request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(10L))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar isbn já utilizado por outro")
    public void createBookWithDuplicatedIsbn() throws Exception {

        var dto = createNewBookDTO();

        String json = new ObjectMapper().writeValueAsString(dto);

        String mensagemErro = "Isbn já cadastrado";

        BDDMockito
                .given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));

    }

    @Test
    @DisplayName("Should get book's details")
    public void getBookDetailsTest() throws Exception {
        // scenary
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title(createNewBookDTO().getTitle())
                .author(createNewBookDTO().getAuthor())
                .isbn(createNewBookDTO().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBookDTO().getTitle()))
                .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBookDTO().getIsbn()));

    }

    @Test
    @DisplayName("Should throw exception when book doesn't exist in database")
    public void bookNotFoundTest() throws Exception {
        // scenary
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1L))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Should delete a book")
    public void deleteBookTest() throws Exception {
        // scenary
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1L));

        mvc
                .perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Should throw an exception when book doesn't exist in database")
    public void deleteBookNotFoundTest() throws Exception {
        // scenary
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1L));

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Should update a book")
    public void updateBookTest() throws Exception {
        // scenary
        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        Book updatingBook = Book.builder()
                .id(id)
                .author("some author")
                .title("some title")
                .isbn("321")
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
        BDDMockito.given(service.update(updatingBook)).willReturn(updatingBook);

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBookDTO().getTitle()))
                .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
                .andExpect(jsonPath("isbn").value("321"));

    }

    @Test
    @DisplayName("Should return 404 when updating book doesn't exist")
    public void updateBookNotFound() throws Exception {
        // scenary
        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);


        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Should filter books")
    public void findBooksTest() throws Exception {
        // scenary
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<>(Collections.singletonList(book), Pageable.ofSize(100), 1) );

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1))
                .andExpect( jsonPath("pageable.pageSize").value(100))
                .andExpect( jsonPath("pageable.pageNumber").value(0));


    }

    private static Book createValidBook() {
        return Book.builder()
                .author("Arthur")
                .title("As aventuras")
                .isbn("123")
                .build();
    }

    private static BookDTO createNewBookDTO() {
        return BookDTO.builder()
                .author("Arthur")
                .title("As aventuras")
                .isbn("001")
                .build();
    }


}
