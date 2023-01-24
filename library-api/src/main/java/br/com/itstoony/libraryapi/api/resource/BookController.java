package br.com.itstoony.libraryapi.api.resource;

import br.com.itstoony.libraryapi.api.dto.BookDTO;
import br.com.itstoony.libraryapi.api.exception.ApiErrors;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.service.BookService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/api/books")
public class BookController {

    private final ModelMapper modelMapper;

    private final BookService bookService;

    private BookController(BookService bookService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {

        Book entity = modelMapper.map( dto, Book.class );

        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessExceptions(BusinessException ex) {
        return new ApiErrors(ex);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id) {
        return bookService.getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, @RequestBody BookDTO dto) {
        return bookService.getById(id).map( book -> {

            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());

            book = bookService.update(book);

            return modelMapper.map(book, BookDTO.class);

        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
