package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.portfolio.infrastructure.rest.dto.PortfolioSummaryResponse;
import com.portfolio.infrastructure.rest.mapper.PortfolioSummaryMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * REST controller for portfolio summary operations
 */
@Path("/api/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioController {

    @Inject
    GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;

    @Inject
    PortfolioSummaryMapper portfolioSummaryMapper;

    /**
     * Get complete portfolio summary (all positions)
     */
    @GET
    @Path("/summary")
    public Uni<PortfolioSummaryResponse> getPortfolioSummary() {
        return getPortfolioSummaryUseCase.getPortfolioSummary()
            .map(portfolioSummaryMapper::toResponse);
    }

    /**
     * Get active portfolio summary (only positions with shares > 0)
     */
    @GET
    @Path("/summary/active")
    public Uni<PortfolioSummaryResponse> getActivePortfolioSummary() {
        return getPortfolioSummaryUseCase.getActiveSummary()
            .map(portfolioSummaryMapper::toResponse);
    }
} 