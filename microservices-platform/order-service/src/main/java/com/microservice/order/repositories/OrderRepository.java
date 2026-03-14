package com.microservice.order.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.microservice.order.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumber(String orderNumber);

    @Query("""
        select o from Order o
        left join fetch o.orderItems
        where o.idempotencyKey = :idempotencyKey
    """)
    Optional<Order> findWithItemsByIdempotencyKey(String idempotencyKey);

    @Query(
        value = """
            select distinct o from Order o
            left join fetch o.orderItems
        """,
        countQuery = "select count(distinct o) from Order o"
    )
    Page<Order> findAllWithItems(Pageable pageable);
}