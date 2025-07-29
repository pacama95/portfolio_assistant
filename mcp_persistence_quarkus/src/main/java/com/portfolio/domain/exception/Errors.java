package com.portfolio.domain.exception;

public interface Errors {

    interface GetPortfolioSummary {
        String errorCode = "10";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface GetPosition {
        String errorCode = "02";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface RecalculatePosition {
        String errorCode = "03";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface UpdateMarketData {
        String errorCode = "04";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

}
