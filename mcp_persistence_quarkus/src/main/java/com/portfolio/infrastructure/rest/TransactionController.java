package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.transaction.CreateTransactionUseCase;
import com.portfolio.application.usecase.transaction.DeleteTransactionUseCase;
import com.portfolio.application.usecase.transaction.GetTransactionUseCase;
import com.portfolio.application.usecase.transaction.UpdateTransactionUseCase;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateTransactionRequest;
import com.portfolio.infrastructure.rest.mapper.TransactionMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for transaction management
 */
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    GetTransactionUseCase getTransactionUseCase;

    @Inject
    UpdateTransactionUseCase updateTransactionUseCase;

    @Inject
    DeleteTransactionUseCase deleteTransactionUseCase;

    @Inject
    TransactionMapper transactionMapper;

    /**
     * Create a new transaction
     */
    @POST
    public Uni<Response> createTransaction(@Valid CreateTransactionRequest request) {
        return Uni.createFrom().item(() -> transactionMapper.toCreateTransactionCommand(request))
                .flatMap(command -> createTransactionUseCase.execute(command))
                .map(transaction -> transactionMapper.toResponse(transaction))
                .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
                .onFailure().recoverWithItem(throwable ->
                    Response.status(Response.Status.BAD_REQUEST)
                       .entity("Error creating transaction: " + throwable.getMessage())
                      .build());
    }

    /**
     * Get transaction by ID
     */
    @GET
    @Path("/{id}")
    public Uni<Response> getTransaction(@PathParam("id") UUID id) {
        return getTransactionUseCase.getById(id)
            .map(transaction -> {
                if (transaction == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(transactionMapper.toResponse(transaction)).build();
            });
    }

    /**
     * Get all transactions
     */
    @GET
    public Multi<TransactionResponse> getAllTransactions() {
        return getTransactionUseCase.getAll()
            .map(transactionMapper::toResponse);
    }

    /**
     * Get transactions by ticker
     */
    @GET
    @Path("/ticker/{ticker}")
    public Multi<TransactionResponse> getTransactionsByTicker(@PathParam("ticker") String ticker) {
        return getTransactionUseCase.getByTicker(ticker)
            .map(transactionMapper::toResponse);
    }

    /**
     * Search transactions with filters
     */
    @GET
    @Path("/search")
    public Multi<TransactionResponse> searchTransactions(
            @QueryParam("ticker") String ticker,
            @QueryParam("type") TransactionType type,
            @QueryParam("fromDate") LocalDate fromDate,
            @QueryParam("toDate") LocalDate toDate) {
        
        return getTransactionUseCase.searchTransactions(ticker, type, fromDate, toDate)
            .map(transactionMapper::toResponse);
    }

    /**
     * Update a transaction
     */
    @PUT
    @Path("/{id}")
    public Uni<Response> updateTransaction(@PathParam("id") UUID id, @Valid UpdateTransactionRequest updateTransactionRequest) {
        return Uni.createFrom().item(() -> transactionMapper.toUpdateTransactionCommand(id, updateTransactionRequest))
                .flatMap(updatedTransaction -> updateTransactionUseCase.execute(updatedTransaction))
                .map(transaction -> {
                    if (transaction == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(transactionMapper.toResponse(transaction)).build();
                })
                .onFailure().recoverWithItem(throwable ->
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity("Error updating transaction: " + throwable.getMessage())
                        .build()
                );
    }

    /**
     * Delete a transaction
     */
    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteTransaction(@PathParam("id") UUID id) {
        return deleteTransactionUseCase.execute(id)
            .map(deleted -> {
                if (deleted) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            });
    }

    /**
     * Get transaction count
     */
    @GET
    @Path("/count")
    public Uni<Long> getTransactionCount() {
        return getTransactionUseCase.countAll();
    }

    /**
     * Get transaction count by ticker
     */
    @GET
    @Path("/count/{ticker}")
    public Uni<Long> getTransactionCountByTicker(@PathParam("ticker") String ticker) {
        return getTransactionUseCase.countByTicker(ticker);
    }
} 