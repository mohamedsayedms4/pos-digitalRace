package com.example.pos.repository;

import com.example.pos.entity.Installment;
import com.example.pos.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByPurchaseInvoice(PurchaseInvoice purchaseInvoice);
    List<Installment> findByPurchaseInvoiceIdOrderByDueDateAsc(Long invoiceId);
    List<Installment> findByStatus(Installment.InstallmentStatus status);
}
