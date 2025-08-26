package FreshBid.back.repository;

import FreshBid.back.entity.PriceForecast;
import FreshBid.back.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceForecastRepository extends JpaRepository<PriceForecast, Long> {

    @Query("SELECT pf FROM PriceForecast pf " +
           "WHERE pf.itemCategory.id = :categoryId " +
           "AND pf.grade = :grade " +
           "AND pf.ds >= :startDate " +
           "ORDER BY pf.ds ASC")
    List<PriceForecast> findByCategoryAndGradeAndDateRange(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade, 
            @Param("startDate") LocalDate startDate);

    @Query("SELECT pf FROM PriceForecast pf " +
           "WHERE pf.itemCategory.id = :categoryId " +
           "AND pf.grade = :grade " +
           "AND pf.ds = :date")
    Optional<PriceForecast> findByCategoryAndGradeAndDate(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade, 
            @Param("date") LocalDate date);

    @Query("SELECT pf FROM PriceForecast pf " +
           "WHERE pf.itemCategory.id = :categoryId " +
           "AND pf.grade = :grade " +
           "ORDER BY pf.ds DESC")
    List<PriceForecast> findByCategoryAndGradeOrderByDateDesc(
            @Param("categoryId") Long categoryId, 
            @Param("grade") Product.Grade grade);
}
