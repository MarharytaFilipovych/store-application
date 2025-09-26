package margo.grid.store.app.bootstrap;

import lombok.RequiredArgsConstructor;
import margo.grid.store.app.repository.ItemRepository;
import margo.grid.store.app.repository.OrderRepository;
import margo.grid.store.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootStrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {

    }

    private void addUsers(){

    }

    private void addItems(){

    }

    private void addOrders(){

    }
}
