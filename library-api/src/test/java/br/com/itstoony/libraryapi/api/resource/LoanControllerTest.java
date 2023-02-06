package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.LoanDTO;
import br.com.itstoony.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.service.BookService;
import br.com.itstoony.libraryapi.service.LoanService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Should create a loan")
    public void createLoanTest() throws Exception {
        // scenery
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .costumer("Manoel Gomes")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);
        Loan loan = Loan.builder()
                .id(1L)
                .costumer("Manoel Gomes")
                .book(createValidBook())
                .loanDate(LocalDate.now())
                .build();

        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(createValidBook()));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( content().string("1"));
    }

    @Test
    @DisplayName("Should throw an exception when trying to loan an invalid book")
    public void invalidBookLoanTest() throws Exception {
        // scenery
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .costumer("Manoel Gomes")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given( bookService.getBookByIsbn(dto.getIsbn()) ).willReturn( Optional.empty() );

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Should throw an exception when trying to loan a loaned book")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        // scenery
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .costumer("Manoel Gomes")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = createValidBook();

        BDDMockito.given( bookService.getBookByIsbn(dto.getIsbn()) ).willReturn( Optional.of(book) );
        BDDMockito.given( loanService.save(Mockito.any(Loan.class))).willThrow( new BusinessException("Book already loaned") );

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value("Book already loaned"));

    }

    @Test
    @DisplayName("Should return a book")
    public void returnBookTest() throws Exception {
        // scenery
        ReturnedLoanDTO returned = ReturnedLoanDTO.builder().returned(true).build();
        Long id = 1L;
        Loan loan = createValidLoan(createValidBook());
        loan.setId(id);

        String json = new ObjectMapper().writeValueAsString(returned);

        BDDMockito.given( loanService.getById(id) ).willReturn(Optional.of(loan));
        BDDMockito.given( loanService.update(Mockito.any(Loan.class)) ).willReturn(loan);

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("returned").value("true"));

        verify( loanService, Mockito.times(1) ).update(loan);
        verify( loanService, Mockito.times(1) ).getById(id);

    }

    @Test
    @DisplayName("Should return 404 when id passed is invalid")
    public void returnAlreadyReturnedBookTest() throws Exception {
        // scenery
        ReturnedLoanDTO returned = ReturnedLoanDTO.builder().returned(true).build();
        Long id = 1L;
        Loan loan = createValidLoan(createValidBook());
        loan.setId(id);

        String json = new ObjectMapper().writeValueAsString(returned);

        BDDMockito.given( loanService.getById(id) ).willReturn(Optional.of(loan));
        BDDMockito.given( loanService.update(Mockito.any(Loan.class)) ).willReturn(loan);

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("returned").value("true"));

    }

    @Test
    @DisplayName("Should throw error when trying to return invalid loan")
    public void returnInvalidLoanTest() throws Exception {
        // scenery
        ReturnedLoanDTO returned = ReturnedLoanDTO.builder().returned(true).build();
        Long id = 1L;
        Loan loan = createValidLoan(createValidBook());
        loan.setId(id);

        String json = new ObjectMapper().writeValueAsString(returned);

        BDDMockito.given( loanService.getById(id) ).willReturn(Optional.empty());

        // execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // verification
        mvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }

    private Loan createValidLoan(Book book) {
        return Loan.builder()
                .book(book)
                .costumer("Fulano")
                .build();
    }

    public static Book createValidBook() {
        return Book.builder().id(1L).isbn("123").build();
    }

}
