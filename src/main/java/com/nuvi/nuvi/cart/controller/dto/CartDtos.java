package com.nuvi.nuvi.cart.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
            @Min(0)
            @Max(10_000_000)
            int amount,
            @NotBlank
            @Pattern(regexp = "KRW")
            String currency
    ) {
    }

    public record WeeklyCartCreateRequest(
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
            String weekStartDate,
            @Valid
            Money budgetOverride
    ) {
    }

    public record CartItemUpdateRequest(
            @Min(1)
            @Max(99)
            Integer quantity,
            Boolean selected
    ) {
        @JsonIgnore
        @AssertTrue(message = "quantity or selected must be provided.")
        public boolean isUpdateRequested() {
            return quantity != null || selected != null;
        }
    }

    public record CartItemReplaceRequest(
            @NotBlank
            @Pattern(regexp = "^prod_[A-Za-z0-9]+$")
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
