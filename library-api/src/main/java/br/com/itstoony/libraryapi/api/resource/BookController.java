package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.BookDTO;
import br.com.itstoony.libraryapi.api.dto.LoanDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.service.BookService;
import br.com.itstoony.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "API responsible for book management")
@Slf4j
public class BookController {

    private final ModelMapper modelMapper;

    private final BookService bookService;

    private final LoanService loanService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully."),
            @ApiResponse(responseCode = "400", description = "Failed to create book.")
    })
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info(" creating a book for isbn: {}", dto.getIsbn());

        Book entity = modelMapper.map( dto, Book.class );

        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);

    }

    @GetMapping("{id}")
    @Operation(summary = "Get details of a book by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book details successfully obtained."),
            @ApiResponse(responseCode = "400", description = "Failed to get book details.")
    })
    public BookDTO get(@PathVariable Long id) {
        log.info(" obtaining details for book id: {}", id);
        return bookService.getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted book."),
            @ApiResponse(responseCode = "400", description = "failed to delete book.")
    })
    public void delete(@PathVariable Long id) {
        log.info(" deleting book of id: {}", id);
        Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update a book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book successfully updated."),
            @ApiResponse(responseCode = "400", description = "Failed to update book.")
    })
    public BookDTO update(@PathVariable Long id, @RequestBody BookDTO dto) {
        log.info(" updating book of id: {}", id);
        return bookService.getById(id).map( book -> {

            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());

            book = bookService.update(book);

            return modelMapper.map(book, BookDTO.class);

        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @Operation(summary = "Find books by params.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found books by parameters successfully obtained."),
            @ApiResponse(responseCode = "400", description = "Failed to find books by parameters.")
    })
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);

        Page<Book> result = bookService.find(filter, pageRequest);

        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .toList();

        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @Operation(summary = "Search loans by book id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book loan search successfully obtained."),
            @ApiResponse(responseCode = "400", description = "Failure to get book loans.")
    })
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> dtoList = result
                .getContent()
                .stream()
                .map( loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                } )
                .toList();

        return new PageImpl<>(dtoList, pageable, result.getTotalElements());
    }
}
