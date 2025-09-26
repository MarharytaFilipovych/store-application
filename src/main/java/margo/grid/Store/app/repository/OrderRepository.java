package margo.grid.store.app.repository;

import margo.grid.store.app.entity.Order;
import margo.grid.store.app.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findOrdersByUser_IdAndStatus_(UUID userId, OrderStatus status, Pageable pageable);
}
