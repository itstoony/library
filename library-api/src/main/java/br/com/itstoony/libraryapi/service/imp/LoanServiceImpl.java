package br.com.itstoony.libraryapi.service.imp;

import br.com.itstoony.libraryapi.api.model.entity.Loan;
import br.com.itstoony.libraryapi.model.repository.LoanRepository;
import br.com.itstoony.libraryapi.service.LoanService;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        return repository.save(loan);
    }

}