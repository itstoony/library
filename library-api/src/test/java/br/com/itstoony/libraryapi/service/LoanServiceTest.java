package br.com.itstoony.libraryapi.service;

import br.com.itstoony.libraryapi.api.dto.LoanFilterDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.model.repository.LoanRepository;
import br.com.itstoony.libraryapi.service.imp.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;


    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Should save a loan")
    public void saveLoanTest() {
        // scenery
        Book book = createValidBook();
        book.setId(1L);

        Loan savingLoan = createLoan(book);

        Loan savedLoan = Loan.builder()
                .id(1L)
                .book(book)
                .loanDate(LocalDate.now())
                .costumer("Fulano")
                .build();

        when( repository.save(savingLoan) ).thenReturn( savedLoan );

        // execution
        Loan loan = service.save(savingLoan);

        // verification
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getCostumer()).isEqualTo(savedLoan.getCostumer());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Should throw BusinessException when trying to save an already saved loan")
    public void saveAlreadySavedLoanTest() {
        // scenery
        Loan loan = createLoan(new Book());

        BDDMockito.given( repository.existsByBookAndNotReturned(loan.getBook())).willReturn( true );
        // execution
        Throwable exception = catchThrowable(() -> service.save(loan));

        // verification
        assertThat( exception )
                        .isInstanceOf(BusinessException.class)
                        .hasMessage("Book already loaned");
        verify( repository, never() ).save(loan);

    }

    @Test
    @DisplayName("Should get a loan's details by it's ID")
    public void getLoanDetailTest() {
        // scenery
        Long id = 1L;
        Book book = createValidBook();
        Loan loan = createLoan(book);
        loan.setId(id);

        when( repository.findById(id) ).thenReturn(Optional.of(loan));

        // execution
        Optional<Loan> result = service.getById(id);

        // verification
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());
        assertThat(result.get().getCostumer()).isEqualTo(loan.getCostumer());

    }

    @Test
    @DisplayName("Should update a loan")
    public void updateLoanTest() {
        // scenery
        Long id = 1L;
        Book book = createValidBook();
        Loan loan = createLoan(book);
        loan.setId(id);
        loan.setReturned(true);

        when( repository.save(loan) ).thenReturn(loan);
        // execution
        Loan updatedLoan = service.update(loan);

        // verification
        assertThat(updatedLoan.getReturned()).isTrue();

        verify( repository ).save(loan);
    }

    @Test
    @DisplayName("Should filter loan by it's properties")
    public void findLoanTest() {
        // scenery
        LoanFilterDTO loanFilterDTO = createLoanFilterDTO();
        Book book = createValidBook();
        Loan loan = createLoan(book);
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Collections.singletonList(loan);

        Page<Loan> page = new PageImpl<>(list, pageRequest, 1);

        when( repository.findByBookIsbnOrCostumer(
                any(String.class),
                any(String.class),
                any(PageRequest.class)) )
                .thenReturn(page);

        // execution
        Page<Loan> result = service.find( loanFilterDTO, pageRequest );

        // verification
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    private static LoanFilterDTO createLoanFilterDTO() {
        return LoanFilterDTO.builder()
                .costumer("Fulano")
                .isbn("321")
                .build();
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
