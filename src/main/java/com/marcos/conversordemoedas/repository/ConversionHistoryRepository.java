package com.marcos.conversordemoedas.repository;

import com.marcos.conversordemoedas.model.ConversionHistory;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {

    List<ConversionHistory> findTop100ByOwnerIdOrderByCreatedAtDescIdDesc(String ownerId);

    List<ConversionHistory> findTop101ByOwnerIdOrderByCreatedAtDescIdDesc(String ownerId);

    @Modifying
    @Query("delete from ConversionHistory history where history.ownerId = :ownerId")
    int deleteByOwnerId(@Param("ownerId") String ownerId);

    @Modifying
    @Query("delete from ConversionHistory history where history.createdAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
