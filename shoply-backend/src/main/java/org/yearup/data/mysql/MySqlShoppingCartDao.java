package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getCartForUser(int userId) {

            String sql = "SELECT sc.product_id, sc.quantity, p.name, p.price, p.category_id, p.description, p.color, p.stock, p.image_url, p.featured FROM shopping_cart sc JOIN products p ON sc.product_id = p.product_id WHERE sc.user_id = ?";
            ShoppingCart cart = new ShoppingCart();

            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Product product = new Product(
                                rs.getInt("product_id"),
                                rs.getString("name"),
                                rs.getBigDecimal("price"),
                                rs.getInt("category_id"),
                                rs.getString("description"),
                                rs.getString("color"),
                                rs.getInt("stock"),
                                rs.getBoolean("featured"),
                                rs.getString("image_url")
                                                );

                        ShoppingCartItem item = new ShoppingCartItem();
                        item.setProduct(product);
                        item.setQuantity(rs.getInt("quantity"));
                        item.setDiscountPercent(BigDecimal.ZERO);

                        cart.add(item);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch shopping cart", e);
            }

            return cart;
        }

    @Override
    public void addProductToCart(int userId, int productId, int quantity) {
        String sql =
                        "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE quantity = quantity + ?";


        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.setInt(4, quantity);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product quantity", e);
        }


    }

    @Override
    public void clearCartForUser(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart for user", e);
        }

    }
}
