package com.ecommerce.project.repositories;

import com.ecommerce.project.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
