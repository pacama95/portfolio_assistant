package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving positions
 */
@ApplicationScoped
public class GetPositionUseCase {

    @Inject
    PositionRepository positionRepository;

    /**
     * Gets a position by ID
     */
    @WithSession
    public Uni<Position> getById(UUID id) {
        return positionRepository.findById(id)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting position with ID %s".formatted(id),
                                throwable));
    }

    /**
     * Gets a position by ticker symbol
     */
    @WithSession
    public Uni<Position> getByTicker(String ticker) {
        return positionRepository.findByTicker(ticker)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting position with ticker %s".formatted(ticker),
                                throwable));
    }

    /**
     * Gets all positions including zero positions
     */
    public Uni<List<Position>> getAll() {
        return positionRepository.findAll()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting all positions",
                                throwable));
    }

    /**
     * Gets only active positions (with shares > 0)
     */
    public Uni<List<Position>> getActivePositions() {
        return positionRepository.findAllWithShares()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting all active positions",
                                throwable));
    }

    /**
     * Checks if a position exists for a ticker
     */
    @WithSession
    public Uni<Boolean> existsByTicker(String ticker) {
        return positionRepository.existsByTicker(ticker)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error checking if position with ticker %s exists".formatted(ticker),
                                throwable));
    }

    /**
     * Counts total positions
     */
    @WithSession
    public Uni<Long> countAll() {
        return positionRepository.countAll()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting positions  count",
                                throwable));
    }

    /**
     * Counts active positions (with shares > 0)
     */
    @WithSession
    public Uni<Long> countActivePositions() {
        return positionRepository.countWithShares().onFailure().transform(throwable ->
                new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                        "Error getting active positions count",
                        throwable));
    }
} 