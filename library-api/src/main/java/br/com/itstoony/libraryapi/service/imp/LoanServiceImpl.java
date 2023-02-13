package br.com.itstoony.libraryapi.service.imp;

import br.com.itstoony.libraryapi.api.dto.LoanFilterDTO;
import br.com.itstoony.libraryapi.api.model.entity.Book;
import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.exception.BusinessException;
import br.com.itstoony.libraryapi.model.repository.LoanRepository;
import br.com.itstoony.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if (repository.existsByBookAndNotReturned(loan.getBook())) {
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable) {
        return repository.findByBookIsbnOrCostumer(filterDTO.getIsbn(), filterDTO.getCostumer(), pageable);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }

}