package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.util.Map;

public interface ShoppingCartDao
{
    ShoppingCart getCartForUser(int userId);
    // add additional method signatures here
    void addProductToCart(int userId, int productId, int quantity);
    void updateProductQuantity(int userId, int productId, int quantity);
    void clearCartForUser(int userId);
}
