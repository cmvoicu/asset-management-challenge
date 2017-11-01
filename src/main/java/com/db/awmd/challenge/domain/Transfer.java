package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transfer {

  @NotNull
  @NotEmpty
  private final String sourceAccountId;
  @NotNull
  @NotEmpty
  private final String destinationAccountId;
  @NotNull
  @Min(value = 0, message = "Amount to must be positive.")
  private final BigDecimal amount;

  @JsonCreator
  public Transfer(@JsonProperty("sourceAccountId") String sourceAccountId,
                  @JsonProperty("destinationAccountId") String destinationAccountId,
                  @JsonProperty("amount") BigDecimal amount) {

    this.sourceAccountId = sourceAccountId;
    this.destinationAccountId = destinationAccountId;
    this.amount = amount;
  }
}