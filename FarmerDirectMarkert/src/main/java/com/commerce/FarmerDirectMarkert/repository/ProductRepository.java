package com.commerce.FarmerDirectMarkert.repository;

import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.model.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmerEmail(String farmerEmail);

    @Query("select p from Product p where "
            + "(:category is null or p.category = :category) "
            + "and (:minPrice is null or p.price >= :minPrice) "
            + "and (:maxPrice is null or p.price <= :maxPrice) "
            + "and (" 
            + "(:searchTerm is null) " 
            + "or lower(p.name) like lower(concat('%', :searchTerm, '%')) "
            + "or lower(p.description) like lower(concat('%', :searchTerm, '%')))")
    List<Product> findByFilters(
            @Param("category") Category category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("searchTerm") String searchTerm,
            Sort sort
    );
}
