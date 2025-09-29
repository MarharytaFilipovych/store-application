package margo.grid.store.app.config;

public class PathConstants {
    public static final String MAIN = "/store";

    public static final String AUTH_PATH = "/auth";
    public static final String ITEMS_PATH = "/items";
    public static final String ORDERS_PATH = "/orders";
    public static final String CART_PATH = "/cart";
    public static final String CART_ITEMS_PATH = "/cart-items";

    public static final String LOGIN_PATH = "/login";
    public static final String REGISTER_PATH = "/register";
    public static final String LOGOUT_PATH = "/logout";
    public static final String FORGOT_PASSWORD_PATH = "/forgot-password";
    public static final String RESET_PASSWORD_PATH = "/reset-password";

    public static final String FULL_AUTH_PATH = MAIN + AUTH_PATH;
    public static final String FULL_ITEMS_PATH = MAIN + ITEMS_PATH;
    public static final String FULL_ORDERS_PATH = MAIN + ORDERS_PATH;
    public static final String FULL_CART_PATH = MAIN + CART_PATH;
    public static final String FULL_CART_ITEMS_PATH = MAIN + CART_ITEMS_PATH;

    public static final String FULL_LOGIN_PATH = FULL_AUTH_PATH + LOGIN_PATH;
    public static final String FULL_REGISTER_PATH = FULL_AUTH_PATH + REGISTER_PATH;
    public static final String FULL_LOGOUT_PATH = FULL_AUTH_PATH + LOGOUT_PATH;
    public static final String FULL_FORGOT_PASSWORD_PATH = FULL_AUTH_PATH + FORGOT_PASSWORD_PATH;
    public static final String FULL_RESET_PASSWORD_PATH = FULL_AUTH_PATH + RESET_PASSWORD_PATH;

    public static final String AUTH_LOGIN = AUTH_PATH + LOGIN_PATH;
    public static final String AUTH_REGISTER = AUTH_PATH + REGISTER_PATH;
    public static final String AUTH_LOGOUT = AUTH_PATH + LOGOUT_PATH;
    public static final String AUTH_FORGOT_PASSWORD = AUTH_PATH + FORGOT_PASSWORD_PATH;
    public static final String AUTH_RESET_PASSWORD = AUTH_PATH + RESET_PASSWORD_PATH;


    private PathConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}