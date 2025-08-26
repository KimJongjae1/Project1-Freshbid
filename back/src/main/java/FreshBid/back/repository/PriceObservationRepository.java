package FreshBid.back.repository;

import FreshBid.back.entity.PriceObservation;
import FreshBid.back.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceObservationRepository extends JpaRepository<PriceObservation, Long> {

    @Query("SELECT po FROM PriceObservation po " +
           "WHERE po.itemCategory.id = :categoryId " +
           "AND po.grade = :grade " +
           "ORDER BY po.observedAt DESC")
    List<PriceObservation> findByCategoryAndGradeOrderByDateDesc(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade);

    @Query("SELECT po FROM PriceObservation po " +
           "WHERE po.itemCategory.id = :categoryId " +
           "AND po.grade = :grade " +
           "AND po.observedAt = :date")
    Optional<PriceObservation> findByCategoryAndGradeAndDate(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade, 
            @Param("date") LocalDate date);

    @Query("SELECT po FROM PriceObservation po " +
           "WHERE po.itemCategory.id = :categoryId " +
           "AND po.grade = :grade " +
           "AND po.observedAt >= :startDate " +
           "ORDER BY po.observedAt ASC")
    List<PriceObservation> findByCategoryAndGradeAndDateRange(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade, 
            @Param("startDate") LocalDate startDate);

    @Query("SELECT po FROM PriceObservation po " +
           "WHERE po.itemCategory.id = :categoryId")
    List<PriceObservation> findByItemCategoryId(@Param("categoryId") Integer categoryId);
}
