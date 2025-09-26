package margo.grid.store.app.controller;

import margo.grid.store.app.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void addItemToCart() {
    }

    @Test
    void modifyItemInCart() {
    }

    @Test
    void deleteItemFromCart() {
    }

    @Test
    void getAllItemsInCart() {
    }
}