package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.HandoverPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HandoverPhotoRepository extends JpaRepository<HandoverPhoto, Long> {

    List<HandoverPhoto> findByHandoverRecordId(Long handoverRecordId);
}
