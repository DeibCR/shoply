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

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("/cart")
@PreAuthorize("hasRole('ROLE_USER')")
@CrossOrigin
public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    public Map<String, Object> getCartForUser(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart cart = shoppingCartDao.getCartForUser(userId);

            //ToDO: maps to store the data
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> items = new HashMap<>();

            //ToDO: loop to cartItems to extract info

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

            //ToDo: populate the maps

            response.put("items", items);
            response.put("total", cart.getTotal());

            return  response;
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProductToCart(Principal principal, @PathVariable int productId){
        try {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.addProductToCart(userId,productId,1);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add product to cart.");
        }
    }



    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated

    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductQuantity(Principal principal, @PathVariable int productId, @RequestBody ShoppingCartItem item){
        try {
            // Get the currently logged-in username
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.updateProductQuantity(userId, productId, item.getQuantity());
        }catch (Exception e){
            throw  new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product quantity.");
        }
    }



    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void clearCart(Principal principal) {
        try {
            // Get the currently logged-in username
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();


            shoppingCartDao.clearCartForUser(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to clear the cart.");
        }
    }

}
