package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedAndWinnersDeclaredEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEnteredInCompetitionEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEntryReceivedVoteEvent;
import engineering.everest.lhotse.competitions.domain.events.WinnerAndSubmittedPhotoPair;
import engineering.everest.lhotse.competitions.domain.queries.CompetitionWithEntriesQuery;
import engineering.everest.lhotse.competitions.persistence.CompetitionEntriesRepository;
import engineering.everest.lhotse.competitions.persistence.CompetitionEntryId;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import engineering.everest.lhotse.competitions.persistence.PersistableCompetitionEntry;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionsEventHandlerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);
    private static final Instant ENTRY_TIMESTAMP = Instant.ofEpochMilli(1234);

    private CompetitionsEventHandler competitionsEventHandler;

    @Mock
    private QueryUpdateEmitter queryUpdateEmitter;
    @Mock
    private CompetitionsReadService competitionsReadService;
    @Mock
    private CompetitionsRepository competitionsRepository;
    @Mock
    private CompetitionEntriesRepository competitionEntriesRepository;

    @BeforeEach
    void setUp() {
        competitionsEventHandler = new CompetitionsEventHandler(queryUpdateEmitter, competitionsReadService,
            competitionsRepository, competitionEntriesRepository);
    }

    @Test
    void prepareForReplay_WillClearProjection() {
        competitionsEventHandler.prepareForReplay();

        verify(competitionsRepository).deleteAll();
    }

    @Test
    void onCompetitionCreatedEvent_WillProject() {
        competitionsEventHandler.on(new CompetitionCreatedEvent(USER_ID, COMPETITION_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2));

        verify(competitionsRepository).createCompetition(COMPETITION_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);
    }

    @Test
    void onPhotoEnteredInCompetitionEvent_WillProject() {
        competitionsEventHandler.on(
            new PhotoEnteredInCompetitionEvent(COMPETITION_ID, PHOTO_ID, USER_ID, USER_ID, "notes"), ENTRY_TIMESTAMP);

        verify(competitionEntriesRepository).createCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);
    }

    @Test
    void onPhotoEnteredInCompetitionEvent_WillEmitQueryUpdate() {
        var expectedCompetitionWithEntries = mock(CompetitionWithEntries.class);
        when(competitionsReadService.getCompetitionWithEntries(COMPETITION_ID)).thenReturn(expectedCompetitionWithEntries);

        competitionsEventHandler.on(
            new PhotoEnteredInCompetitionEvent(COMPETITION_ID, PHOTO_ID, USER_ID, USER_ID, "notes"), ENTRY_TIMESTAMP);

        verify(queryUpdateEmitter).emit(eq(CompetitionWithEntriesQuery.class), any(), eq(expectedCompetitionWithEntries));
    }

    @Test
    void onPhotoEntryReceivedVoteEvent_WillProject() {
        var persistableCompetitionEntry = new PersistableCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);
        when(competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID))).thenReturn(
            Optional.of(persistableCompetitionEntry));

        competitionsEventHandler.on(new PhotoEntryReceivedVoteEvent(COMPETITION_ID, PHOTO_ID, USER_ID));
        competitionsEventHandler.on(new PhotoEntryReceivedVoteEvent(COMPETITION_ID, PHOTO_ID, USER_ID));

        assertEquals(2, persistableCompetitionEntry.getVotesReceived());
    }

    @Test
    void onPhotoEntryReceivedVoteEvent_WillEmitQueryUpdate() {
        var persistableCompetitionEntry = new PersistableCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);
        when(competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID)))
            .thenReturn(Optional.of(persistableCompetitionEntry));
        var expectedCompetitionWithEntries = mock(CompetitionWithEntries.class);
        when(competitionsReadService.getCompetitionWithEntries(COMPETITION_ID)).thenReturn(expectedCompetitionWithEntries);

        competitionsEventHandler.on(new PhotoEntryReceivedVoteEvent(COMPETITION_ID, PHOTO_ID, USER_ID));

        verify(queryUpdateEmitter).emit(eq(CompetitionWithEntriesQuery.class), any(), eq(expectedCompetitionWithEntries));
    }

    @Test
    void onCompetitionWinnersDeclared_WillProject() {
        var persistableCompetitionEntry = new PersistableCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);
        when(competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID)))
            .thenReturn(Optional.of(persistableCompetitionEntry));

        competitionsEventHandler.on(
            new CompetitionEndedAndWinnersDeclaredEvent(COMPETITION_ID, List.of(new WinnerAndSubmittedPhotoPair(USER_ID, PHOTO_ID)), 1));

        assertTrue(persistableCompetitionEntry.isWinner());
    }

    @Test
    void onCompetitionWinnersDeclared_WillEmitQueryUpdate() {
        var persistableCompetitionEntry = new PersistableCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);
        when(competitionEntriesRepository.findById(eq(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID))))
            .thenReturn(Optional.of(persistableCompetitionEntry));
        var expectedCompetitionWithEntries = mock(CompetitionWithEntries.class);
        when(competitionsReadService.getCompetitionWithEntries(COMPETITION_ID)).thenReturn(expectedCompetitionWithEntries);

        competitionsEventHandler.on(
            new CompetitionEndedAndWinnersDeclaredEvent(COMPETITION_ID, List.of(new WinnerAndSubmittedPhotoPair(USER_ID, PHOTO_ID)), 1));

        verify(queryUpdateEmitter).emit(eq(CompetitionWithEntriesQuery.class), any(), eq(expectedCompetitionWithEntries));
    }
}
