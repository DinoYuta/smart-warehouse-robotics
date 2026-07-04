package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.dto.CustomerPickupCodeFormDto;
import com.warehouse.model.CustomerPickupCode;
import com.warehouse.model.CustomerPickupCodeStatus;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.CustomerPickupCodeRepository;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.repository.ZonePolicyAssignmentRepository;
import com.warehouse.service.CustomerPickupCodeService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CustomerPickupCodeFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerPickupCodeRepository pickupCodeRepository;

    @Autowired
    private CustomerPickupCodeService pickupCodeService;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private ZonePolicyAssignmentRepository assignmentRepository;

    @BeforeEach
    void cleanData() {
        assignmentRepository.deleteAll();
        pickupCodeRepository.deleteAll();
        missionRepository.deleteAll();
        resetSeededRobots();
    }

    @Test
    void managerPickupCodePageLoadsWithCreationFormAndRecentList() throws Exception {
        mockMvc.perform(get("/manager/customer-pickup-codes"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-customer-pickup-codes"))
                .andExpect(model().attributeExists("pickupCodeForm", "pickupCodes", "cargoTypes", "locationsByZone"))
                .andExpect(content().string(containsString("Customer Pickup Codes")))
                .andExpect(content().string(containsString("Create Pickup Code")))
                .andExpect(content().string(containsString("Customer Name")))
                .andExpect(content().string(containsString("Customer Email")))
                .andExpect(content().string(containsString("Customer Phone")))
                .andExpect(content().string(containsString("Email Preview")));
    }

    @Test
    void managerCanCreatePickupCodeWithValidatedCustomerDataAndEmailPreview() throws Exception {
        mockMvc.perform(post("/manager/customer-pickup-codes")
                        .principal(new TestingAuthenticationToken("Manage", null))
                        .param("customerName", "  Linh Tran  ")
                        .param("customerEmail", "linh.tran@example.com")
                        .param("customerPhone", "+84 901 234 567")
                        .param("cargoType", "Small Cargo")
                        .param("pickupLocation", "A1")
                        .param("note", "Customer will arrive at gate 2."))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-customer-pickup-codes"))
                .andExpect(model().attributeExists("createdPickupCode", "successMessage"))
                .andExpect(content().string(containsString("Pickup code created. Email preview is ready.")))
                .andExpect(content().string(containsString("Smart Warehouse Pickup Code")))
                .andExpect(content().string(containsString("Your pickup code is: CUS-")))
                .andExpect(content().string(containsString("Mã nhận hàng Smart Warehouse")))
                .andExpect(content().string(containsString("Priority: High")))
                .andExpect(content().string(containsString("Độ ưu tiên: Cao")));

        CustomerPickupCode pickupCode = pickupCodeRepository.findAll().get(0);
        assertThat(pickupCode.getCode()).startsWith("CUS-");
        assertThat(pickupCode.getCode()).isEqualTo(pickupCode.getCode().toUpperCase(Locale.US));
        assertThat(pickupCode.getCustomerName()).isEqualTo("Linh Tran");
        assertThat(pickupCode.getCustomerEmail()).isEqualTo("linh.tran@example.com");
        assertThat(pickupCode.getCustomerPhone()).isEqualTo("0901234567");
        assertThat(pickupCode.getCargoType()).isEqualTo("Small Cargo");
        assertThat(pickupCode.getPickupLocation()).isEqualTo("A1");
        assertThat(pickupCode.getPriority()).isEqualTo(1);
        assertThat(pickupCode.getStatus()).isEqualTo(CustomerPickupCodeStatus.UNUSED);
        assertThat(pickupCode.getMissionId()).isNull();
        assertThat(pickupCode.getCreatedBy()).isEqualTo("Manage");
        assertThat(pickupCode.getCreatedAt()).isNotNull();
    }

    @Test
    void managerCreateRejectsInvalidEmail() throws Exception {
        assertCreateRejected("Valid Customer", "not-an-email", "0901234567", "Invalid customer email.");
    }

    @Test
    void managerCreateRejectsInvalidPhone() throws Exception {
        assertCreateRejected("Valid Customer", "customer@example.com", "1111111111", "Invalid customer phone number.");
    }

    @Test
    void managerCreateRejectsMissingCustomerName() throws Exception {
        assertCreateRejected("", "customer@example.com", "0901234567", "Customer name is required.");
    }

    @Test
    void generatedPickupCodesAreUniqueAndUppercase() {
        CustomerPickupCode firstCode = pickupCodeService.createPickupCode(validForm(
                "Customer One",
                "one@example.com",
                "0901234567",
                "Small Cargo",
                "A1"
        ), "Manage");
        CustomerPickupCode secondCode = pickupCodeService.createPickupCode(validForm(
                "Customer Two",
                "two@example.com",
                "0907654321",
                "Medium Cargo",
                "B2"
        ), "Manage");

        assertThat(firstCode.getCode()).isNotEqualTo(secondCode.getCode());
        assertThat(firstCode.getCode()).isEqualTo(firstCode.getCode().toUpperCase(Locale.US));
        assertThat(secondCode.getCode()).isEqualTo(secondCode.getCode().toUpperCase(Locale.US));
        assertThat(firstCode.getCode()).matches("CUS-\\d{4}-[A-Z2-9]{6}");
        assertThat(secondCode.getCode()).matches("CUS-\\d{4}-[A-Z2-9]{6}");
    }

    @Test
    void staffCanLookupUnusedPickupCodeAndSeeHighPriorityCargoDetails() throws Exception {
        CustomerPickupCode pickupCode = savePickupCode("Lookup Customer", "lookup@example.com", "0901234567");

        mockMvc.perform(post("/staff/pickup-request/customer-code/lookup")
                        .param("pickupCode", pickupCode.getCode().toLowerCase(Locale.US)))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("lookupPickupCode"))
                .andExpect(content().string(containsString("Lookup Customer")))
                .andExpect(content().string(containsString("lookup@example.com")))
                .andExpect(content().string(containsString("0901234567")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("A1")))
                .andExpect(content().string(containsString("Priority: High")))
                .andExpect(content().string(containsString("Process Pickup Code")));
    }

    @Test
    void staffCanProcessUnusedCodeIntoHighPriorityMissionThroughExistingPipeline() throws Exception {
        CustomerPickupCode pickupCode = savePickupCode("Process Customer", "process@example.com", "0901234567");

        mockMvc.perform(post("/staff/pickup-request/customer-code/process")
                        .principal(new TestingAuthenticationToken("Nova001", null))
                        .param("pickupCode", "  " + pickupCode.getCode().toLowerCase(Locale.US) + "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        containsString("through RuleEvaluator and StrategyContext")
                ));

        CustomerPickupCode usedCode = pickupCodeRepository.findByCodeIgnoreCase(pickupCode.getCode()).orElseThrow();
        assertThat(usedCode.getStatus()).isEqualTo(CustomerPickupCodeStatus.USED);
        assertThat(usedCode.getUsedBy()).isEqualTo("Nova001");
        assertThat(usedCode.getUsedAt()).isNotNull();
        assertThat(usedCode.getMissionId()).isNotNull();

        Mission mission = missionRepository.findById(usedCode.getMissionId()).orElseThrow();
        assertThat(mission.getRequestCode()).isEqualTo(pickupCode.getCode());
        assertThat(mission.getCustomerName()).isEqualTo("Process Customer");
        assertThat(mission.getCargoType()).isEqualTo("Small Cargo");
        assertThat(mission.getZone()).isEqualTo("Zone A");
        assertThat(mission.getLocationCode()).isEqualTo("A1");
        assertThat(mission.getPriority()).isEqualTo(1);
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(mission.getProcessedAt()).isNotNull();
        assertThat(mission.getMatchedRuleName()).isEqualTo("Urgent Task Fast Route Rule");
        assertThat(mission.getSelectedStrategyName()).isEqualTo("FastRouteStrategy");
        assertThat(mission.getDecisionSummary()).contains("StrategyContext");

        mockMvc.perform(get("/staff/missions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(pickupCode.getCode())))
                .andExpect(content().string(containsString("1 = High")))
                .andExpect(content().string(containsString("FastRouteStrategy")))
                .andExpect(content().string(containsString("Start Execution")));
    }

    @Test
    void usedCodeCannotCreateDuplicateMissionOrBeLookedUpAsUnused() throws Exception {
        CustomerPickupCode pickupCode = savePickupCode("Used Customer", "used@example.com", "0901234567");

        mockMvc.perform(post("/staff/pickup-request/customer-code/process")
                        .principal(new TestingAuthenticationToken("Nova001", null))
                        .param("pickupCode", pickupCode.getCode()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"));

        long missionCountAfterFirstUse = missionRepository.count();

        mockMvc.perform(post("/staff/pickup-request/customer-code/process")
                        .principal(new TestingAuthenticationToken("Nova001", null))
                        .param("pickupCode", pickupCode.getCode()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/pickup-request"))
                .andExpect(flash().attribute(
                        "pickupCodeErrorMessage",
                        CustomerPickupCodeService.PICKUP_CODE_USED_MESSAGE
                ));

        mockMvc.perform(post("/staff/pickup-request/customer-code/lookup")
                        .param("pickupCode", pickupCode.getCode()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CustomerPickupCodeService.PICKUP_CODE_USED_MESSAGE)))
                .andExpect(content().string(not(containsString("Process Pickup Code"))));

        assertThat(missionRepository.count()).isEqualTo(missionCountAfterFirstUse);
    }

    @Test
    void invalidPickupCodeShowsNotFoundMessageAndCreatesNoMission() throws Exception {
        mockMvc.perform(post("/staff/pickup-request/customer-code/lookup")
                        .param("pickupCode", "CUS-2026-NOPE99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CustomerPickupCodeService.PICKUP_CODE_NOT_FOUND_MESSAGE)));

        mockMvc.perform(post("/staff/pickup-request/customer-code/process")
                        .param("pickupCode", "CUS-2026-NOPE99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/pickup-request"))
                .andExpect(flash().attribute(
                        "pickupCodeErrorMessage",
                        CustomerPickupCodeService.PICKUP_CODE_NOT_FOUND_MESSAGE
                ));

        assertThat(missionRepository.count()).isZero();
    }

    @Test
    void pickupCodeCreatedMissionCanStartExecutionAndAppearInLiveMapState() throws Exception {
        CustomerPickupCode pickupCode = savePickupCode("Map Customer", "map@example.com", "0901234567");
        mockMvc.perform(post("/staff/pickup-request/customer-code/process")
                        .param("pickupCode", pickupCode.getCode()))
                .andExpect(status().is3xxRedirection());
        Mission mission = missionRepository.findAll().get(0);

        mockMvc.perform(post("/staff/missions/{id}/start-execution", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"));

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(pickupCode.getCode())))
                .andExpect(content().string(containsString("IN_PROGRESS")))
                .andExpect(content().string(containsString("A1")));
    }

    @Test
    void vietnamesePickupCodeTranslationKeysExist() throws Exception {
        String settingsScript = Files.readString(
                Path.of("src/main/resources/static/js/app-settings.js"),
                StandardCharsets.UTF_8
        );

        assertThat(settingsScript)
                .contains("Mã nhận hàng khách hàng")
                .contains("Mã nhận hàng")
                .contains("Tên khách hàng")
                .contains("Email khách hàng")
                .contains("Số điện thoại khách hàng")
                .contains("Xem trước email")
                .contains("Tạo mã nhận hàng")
                .contains("Trạng thái mã")
                .contains("Chưa sử dụng")
                .contains("Đã sử dụng")
                .contains("Người sử dụng")
                .contains("Thời gian sử dụng")
                .contains("Không tìm thấy mã nhận hàng.")
                .contains("Mã nhận hàng này đã được sử dụng.")
                .contains("Độ ưu tiên: Cao")
                .contains("Xử lý mã nhận hàng")
                .contains("Số điện thoại khách hàng không hợp lệ.")
                .contains("Email khách hàng không hợp lệ.")
                .contains("Vui lòng nhập tên khách hàng.")
                .contains("Vui lòng chọn loại hàng.")
                .contains("Vui lòng chọn vị trí lấy hàng.")
                .contains("Vui lòng nhập mã nhận hàng.");
    }

    private void assertCreateRejected(String customerName,
                                      String customerEmail,
                                      String customerPhone,
                                      String expectedMessage) throws Exception {
        mockMvc.perform(post("/manager/customer-pickup-codes")
                        .param("customerName", customerName)
                        .param("customerEmail", customerEmail)
                        .param("customerPhone", customerPhone)
                        .param("cargoType", "Small Cargo")
                        .param("pickupLocation", "A1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-customer-pickup-codes"))
                .andExpect(model().attributeExists("validationErrors"))
                .andExpect(content().string(containsString(expectedMessage)));

        assertThat(pickupCodeRepository.count()).isZero();
    }

    private CustomerPickupCode savePickupCode(String customerName, String customerEmail, String customerPhone) {
        return pickupCodeService.createPickupCode(validForm(
                customerName,
                customerEmail,
                customerPhone,
                "Small Cargo",
                "A1"
        ), "Manage");
    }

    private CustomerPickupCodeFormDto validForm(String customerName,
                                                String customerEmail,
                                                String customerPhone,
                                                String cargoType,
                                                String pickupLocation) {
        CustomerPickupCodeFormDto form = new CustomerPickupCodeFormDto();
        form.setCustomerName(customerName);
        form.setCustomerEmail(customerEmail);
        form.setCustomerPhone(customerPhone);
        form.setCargoType(cargoType);
        form.setPickupLocation(pickupLocation);
        form.setNote("Test pickup code");
        return form;
    }

    private void resetSeededRobots() {
        List<Robot> robots = robotRepository.findAllByOrderByIdAsc();
        robots.forEach(robot -> {
            clearChargingState(robot);
            if ("RB-100".equals(robot.getCode())) {
                robot.setStatus("IDLE");
                robot.setBattery(68);
                robot.setObstacleDetected(false);
            } else if ("RB-200".equals(robot.getCode())) {
                robot.setStatus("MOVING");
                robot.setBattery(15);
                robot.setObstacleDetected(true);
            } else if ("RB-300".equals(robot.getCode())) {
                robot.setStatus("LOADED");
                robot.setBattery(76);
                robot.setObstacleDetected(false);
            }
        });
        robotRepository.saveAll(robots);
    }

    private void clearChargingState(Robot robot) {
        robot.setChargingRequired(false);
        robot.setCharging(false);
        robot.setChargingStartedAt(null);
        robot.setChargingCompletedAt(null);
        robot.setBatteryBeforeCharging(null);
    }
}
