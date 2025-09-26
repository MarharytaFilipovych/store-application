package margo.grid.store.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"items", "user"})
@Builder
@ToString(exclude = {"items", "user"})
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private margo.grid.Store.app.entity.OrderStatus status;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "order_item",
                joinColumns = @JoinColumn(name = "order_id"),
                inverseJoinColumns = @JoinColumn(name = "item_id"))
    private Set<Item> items = new HashSet<>();

    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
            item.getOrders().add(this);
        }
    }

    public void removeItem(Item item) {
        if (item != null && items.contains(item)) {
            items.remove(item);
            item.getOrders().remove(this);
        }
    }

    @ManyToOne(targetEntity = User.class, optional = false)
    private User user;
}


