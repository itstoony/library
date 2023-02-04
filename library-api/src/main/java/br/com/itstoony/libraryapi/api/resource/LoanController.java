package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.LoanDTO;
import br.com.itstoony.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.service.BookService;
import br.com.itstoony.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow( () ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan loan = Loan.builder()
                .costumer(dto.getCostumer())
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        loan = service.save(loan);

        return loan.getId();
    }

    @PatchMapping("{id}")
    public ReturnedLoanDTO returnBook( @PathVariable Long id, @RequestBody ReturnedLoanDTO dto ) {
        Loan loan = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan not found"));
        loan.setReturned(true);
        loan = service.update(loan);

        return ReturnedLoanDTO.builder().returned(loan.getReturned()).build();
    }
}
