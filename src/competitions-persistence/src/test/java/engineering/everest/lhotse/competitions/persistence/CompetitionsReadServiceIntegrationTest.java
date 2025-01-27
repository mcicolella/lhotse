package engineering.everest.lhotse.competitions.persistence;

import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.domain.CompetitionEntry;
import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.persistence.config.TestCompetitionsJpaConfig;
import engineering.everest.lhotse.competitions.services.DefaultCompetitionsReadService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD, type = POSTGRES)
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "engineering.everest.lhotse.competitions")
@ContextConfiguration(classes = { TestCompetitionsJpaConfig.class })
@Execution(SAME_THREAD)
public class CompetitionsReadServiceIntegrationTest {

    private static final UUID COMPETITION_ID_1 = randomUUID();
    private static final UUID COMPETITION_ID_2 = randomUUID();
    private static final UUID PHOTO_ID_1 = randomUUID();
    private static final UUID PHOTO_ID_2 = randomUUID();
    private static final UUID USER_ID_1 = randomUUID();
    private static final UUID USER_ID_2 = randomUUID();
    private static final String DESCRIPTION_1 = "competition 1";
    private static final String DESCRIPTION_2 = "competition 2";
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP_1 = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP_2 = Instant.ofEpochMilli(111);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP_1 = Instant.ofEpochMilli(456);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP_2 = Instant.ofEpochMilli(222);
    private static final Instant VOTING_ENDS_TIMESTAMP_1 = Instant.ofEpochMilli(789);
    private static final Instant VOTING_ENDS_TIMESTAMP_2 = Instant.ofEpochMilli(333);

    private static final Competition COMPETITION_1 = new Competition(COMPETITION_ID_1, DESCRIPTION_1, SUBMISSIONS_OPEN_TIMESTAMP_1,
        SUBMISSIONS_CLOSE_TIMESTAMP_1, VOTING_ENDS_TIMESTAMP_1, 2);
    private static final Competition COMPETITION_2 = new Competition(COMPETITION_ID_2, DESCRIPTION_2, SUBMISSIONS_OPEN_TIMESTAMP_2,
        SUBMISSIONS_CLOSE_TIMESTAMP_2, VOTING_ENDS_TIMESTAMP_2, 2);
    private static final CompetitionEntry COMPETITION_2_ENTRY_1 = new CompetitionEntry(COMPETITION_ID_2, PHOTO_ID_1, USER_ID_1,
        Instant.ofEpochMilli(108), 0, false);
    private static final CompetitionEntry COMPETITION_2_ENTRY_2 = new CompetitionEntry(COMPETITION_ID_2, PHOTO_ID_2, USER_ID_2,
        Instant.ofEpochMilli(110), 0, false);
    private static final CompetitionWithEntries COMPETITION_2_WITH_ENTRIES =
        new CompetitionWithEntries(COMPETITION_2, List.of(COMPETITION_2_ENTRY_1, COMPETITION_2_ENTRY_2));

    @Autowired
    private CompetitionsRepository competitionsRepository;
    @Autowired
    private CompetitionEntriesRepository competitionEntriesRepository;
    @Autowired
    private DefaultCompetitionsReadService competitionsReadService;

    @BeforeEach
    void setUp() {
        competitionsRepository.createCompetition(COMPETITION_ID_1, DESCRIPTION_1, SUBMISSIONS_OPEN_TIMESTAMP_1,
            SUBMISSIONS_CLOSE_TIMESTAMP_1, VOTING_ENDS_TIMESTAMP_1, 2);
        competitionsRepository.createCompetition(COMPETITION_ID_2, DESCRIPTION_2, SUBMISSIONS_OPEN_TIMESTAMP_2,
            SUBMISSIONS_CLOSE_TIMESTAMP_2, VOTING_ENDS_TIMESTAMP_2, 2);
        competitionEntriesRepository.createCompetitionEntry(COMPETITION_ID_2, PHOTO_ID_1, USER_ID_1, Instant.ofEpochMilli(108));
        competitionEntriesRepository.createCompetitionEntry(COMPETITION_ID_2, PHOTO_ID_2, USER_ID_2, Instant.ofEpochMilli(110));
    }

    @Test
    void readService_WillRetrieveCompetitionsOrderedByDescVotingEndsTimestamp() {
        assertEquals(List.of(COMPETITION_1, COMPETITION_2),
            competitionsReadService.getAllCompetitionsOrderedByDescVotingEndsTimestamp());
    }

    @Test
    void readService_WillRetrieveCompetitionsWithTheirEntries() {
        assertEquals(COMPETITION_2_WITH_ENTRIES, competitionsReadService.getCompetitionWithEntries(COMPETITION_ID_2));
    }
}
