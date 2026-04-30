package com.nuvi.nuvi.cart.application;

import com.nuvi.nuvi.cart.controller.dto.CartDtos.Cart;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartAlternative;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartAlternativesResponse;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartItem;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartItemType;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartStatus;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartTotals;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.Money;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.RecommendationReason;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.RecommendationReasonCode;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.SafetySummary;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.WeeklyCartCreateRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class CartApplicationService {

    public Cart generateWeeklyCart(WeeklyCartCreateRequest request) {
        String weekStartDate = request != null && request.weekStartDate() != null
                ? request.weekStartDate()
                : LocalDate.now(ZoneOffset.UTC).toString();
        return skeletonCart("cart_skeleton", weekStartDate);
    }

    public Cart getCurrentCart() {
        return skeletonCart("cart_current", LocalDate.now(ZoneOffset.UTC).toString());
    }

    public Cart getCart(String cartId) {
        return skeletonCart(cartId, LocalDate.now(ZoneOffset.UTC).toString());
    }

    public Cart updateItem(String cartId) {
        return skeletonCart(cartId, LocalDate.now(ZoneOffset.UTC).toString());
    }

    public Cart excludeItem(String cartId) {
        return skeletonCart(cartId, LocalDate.now(ZoneOffset.UTC).toString());
    }

    public Cart replaceItem(String cartId) {
        return skeletonCart(cartId, LocalDate.now(ZoneOffset.UTC).toString());
    }

    public Cart refreshCart(String cartId) {
        return skeletonCart(cartId, LocalDate.now(ZoneOffset.UTC).toString());
    }

    public CartAlternativesResponse getAlternatives() {
        return new CartAlternativesResponse(List.of(new CartAlternative(
                "prod_skeleton_alt",
                "offer_skeleton_alt",
                "Nuvi skeleton alternative",
                CartItemType.FOOD_PRODUCT,
                new Money(12900, "KRW"),
                List.of(new RecommendationReason(RecommendationReasonCode.BUDGET_FIT, "Fits the weekly budget.")),
                List.of()
        )));
    }

    private Cart skeletonCart(String cartId, String weekStartDate) {
        Money unitPrice = new Money(12900, "KRW");
        CartItem item = new CartItem(
                "citem_skeleton",
                "prod_skeleton",
                "offer_skeleton",
                CartItemType.FOOD_PRODUCT,
                "Nuvi skeleton product",
                1,
                unitPrice,
                true,
                false,
                List.of(new RecommendationReason(RecommendationReasonCode.GOAL_MATCH, "Matches the selected profile goal.")),
                List.of()
        );
        return new Cart(
                cartId,
                CartStatus.ACTIVE,
                weekStartDate,
                null,
                List.of(item),
                new CartTotals(unitPrice, new Money(0, "KRW"), unitPrice),
                new SafetySummary(0, 0, false, false),
                "rec_skeleton",
                ZonedDateTime.now(ZoneOffset.UTC).toString(),
                null
        );
    }
}
