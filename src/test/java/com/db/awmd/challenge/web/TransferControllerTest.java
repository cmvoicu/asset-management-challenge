package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransferControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;
    @Autowired
    private AccountsService accountsService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        accountsService.getAccountsRepository().clearAccounts();
        Account source = new Account("Id-source", BigDecimal.TEN);
        Account destination = new Account("Id-destination", BigDecimal.ZERO);
        accountsService.createAccount(source);
        accountsService.createAccount(destination);
    }

    @Test
    public void executeTransfer() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                         "\"destinationAccountId\":\"Id-destination\"," +
                         "\"amount\":3}"))
                .andExpect(status().isOk());

        Account source = accountsService.getAccount("Id-source");
        Account destination = accountsService.getAccount("Id-destination");
        assertThat(source.getBalance()).isEqualByComparingTo("7");
        assertThat(destination.getBalance()).isEqualByComparingTo("3");
    }

    @Test
    public void transferEmptyAccount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                        "\"destinationAccountId\":\"Id-destination\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferEmptyAmmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"\"," +
                        "\"destinationAccountId\":\"Id-destination\"," +
                        "\"amount\":-0.01}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferNegativeAmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                        "\"destinationAccountId\":\"Id-destination\"," +
                        "\"amount\":-0.01}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferZeroAmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                        "\"destinationAccountId\":\"Id-destination\"," +
                        "\"amount\":0.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferIntoSameAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                        "\"destinationAccountId\":\"Id-source\"," +
                        "\"amount\":0.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void accountNotFound() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-NON-EXISTING\"," +
                        "\"destinationAccountId\":\"Id-destination\"," +
                        "\"amount\":11}"))
                .andExpect(status().isBadRequest());
        Account source = accountsService.getAccount("Id-source");
        Account destination = accountsService.getAccount("Id-destination");
        assertThat(source.getBalance()).isEqualByComparingTo("10");
        assertThat(destination.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    public void insufficientFundsTransfer() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceAccountId\":\"Id-source\"," +
                        "\"destinationAccountId\":\"Id-destination\"," +
                        "\"amount\":11}"))
                .andExpect(status().isBadRequest());
        Account source = accountsService.getAccount("Id-source");
        Account destination = accountsService.getAccount("Id-destination");
        assertThat(source.getBalance()).isEqualByComparingTo("10");
        assertThat(destination.getBalance()).isEqualByComparingTo("0");
    }
}