package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.BookDTO;
import br.com.itstoony.libraryapi.api.dto.LoanDTO;
import br.com.itstoony.libraryapi.api.dto.LoanFilterDTO;
import br.com.itstoony.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.service.BookService;
import br.com.itstoony.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "API responsible for maintaining book loans.")
public class LoanController {

    private final LoanService service;

    private final BookService bookService;

    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "borrow a book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful loan."),
            @ApiResponse(responseCode = "400", description = "Failure to take out the loan.")
    })
    public Long create(@RequestBody LoanDTO dto) {
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow( () ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan loan = Loan.builder()
                .customer(dto.getCustomer())
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        loan = service.save(loan);

        return loan.getId();
    }

    @PatchMapping("{id}")
    @Operation(summary = "Return a book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan details successfully obtained."),
            @ApiResponse(responseCode = "400", description = "Failed to get loan details.")
    })
    public ReturnedLoanDTO returnBook( @PathVariable Long id ) {
        Loan loan = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        loan.setReturned(true);
        loan = service.update(loan);

        return ReturnedLoanDTO.builder().returned(loan.getReturned()).build();
    }

    @GetMapping
    @Operation(summary = "Get loans with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Find loans by parameters successfully obtained."),
            @ApiResponse(responseCode = "400", description = "Failed to find loans by parameters.")
    })
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {

        Page<Loan> result = service.find(dto, pageRequest);

        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO =  modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
                .toList();

        return new PageImpl<>(loans, pageRequest, result.getTotalElements());

    }
}
