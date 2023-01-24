package br.com.itstoony.libraryapi.service;

import br.com.itstoony.libraryapi.api.model.entity.Book;

import java.util.Optional;


public interface BookService {

    Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

}
