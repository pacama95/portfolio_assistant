package com.portfolio.application.usecase.portfolio;

import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.model.PortfolioSummary;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.exception.Errors;

class GetPortfolioSummaryUseCaseTest {
    private PositionRepository positionRepository;
    private GetPortfolioSummaryUseCase useCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        useCase = new GetPortfolioSummaryUseCase();
        useCase.positionRepository = positionRepository;
    }

    @Test
    void testGetPortfolioSummaryWithEmptyPositions() {
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        Uni<PortfolioSummary> uni = useCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.totalMarketValue());
        assertEquals(BigDecimal.ZERO, summary.totalCost());
        assertEquals(BigDecimal.ZERO, summary.totalUnrealizedGainLoss());
        assertEquals(BigDecimal.ZERO, summary.totalUnrealizedGainLossPercentage());
        assertEquals(0, summary.totalPositions());
        assertEquals(0, summary.activePositions());
    }

    @Test
    void testGetPortfolioSummaryWithPositions() {
        Position p1 = mock(Position.class);
        when(p1.hasShares()).thenReturn(true);
        when(p1.getMarketValue()).thenReturn(new BigDecimal("100.00"));
        when(p1.getTotalCost()).thenReturn(new BigDecimal("80.00"));

        Position p2 = mock(Position.class);
        when(p2.hasShares()).thenReturn(false);
        when(p2.getMarketValue()).thenReturn(new BigDecimal("50.00"));
        when(p2.getTotalCost()).thenReturn(new BigDecimal("60.00"));

        List<Position> positions = List.of(p1, p2);
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().item(positions));

        Uni<PortfolioSummary> uni = useCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(summary);
        assertEquals(new BigDecimal("150.00"), summary.totalMarketValue());
        assertEquals(new BigDecimal("140.00"), summary.totalCost());
        assertEquals(new BigDecimal("10.00"), summary.totalUnrealizedGainLoss());
        assertEquals(new BigDecimal("7.142857"), summary.totalUnrealizedGainLossPercentage());
        assertEquals(2, summary.totalPositions());
        assertEquals(1, summary.activePositions());
    }

    @Test
    void testGetActiveSummary() {
        Position p1 = mock(Position.class);
        when(p1.hasShares()).thenReturn(true);
        when(p1.getMarketValue()).thenReturn(new BigDecimal("200.00"));
        when(p1.getTotalCost()).thenReturn(new BigDecimal("150.00"));

        List<Position> positions = List.of(p1);
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));

        Uni<PortfolioSummary> uni = useCase.getActiveSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(summary);
        assertEquals(new BigDecimal("200.00"), summary.totalMarketValue());
        assertEquals(new BigDecimal("150.00"), summary.totalCost());
        assertEquals(new BigDecimal("50.00"), summary.totalUnrealizedGainLoss());
        assertEquals(new BigDecimal("33.333333"), summary.totalUnrealizedGainLossPercentage());
        assertEquals(1, summary.totalPositions());
        assertEquals(1, summary.activePositions());
    }

    @Test
    void testGetPortfolioSummaryWhenErrorFindingAllPositions() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<PortfolioSummary> uni = useCase.getPortfolioSummary();
        UniAssertSubscriber<PortfolioSummary> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, thrown.getError());
        assertEquals("Error getting all positions", thrown.getMessage());
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetActiveSummaryWhenErrorFindingAllWithSharesError() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<PortfolioSummary> uni = useCase.getActiveSummary();
        UniAssertSubscriber<PortfolioSummary> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, thrown.getError());
        assertEquals("Error getting all positions with shares", thrown.getMessage());
        assertEquals(repoException, thrown.getCause());
    }
} 