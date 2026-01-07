package logic;

import java.io.Serializable;

public enum ResponseStatus implements Serializable {

    // ===== availability =====
    AVAILABLE,
    SUGGESTED_TIMES,

    // ===== reservation =====
    RESERVATION_CREATED,
    INVALID_INPUT,

    // ===== code lookup =====
    CODE_FOUND,
    CODE_NOT_FOUND,
    CODES_SENT,
    NO_CODES_FOUND,

    // ===== cancel / payment =====
    CANCEL_SUCCESS,
    CANCEL_FAILED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // ===== terminal =====
    CHECKIN_SUCCESS,
    NO_AVAILABLE_TABLE,
    CHECKIN_NOT_ALLOWED,

    // ===== history =====
    ACCOUNT_VISITS,
    ACCOUNT_RESERVATIONS,

    // ===== generic =====
    SERVER_ERROR
}
