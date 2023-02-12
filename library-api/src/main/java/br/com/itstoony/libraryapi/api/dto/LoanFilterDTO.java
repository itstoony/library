package br.com.itstoony.libraryapi.api.dto;

import lombok.Data;

@Data
public class LoanFilterDTO {

    private String isbn;
    private String costumer;

}
