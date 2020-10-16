package org.panacea.drmp.bia.domain.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BIAException extends RuntimeException {

    protected Throwable throwable;

    public BIAException(String message) {
        super(message);
    }

    public BIAException(String message, Throwable throwable) {
        super(message);
        this.throwable = throwable;
        log.error("[BIA]: ", message);
    }

    public Throwable getCause() {
        return throwable;
    }
}