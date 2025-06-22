package com.ecommerce.project.repositories;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageDetails);
    Page<Product> findByNameLikeIgnoreCase(String keyword, Pageable pageDetails);
    boolean existsByImage(String image);
    boolean existsByNameAndCategory(String name, Category category);
}
