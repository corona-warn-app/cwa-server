package org.ena.server.services.common.persistence.repository;

import org.ena.server.services.common.persistence.domain.DiagnosisKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends JpaRepository<DiagnosisKey, Long> {
}
