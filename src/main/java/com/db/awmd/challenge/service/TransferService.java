package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class TransferService {

    private final AccountsService accountsService;
    private final NotificationService notificationService;

    @Autowired
    public TransferService(AccountsService accountsService, NotificationService notificationService) {
        this.accountsService = accountsService;
        this.notificationService = notificationService;
    }


    public void executeTransfer(Transfer transfer) {
        BigDecimal amount = transfer.getAmount();
        Account sourceAccount = getExistingAccount(transfer.getSourceAccountId());
        Account destinationAccount = getExistingAccount(transfer.getDestinationAccountId());
        synchronized (getFirstLock(sourceAccount, destinationAccount)) {
            synchronized (getSecondLock(sourceAccount, destinationAccount)) {
                BigDecimal sourceBalance = sourceAccount.getBalance();
                if (sourceBalance.compareTo(amount) < 0) {
                    throw new InsufficientFundsException("Insufficient funds " + sourceBalance +
                            " on account " + sourceAccount.getAccountId());
                }
                BigDecimal destinationBalance = destinationAccount.getBalance();
                sourceAccount.setBalance(sourceBalance.subtract(amount));
                destinationAccount.setBalance(destinationBalance.add(amount));
            }
        }
        notificationService.notifyAboutTransfer(sourceAccount,
                "Transferred " + amount + " to" + destinationAccount.getAccountId());
        notificationService.notifyAboutTransfer(destinationAccount,
                "Received " + amount + "from " + sourceAccount.getAccountId());
    }

    private Account getExistingAccount(String accountId) {
        Account account = accountsService.getAccount(accountId);
        //TODO: refactor AccountsService.getAccount to return Optional
        if (accountNotFound(account)) {
            throw new AccountNotFoundException(
                    "Account id " + accountId + " does NOT exists!");
        }
        return account;
    }

    private boolean accountNotFound(Account account) {
        return !Optional.ofNullable(account).isPresent();
    }

    Account getFirstLock(Account account, Account otherAccount) {
        if (account.getAccountId().compareTo(otherAccount.getAccountId()) < 0) {
            return account;
        } else {
            return otherAccount;
        }
    }

    Account getSecondLock(Account account, Account otherAccount) {
        if (account.getAccountId().compareTo(otherAccount.getAccountId()) < 0) {
            return otherAccount;
        } else {
            return account;
        }
    }

    private boolean isTransactionValid(Transfer transfer) {
        //TODO:was moved to Controller validator, but can be double checked here
        if (BigDecimal.ZERO.equals(transfer.getAmount())) {
            log.warn("Amount to transfer is zero");
            return false;
        }
        if (transfer.getSourceAccountId().equals(transfer.getDestinationAccountId())) {
            log.warn("Transfer to the same account");
            return false;
        }
        return true;
    }
}