package margo.grid.store.app.bootstrap;

import margo.grid.store.app.repository.ItemRepository;
import margo.grid.store.app.repository.OrderRepository;
import margo.grid.store.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BootStrapTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    BootStrap bootStrap;

    @BeforeEach
    void setUp() {
        bootStrap = new BootStrap(userRepository, itemRepository, orderRepository);
    }

    @Test
    void run(){
        bootStrap.run();
        assertThat(userRepository.count()).isGreaterThan(0);
        assertThat(itemRepository.count()).isGreaterThan(0);
        assertThat(orderRepository.count()).isGreaterThan(0);
    }
}