package margo.grid.Store.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.OrderResponseDto;
import margo.grid.Store.app.dto.PageResponseDto;
import margo.grid.Store.app.dto.PaginationRequestDto;
import margo.grid.Store.app.service.OrderService;
import margo.grid.Store.app.utils.MyUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@AuthenticationPrincipal MyUserDetails user){
        OrderResponseDto orderResponse = orderService.createOrder(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}").build(orderResponse.getId());
        return ResponseEntity.created(location).body(orderResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable UUID id,
                                                     @AuthenticationPrincipal MyUserDetails user) {
        OrderResponseDto order = orderService.getOrderById(id, user);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id,
                                            @AuthenticationPrincipal MyUserDetails user){
        orderService.cancelOrder(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<OrderResponseDto>> getAllUserOrders(@Valid PaginationRequestDto pagination,
                                                                              @AuthenticationPrincipal MyUserDetails user){
        Page<OrderResponseDto> orders = orderService.getAllUserOrders(user, pagination.getLimit(), pagination.getOffset());
        return ResponseEntity.ok().body(PageResponseDto.from(orders));
    }
}