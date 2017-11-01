package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest {

    @MockBean
    private NotificationService notificationService;
    @Autowired
    private AccountsService accountsService;
    @Autowired
    private TransferService transferService;

    @Before
    public void setUp() throws Exception {
        accountsService.getAccountsRepository().clearAccounts();
        Account source = new Account("Id-source", BigDecimal.TEN);
        Account destination = new Account("Id-destination", BigDecimal.ONE);
        accountsService.createAccount(source);
        accountsService.createAccount(destination);
    }

    @Test
    public void executeTransfer() throws Exception {
        Transfer transfer = new Transfer("Id-source", "Id-destination", BigDecimal.valueOf(5));
        transferService.executeTransfer(transfer);
        Account source = accountsService.getAccount("Id-source");
        Account destination = accountsService.getAccount("Id-destination");


        assertThat(source.getBalance()).isEqualByComparingTo("5");
        assertThat(destination.getBalance()).isEqualByComparingTo("6");
        verify(notificationService).notifyAboutTransfer(eq(source),anyString());
        verify(notificationService).notifyAboutTransfer(eq(destination),anyString());
    }

    @Test
    public void insufficientFunds() throws Exception {
        Transfer transfer = new Transfer("Id-source", "Id-destination", BigDecimal.valueOf(10.5));
        Account source = accountsService.getAccount("Id-source");
        try {
            transferService.executeTransfer(transfer);
            fail("Should have failed on insufficient funds");
        } catch (InsufficientFundsException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient funds " + source.getBalance() +
                    " on account " + source.getAccountId());
        }
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void accountNotFound() throws Exception {
        final String nonExistentId = "Id-NON-EXISTENT";
        Transfer transfer = new Transfer("Id-source", nonExistentId, BigDecimal.ONE);

        try {
            transferService.executeTransfer(transfer);
            fail("Should have failed when account does NOT exits");
        } catch (AccountNotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + nonExistentId + " does NOT exists!");
        }
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void getFirstLock() throws Exception {
        Account firstLocked = new Account("Id1", BigDecimal.TEN);
        Account secondLocked = new Account("Id2", BigDecimal.ONE);

        Account result = transferService.getFirstLock(firstLocked, secondLocked);
        assertEquals(result, firstLocked);

        result = transferService.getFirstLock(secondLocked, firstLocked);
        assertEquals(result,firstLocked);
    }

    @Test
    public void getSecondLock() throws Exception {
        Account firstLocked = new Account("Id1", BigDecimal.TEN);
        Account secondLocked = new Account("Id2", BigDecimal.ONE);

        Account result = transferService.getSecondLock(firstLocked, secondLocked);
        assertEquals(result, secondLocked);

        result = transferService.getSecondLock(secondLocked, firstLocked);
        assertEquals(result,secondLocked);
    }
}