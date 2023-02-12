package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.BookDTO;
import br.com.itstoony.libraryapi.api.dto.LoanDTO;
import br.com.itstoony.libraryapi.api.dto.LoanFilterDTO;
import br.com.itstoony.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.service.BookService;
import br.com.itstoony.libraryapi.service.LoanService;
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
public class LoanController {

    private final LoanService service;

    private final BookService bookService;

    private final ModelMapper modelMapper;

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
    public ReturnedLoanDTO returnBook( @PathVariable Long id ) {
        Loan loan = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        loan.setReturned(true);
        loan = service.update(loan);

        return ReturnedLoanDTO.builder().returned(loan.getReturned()).build();
    }

    @GetMapping
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
