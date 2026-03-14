package com.microservice.order.repositories;

import java.util.List;
import java.util.Optional;

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

    @Query("""
        select distinct o from Order o
        left join fetch o.orderItems
    """)
    List<Order> findAllWithItems();
}