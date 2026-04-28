package com.nuvi.nuvi.common.api;

public enum ApiErrorCode {
    VALIDATION_FAILED("Request validation failed."),
    AUTH_REQUIRED("Authentication is required."),
    FORBIDDEN("Access is forbidden."),
    RATE_LIMITED("Too many requests."),
    FEATURE_DISABLED("This feature is disabled."),
    EMAIL_AUTH_DISABLED("Email authentication is disabled for beta."),
    CONSENT_REQUIRED("Required consent is missing."),
    PROFILE_INCOMPLETE("Profile is incomplete."),
    ALLERGY_CONFLICT("The recommendation conflicts with allergy information."),
    ALLERGY_MAPPING_INCOMPLETE("Allergen mapping is incomplete."),
    SUPPLEMENT_RULE_CONFLICT("The recommendation conflicts with supplement safety rules."),
    SUPPLEMENT_INPUT_INVALID("This supplement or medication input is not accepted."),
    ANAPHYLACTIC_BLOCK("This action is blocked by a severe allergy safety rule."),
    PRODUCT_UNAVAILABLE("The product is unavailable."),
    PARTNER_API_UNAVAILABLE("Partner API is unavailable."),
    PARTNER_OFFER_EXPIRED("Partner offer is expired."),
    CART_NOT_FOUND("Cart was not found."),
    STALE_CART_NEEDS_REFRESH("Cart needs price or availability refresh."),
    BUDGET_EXCEEDED_NO_ALTERNATIVES("No alternatives are available within budget."),
    MEAL_PLAN_NOT_FOUND("Meal plan was not found."),
    RECOMMENDATION_FAILED("Recommendation failed."),
    IDEMPOTENCY_KEY_CONFLICT("Idempotency key conflicts with a different payload."),
    INTERNAL_ERROR("Internal server error.");

    private final String defaultMessage;

    ApiErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
