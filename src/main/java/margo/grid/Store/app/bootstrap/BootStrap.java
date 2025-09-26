package margo.grid.store.app.bootstrap;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.entity.Order;
import margo.grid.store.app.entity.OrderStatus;
import margo.grid.store.app.entity.User;
import margo.grid.store.app.repository.ItemRepository;
import margo.grid.store.app.repository.OrderRepository;
import margo.grid.store.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class BootStrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try{
            log.info("Loading bootstrap data...");
            if (userRepository.count() == 0) addUsers();
            if (itemRepository.count() == 0) addItems();
            if (orderRepository.count() == 0) addOrders();
            log.info("Bootstrap data loaded successfully!");
        }
        catch (IOException e){
            log.error("Error occurred while loading csv data, sorry:((");
        }
    }

    private void addUsers() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/users.csv");
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                User user = User.builder()
                        .email(data[0].trim())
                        .passwordHash(data[1].trim())
                        .build();
                users.add(user);
            }
        }
        userRepository.saveAll(users);
        log.info("Added {} users from CSV", users.size());
    }


    private void addItems() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/items.csv");
        List<Item> items = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))){
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null){
                String[] data = line.split(",");
                Item item = Item.builder()
                        .title(data[0].trim())
                        .price(new BigDecimal(data[1].trim()))
                        .availableQuantity(Integer.parseInt(data[2].trim()))
                        .build();
                items.add(item);
            }
        }

        itemRepository.saveAll(items);
        log.info("Added {} items", items.size());
    }


    public void addOrders() {
        List<User> users = userRepository.findAll();
        List<Item> items = itemRepository.findAll();
        if (users.isEmpty() || items.isEmpty()) {
            log.warn("Cannot create orders: users or items data was not loaded");
            return;
        }

        Random random = new Random();
        OrderStatus[] statuses = OrderStatus.values();


        for (int i = 0; i < 10; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            Order order = Order.builder()
                    .status(statuses[random.nextInt(statuses.length)])
                    .user(randomUser)
                    .items(new HashSet<>())
                    .build();
            int itemCount = random.nextInt(5) + 1;
            Set<Integer> usedItemIndices = new HashSet<>();

            for (int j = 0; j < itemCount; j++) {
                int itemIndex;
                do {
                    itemIndex = random.nextInt(items.size());
                } while (usedItemIndices.contains(itemIndex));

                usedItemIndices.add(itemIndex);
                Item randomItem = items.get(itemIndex);
                order.addItem(randomItem);
            }

            randomUser.addOrder(order);
            orderRepository.save(order);
        }
        userRepository.saveAll(users);
        log.info("Added 10 orders with random items and users");
    }
}