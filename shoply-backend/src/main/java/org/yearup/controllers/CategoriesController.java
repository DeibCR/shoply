package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.security.jwt.TokenProvider;

import java.util.List;


@RestController
@RequestMapping("categories")
@CrossOrigin
public class CategoriesController
{
    private CategoryDao categoryDao;
    private ProductDao productDao;
    private TokenProvider tokenProvider;





    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao, TokenProvider tokenProvider) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
        this.tokenProvider = tokenProvider;
    }



    @GetMapping
    public List<Category> getAll()
    {
        // find and return all categories
        return categoryDao.getAllCategories();
    }


    @GetMapping("{id}")
    public ResponseEntity<Object> getById(@PathVariable int id)
    {
        // get the category by id
        Category category = categoryDao.getById(id);

        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(category);
    }


    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // get a list of product by categoryId
        return null;
    }


    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody Category category, @RequestHeader ("Authorization") String authHeader)
    {
        String token = authHeader.replace("Bearer ", "");

        if (!tokenProvider.validateToken(token)) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired token");
        }



        try
        {
            Category createdCategory = categoryDao.create(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {

        if (categoryDao.getById(id) ==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        try {
            categoryDao.update(id,category);
        } catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad");
        }
    }



    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteCategory(@PathVariable int id)
    {

        if (categoryDao.getById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        try {
            categoryDao.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad");
        }
    }
}