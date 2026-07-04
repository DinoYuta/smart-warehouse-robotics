package com.warehouse.repository;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    Optional<Mission> findByIdAndDeletedAtIsNull(Long id);

    List<Mission> findByAssignedRobotIdIsNotNullAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc(
            Collection<MissionStatus> statuses);

    List<Mission> findByStatusAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(MissionStatus status);

    List<Mission> findByAssignedRobotIdAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc(
            Long assignedRobotId,
            Collection<MissionStatus> statuses);

    long countByAssignedRobotIdAndStatusInAndDeletedAtIsNull(Long assignedRobotId,
                                                             Collection<MissionStatus> statuses);

    long countByAssignedRobotIdAndPriorityAndStatusInAndDeletedAtIsNull(Long assignedRobotId,
                                                                        Integer priority,
                                                                        Collection<MissionStatus> statuses);

    long countByStatusInAndDeletedAtIsNull(Collection<MissionStatus> statuses);
}
