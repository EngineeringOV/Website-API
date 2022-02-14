package ventures.of.api;


    /*
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import ventures.of.api.testing.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class dsTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private WebApplicationContext webContext;

    private TransactionTemplate transaction;

    private MockMvc mockMvc;
    @Before
    public void setup() {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> entityRelationRepository.deleteAll());
        transaction.executeWithoutResult(status -> legalEntityRepository.deleteAll());

        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    /*
     * Run multiple parallel requests in their own transactions in order to simulate high load.
     */
    /*
    @Test
    public void testMultiProcessIndividualFetch() throws Exception {
        // Given
        String ssn = "198001013872";

        when(checkBizClient.personInformation(ssn))
                .thenAnswer(answer(TestUtil.readResource("example/checkbiz_198001013872_emigrated.json", PersonAddressModel.class), 1000L));

        for (int i = 0; i < 10; i++) { // Run the tests multiple times
            transaction.executeWithoutResult(status -> {
                entityRelationRepository.deleteAll();
                legalEntityRepository.deleteAll();
            });

            // When
            List<ForkJoinTask<String>> results = new ArrayList<>();
            ForkJoinPool pool = new ForkJoinPool(20);
            for (int j = 0; j < 40; j++) { // Trigger many requests
                results.add(pool.submit(() -> mockMvc.perform(get("/rest/individual/" + ssn)).andReturn().getResponse().getContentAsString(UTF_8)));
            }

            // Then
            results.forEach(r -> {
                try {
                    String response = r.join();
                    IndividualSearchByNameOrAddressModel details = objectMapper.readValue(response, IndividualSearchByNameOrAddressModel.class);
                    assertEquals(ssn, details.getSsn());
                    assertNotNull(details);
                } catch (Exception e) {
                    fail("Exception: " + e.getMessage());
                }
            });
            assertEquals(40, results.size());
        }
    }

        private <T> Answer<T> answer(T response, long delay) {
            return new Answer<T>() {
                @Override
                public T answer(InvocationOnMock invocation) throws Throwable {
//                Thread.sleep(delay);
                    return response;
                }
            };
        }

    }
*/