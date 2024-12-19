package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/cart")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")

@CrossOrigin
public class ShoppingCartController {
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }


    @GetMapping
    public Map<String, Object> getCartForUser(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart cart = shoppingCartDao.getCartForUser(userId);

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> items = new HashMap<>();

            for (Map.Entry<Integer, ShoppingCartItem> entry : cart.getItems().entrySet()) {
                ShoppingCartItem item = entry.getValue();
                Product product = item.getProduct();

                Map<String, Object> productDetails = new HashMap<>();
                productDetails.put("productId", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("price", product.getPrice());
                productDetails.put("categoryId", product.getCategoryId());
                productDetails.put("description", product.getDescription());
                productDetails.put("color", product.getColor());
                productDetails.put("stock", product.getStock());
                productDetails.put("imageUrl", product.getImageUrl());
                productDetails.put("featured", product.isFeatured());

                Map<String, Object> itemDetails = new HashMap<>();
                itemDetails.put("product", productDetails);
                itemDetails.put("quantity", item.getQuantity());
                itemDetails.put("discountPercent", item.getDiscountPercent());
                itemDetails.put("lineTotal", item.getLineTotal());

                items.put(String.valueOf(product.getProductId()), itemDetails);
            }
            response.put("items", items);
            response.put("total", cart.getTotal());

            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addProductToCart(Principal principal, @PathVariable int productId) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.addProductToCart(userId, productId, 1);
            return getCartForUser(principal);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add product to cart.");
        }
    }
    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductQuantity(Principal principal, @PathVariable int productId, @RequestBody ShoppingCartItem item) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.updateProductQuantity(userId, productId, item.getQuantity());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product quantity.");
        }
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> clearCart(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();


            shoppingCartDao.clearCartForUser(userId);
            return getCartForUser(principal);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to clear the cart.");
        }
    }

}
