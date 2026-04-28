package com.nuvi.nuvi.cart.controller.dto;

import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public enum CartStatus {
        DRAFT,
        ACTIVE,
        STALE,
        ARCHIVED
    }

    public enum CartItemType {
        FOOD_PRODUCT,
        SUPPLEMENT
    }

    public enum RecommendationReasonCode {
        GOAL_MATCH,
        BUDGET_FIT,
        PREFERENCE_MATCH,
        BASELINE_CONTINUITY,
        AVAILABILITY,
        SAFETY_FILTERED
    }

    public enum SafetyWarningCode {
        INTOLERANCE_NOTICE,
        DUPLICATE_INGREDIENT,
        EXCESS_RISK,
        CONSULT_EXPERT,
        INSUFFICIENT_DATA,
        SPONSORED_DISCLOSURE
    }

    public enum SafetyWarningSeverity {
        INFO,
        NOTICE,
        WARNING,
        BLOCKED
    }

    public enum ReplaceReason {
        PRICE,
        TASTE,
        AVAILABILITY,
        SAFETY_WARNING,
        USER_PREFERENCE
    }

    public record Money(
            int amount,
            String currency
    ) {
    }

    public record WeeklyCartCreateRequest(
            String weekStartDate,
            Money budgetOverride
    ) {
    }

    public record CartItemUpdateRequest(
            Integer quantity,
            Boolean selected
    ) {
    }

    public record CartItemReplaceRequest(
            String replacementProductId,
            ReplaceReason reason
    ) {
    }

    public record RecommendationReason(
            RecommendationReasonCode code,
            String message
    ) {
    }

    public record SafetyWarning(
            SafetyWarningCode code,
            SafetyWarningSeverity severity,
            String message
    ) {
    }

    public record SafetySummary(
            int blockedCount,
            int warningCount,
            boolean medicationInputAccepted,
            boolean expertConsultationRecommended
    ) {
    }

    public record CartTotals(
            Money subtotal,
            Money estimatedShipping,
            Money estimatedTotal
    ) {
    }

    public record CartItem(
            String id,
            String productId,
            String offerId,
            CartItemType itemType,
            String name,
            int quantity,
            Money unitPrice,
            boolean selected,
            boolean sponsored,
            List<RecommendationReason> reasons,
            List<SafetyWarning> warnings
    ) {
    }

    public record Cart(
            String id,
            CartStatus status,
            String weekStartDate,
            String baselineCartId,
            List<CartItem> items,
            CartTotals totals,
            SafetySummary safetySummary,
            String recommendationId,
            String generatedAt,
            String refreshedAt
    ) {
    }

    public record CartAlternative(
            String productId,
            String offerId,
            String name,
            CartItemType itemType,
            Money unitPrice,
            List<RecommendationReason> reasons,
            List<SafetyWarning> warnings
    ) {
    }

    public record CartAlternativesResponse(
            List<CartAlternative> alternatives
    ) {
    }
}
