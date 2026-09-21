package com.marcos.conversordemoedas.repository;

import com.marcos.conversordemoedas.model.ConversionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {

    List<ConversionHistory> findAllByOrderByCreatedAtDesc();
}
