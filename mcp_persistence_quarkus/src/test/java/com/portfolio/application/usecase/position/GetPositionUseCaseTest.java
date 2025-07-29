package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetPositionUseCaseTest {
    private PositionRepository positionRepository;
    private GetPositionUseCase useCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        useCase = new GetPositionUseCase();
        useCase.positionRepository = positionRepository;
    }

    @Test
    void testGetByIdSuccess() {
        UUID id = UUID.randomUUID();
        Position position = mock(Position.class);
        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().item(position));

        Uni<Position> uni = useCase.getById(id);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(position, result);
    }

    @Test
    void testGetByIdError() {
        UUID id = UUID.randomUUID();
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Position> uni = useCase.getById(id);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(id.toString()));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetByTickerSuccess() {
        String ticker = "AAPL";
        Position position = mock(Position.class);

        when(positionRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item(position));
        Uni<Position> uni = useCase.getByTicker(ticker);

        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();
        assertEquals(position, result);
    }

    @Test
    void testGetByTickerError() {
        String ticker = "AAPL";
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Position> uni = useCase.getByTicker(ticker);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetAllSuccess() {
        List<Position> positions = Collections.singletonList(mock(Position.class));
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().item(positions));

        Uni<List<Position>> uni = useCase.getAll();
        List<Position> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(positions, result);
    }

    @Test
    void testGetAllError() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<List<Position>> uni = useCase.getAll();
        UniAssertSubscriber<List<Position>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("all positions"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetActivePositionsSuccess() {
        List<Position> positions = Collections.singletonList(mock(Position.class));
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));

        Uni<List<Position>> uni = useCase.getActivePositions();
        List<Position> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(positions, result);
    }

    @Test
    void testGetActivePositionsError() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<List<Position>> uni = useCase.getActivePositions();
        UniAssertSubscriber<List<Position>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("active positions"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testExistsByTickerSuccess() {
        String ticker = "AAPL";
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(true));

        Uni<Boolean> uni = useCase.existsByTicker(ticker);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertTrue(result);
    }

    @Test
    void testExistsByTickerError() {
        String ticker = "AAPL";
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Boolean> uni = useCase.existsByTicker(ticker);
        UniAssertSubscriber<Boolean> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testCountAllSuccess() {
        when(positionRepository.countAll()).thenReturn(Uni.createFrom().item(42L));

        Uni<Long> uni = useCase.countAll();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(42L, result);
    }

    @Test
    void testCountAllError() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.countAll()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Long> uni = useCase.countAll();
        UniAssertSubscriber<Long> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("positions  count"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testCountActivePositionsSuccess() {
        when(positionRepository.countWithShares()).thenReturn(Uni.createFrom().item(7L));

        Uni<Long> uni = useCase.countActivePositions();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(7L, result);
    }

    @Test
    void testCountActivePositionsError() {
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.countWithShares()).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Long> uni = useCase.countActivePositions();
        UniAssertSubscriber<Long> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("active positions count"));
        assertEquals(repoException, thrown.getCause());
    }
} 