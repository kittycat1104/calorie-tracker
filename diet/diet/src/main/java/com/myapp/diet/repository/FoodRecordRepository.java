package com.myapp.diet.repository;

import com.myapp.diet.entity.FoodRecord;
import com.myapp.diet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface FoodRecordRepository extends JpaRepository<FoodRecord,Long> {
    List<FoodRecord> findByUserAndDate(User user, LocalDate now);
}
