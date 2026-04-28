package com.nuvi.nuvi.cart.controller;

import com.nuvi.nuvi.cart.application.CartApplicationService;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.Cart;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartAlternativesResponse;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartItemReplaceRequest;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.CartItemUpdateRequest;
import com.nuvi.nuvi.cart.controller.dto.CartDtos.WeeklyCartCreateRequest;
import com.nuvi.nuvi.common.api.ApiResponse;
import com.nuvi.nuvi.common.api.RequestMetaFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/carts")
public class CartController {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final CartApplicationService cartService;
    private final RequestMetaFactory metaFactory;

    public CartController(CartApplicationService cartService, RequestMetaFactory metaFactory) {
        this.cartService = cartService;
        this.metaFactory = metaFactory;
    }

    @PostMapping("/weekly")
    public ResponseEntity<ApiResponse<Cart>> generateWeeklyCart(
            @RequestHeader(IDEMPOTENCY_KEY) @NotBlank @Size(min = 16, max = 128) String idempotencyKey,
            @Valid @RequestBody(required = false) WeeklyCartCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(cartService.generateWeeklyCart(request), metaFactory.current()));
    }

    @GetMapping("/current")
    public ApiResponse<Cart> getCurrentCart() {
        return ApiResponse.ok(cartService.getCurrentCart(), metaFactory.current());
    }

    @GetMapping("/{cartId}")
    public ApiResponse<Cart> getCartById(@PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId) {
        return ApiResponse.ok(cartService.getCart(cartId), metaFactory.current());
    }

    @PatchMapping("/{cartId}/items/{cartItemId}")
    public ApiResponse<Cart> updateCartItem(
            @RequestHeader(IDEMPOTENCY_KEY) @NotBlank @Size(min = 16, max = 128) String idempotencyKey,
            @PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId,
            @PathVariable @Pattern(regexp = "^citem_[A-Za-z0-9]+$") String cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        return ApiResponse.ok(cartService.updateItem(cartId), metaFactory.current());
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ApiResponse<Cart> removeCartItem(
            @RequestHeader(IDEMPOTENCY_KEY) @NotBlank @Size(min = 16, max = 128) String idempotencyKey,
            @PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId,
            @PathVariable @Pattern(regexp = "^citem_[A-Za-z0-9]+$") String cartItemId
    ) {
        return ApiResponse.ok(cartService.removeItem(cartId), metaFactory.current());
    }

    @PostMapping("/{cartId}/items/{cartItemId}/replace")
    public ApiResponse<Cart> replaceCartItem(
            @RequestHeader(IDEMPOTENCY_KEY) @NotBlank @Size(min = 16, max = 128) String idempotencyKey,
            @PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId,
            @PathVariable @Pattern(regexp = "^citem_[A-Za-z0-9]+$") String cartItemId,
            @Valid @RequestBody CartItemReplaceRequest request
    ) {
        return ApiResponse.ok(cartService.replaceItem(cartId), metaFactory.current());
    }

    @GetMapping("/{cartId}/items/{cartItemId}/alternatives")
    public ApiResponse<CartAlternativesResponse> getCartItemAlternatives(
            @PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId,
            @PathVariable @Pattern(regexp = "^citem_[A-Za-z0-9]+$") String cartItemId
    ) {
        return ApiResponse.ok(cartService.getAlternatives(), metaFactory.current());
    }

    @PostMapping("/{cartId}/refresh")
    public ApiResponse<Cart> refreshCart(
            @RequestHeader(IDEMPOTENCY_KEY) @NotBlank @Size(min = 16, max = 128) String idempotencyKey,
            @PathVariable @Pattern(regexp = "^cart_[A-Za-z0-9]+$") String cartId
    ) {
        return ApiResponse.ok(cartService.refreshCart(cartId), metaFactory.current());
    }
}
