package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.position.GetPositionUseCase;
import com.portfolio.application.usecase.position.RecalculatePositionUseCase;
import com.portfolio.application.usecase.position.UpdateMarketDataUseCase;
import com.portfolio.infrastructure.rest.dto.PositionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateMarketDataRequest;
import com.portfolio.infrastructure.rest.mapper.PositionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for position management
 */
@Path("/api/positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PositionController {

    @Inject
    GetPositionUseCase getPositionUseCase;

    @Inject
    UpdateMarketDataUseCase updateMarketDataUseCase;

    @Inject
    RecalculatePositionUseCase recalculatePositionUseCase;

    @Inject
    PositionMapper positionMapper;

    /**
     * Get all positions
     */
    @GET
    public Uni<List<PositionResponse>> getAllPositions() {
        return getPositionUseCase.getAll()
            .map(positionMapper::toResponses);
    }

    /**
     * Get only active positions (with shares > 0)
     */
    @GET
    @Path("/active")
    public Uni<List<PositionResponse>> getActivePositions() {
        return getPositionUseCase.getActivePositions()
            .map(positionMapper::toResponses);
    }

    /**
     * Get position by ID
     */
    @GET
    @Path("/{id}")
    public Uni<Response> getPosition(@PathParam("id") UUID id) {
        return getPositionUseCase.getById(id)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            });
    }

    /**
     * Get position by ticker
     */
    @GET
    @Path("/ticker/{ticker}")
    public Uni<Response> getPositionByTicker(@PathParam("ticker") String ticker) {
        return getPositionUseCase.getByTicker(ticker)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            });
    }

    /**
     * Update market price for a position
     */
    @PUT
    @Path("/ticker/{ticker}/price")
    public Uni<Response> updateMarketPrice(@PathParam("ticker") String ticker, 
                                         @Valid UpdateMarketDataRequest request) {
        return updateMarketDataUseCase.execute(ticker, request.price())
            .map(position -> Response.ok(positionMapper.toResponse(position)).build())
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating market data: " + throwable.getMessage())
                    .build()
            );
    }

    /**
     * Recalculate position from transactions
     */
    @POST
    @Path("/ticker/{ticker}/recalculate")
    public Uni<Response> recalculatePosition(@PathParam("ticker") String ticker) {
        return recalculatePositionUseCase.execute(ticker)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            })
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error recalculating position: " + throwable.getMessage())
                    .build()
            );
    }

    /**
     * Check if position exists for ticker
     */
    @GET
    @Path("/ticker/{ticker}/exists")
    public Uni<Boolean> checkPositionExists(@PathParam("ticker") String ticker) {
        return getPositionUseCase.existsByTicker(ticker);
    }

    /**
     * Get total position count
     */
    @GET
    @Path("/count")
    public Uni<Long> getPositionCount() {
        return getPositionUseCase.countAll();
    }

    /**
     * Get active position count
     */
    @GET
    @Path("/count/active")
    public Uni<Long> getActivePositionCount() {
        return getPositionUseCase.countActivePositions();
    }
} 