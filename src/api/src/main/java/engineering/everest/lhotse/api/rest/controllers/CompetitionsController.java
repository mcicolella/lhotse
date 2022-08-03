package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import engineering.everest.lhotse.api.rest.annotations.RegisteredUser;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.CompetitionSubmissionRequest;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.api.rest.responses.CompetitionSummaryResponse;
import engineering.everest.lhotse.api.rest.responses.CompetitionWithEntriesResponse;
import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.domain.queries.CompetitionWithEntriesQuery;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import engineering.everest.lhotse.competitions.services.CompetitionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@RestController
@RequestMapping("/api/competitions")
@Api(tags = "Competitions")
public class CompetitionsController {

    private final DtoConverter dtoConverter;
    private final CompetitionsService competitionsService;
    private final CompetitionsReadService competitionsReadService;
    private final QueryGateway queryGateway;

    public CompetitionsController(DtoConverter dtoConverter,
                                  CompetitionsService competitionsService,
                                  CompetitionsReadService competitionsReadService,
                                  QueryGateway queryGateway) {
        this.dtoConverter = dtoConverter;
        this.competitionsService = competitionsService;
        this.competitionsReadService = competitionsReadService;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @ApiOperation("Create a new competition to run")
    @AdminOnly
    public UUID createCompetition(@ApiIgnore Principal principal, @RequestBody CreateCompetitionRequest request) {
        return competitionsService.createCompetition(UUID.fromString(principal.getName()), request.getDescription(),
            request.getSubmissionsOpenTimestamp(), request.getSubmissionsCloseTimestamp(), request.getVotingEndsTimestamp(),
            request.getMaxEntriesPerUser());
    }

    @GetMapping
    @ResponseStatus(OK)
    @ApiOperation("Retrieve a summary of all competitions")
    @AdminOrRegisteredUser
    public List<CompetitionSummaryResponse> getSummaryOfAllCompetitions(@ApiIgnore Principal principal) {
        return competitionsReadService.getAllCompetitionsOrderedByDescVotingEndsTimestamp().stream()
            .map(dtoConverter::convert)
            .collect(toList());
    }

    @PostMapping("/{competitionId}/photos")
    @ResponseStatus(CREATED)
    @ApiOperation("Submit a photo to the competition")
    @RegisteredUser
    public void submitPhotoToCompetition(@ApiIgnore Principal principal,
                                         @PathVariable UUID competitionId,
                                         @RequestBody CompetitionSubmissionRequest request) {
        competitionsService.submitPhoto(UUID.fromString(principal.getName()), competitionId, request.getPhotoId(),
            request.getSubmissionNotes());
    }

    @GetMapping(path = "/{competitionId}")
    @ResponseStatus(OK)
    @ApiOperation("Full details for a single competition")
    @AdminOrRegisteredUser
    public CompetitionWithEntriesResponse getCompetition(@ApiIgnore Principal principal, @PathVariable UUID competitionId) {
        return dtoConverter.convert(competitionsReadService.getCompetitionWithEntries(competitionId));
    }

    @GetMapping(path = "/{competitionId}", produces = APPLICATION_NDJSON_VALUE)
    @ResponseStatus(OK)
    @ApiOperation("Retrieve a competition and subscribe to updates")
    @AdminOrRegisteredUser
    public Flux<CompetitionWithEntriesResponse> getCompetitionWithEntriesUpdates(@ApiIgnore Principal principal,
                                                                                 @PathVariable UUID competitionId) {
        var subscriptionQueryResult = queryGateway.subscriptionQuery(
            new CompetitionWithEntriesQuery(competitionId), CompetitionWithEntries.class, CompetitionWithEntries.class);

        return subscriptionQueryResult.updates()
            .mergeWith(subscriptionQueryResult.initialResult())
            .map(dtoConverter::convert);
    }

    @PostMapping("/{competitionId}/photos/{photoId}/vote")
    @ResponseStatus(CREATED)
    @ApiOperation("Vote for an entry in a competition")
    @RegisteredUser
    public void voteForCompetitionEntry(@ApiIgnore Principal principal,
                                        @PathVariable UUID competitionId,
                                        @PathVariable UUID photoId) {
        competitionsService.voteForPhoto(UUID.fromString(principal.getName()), competitionId, photoId);
    }
}
