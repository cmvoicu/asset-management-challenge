package com.db.awmd.challenge.validator;


import com.db.awmd.challenge.domain.Transfer;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class TransferValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Transfer.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Transfer transfer = (Transfer) target;

        if(errors.hasErrors()){
            return;
        }
        if(transfer.getAmount().compareTo(BigDecimal.ZERO)<=0) {
            errors.rejectValue("amount", "Amount should be strictly greater than zero");
        }
        if(transfer.getSourceAccountId().equals(transfer.getDestinationAccountId())) {
            errors.rejectValue("destinationAccountId", "Destination account should be different than the source one");
        }
    }
}