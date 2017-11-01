package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.service.TransferService;
import com.db.awmd.challenge.validator.TransferValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class TransferController {

  private final TransferService transferService;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.addValidators(new TransferValidator());
  }

  @Autowired
  public TransferController(TransferService transferService) {
    this.transferService = transferService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> executeTransfer(@RequestBody @Validated Transfer transfer) {
    log.info("Performing transfer {}", transfer);

    try {
      this.transferService.executeTransfer(transfer);
    } catch (AccountNotFoundException|InsufficientFundsException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

}