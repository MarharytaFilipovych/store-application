package margo.grid.Store.app.controller;

import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.OrderResponseDto;
import margo.grid.Store.app.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(){
        OrderResponseDto orderResponse = orderService.createOrder();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}").build(orderResponse.getId());
        return ResponseEntity.created(location).body(orderResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable UUID id) {
        OrderResponseDto orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id){
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
