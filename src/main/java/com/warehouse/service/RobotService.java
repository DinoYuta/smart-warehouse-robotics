package com.warehouse.service;

import com.warehouse.model.Robot;
import com.warehouse.repository.RobotRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RobotService {

    private final RobotRepository robotRepository;

    public RobotService(RobotRepository robotRepository) {
        this.robotRepository = robotRepository;
    }

    public List<Robot> getRobots() {
        return robotRepository.findAllByOrderByIdAsc();
    }
}
