package ye.gov.sanaa.healthoffice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.sanaa.healthoffice.dto.InspectionDto;
import ye.gov.sanaa.healthoffice.entity.*;
import ye.gov.sanaa.healthoffice.repository.*;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class InspectionService {

        private final InspectionRepository inspectionRepository;
        private final ApplicationRepository applicationRepository;
        private final AdminRepository adminRepository;
        private final NotificationService notificationService;
        private final AuditService auditService;
        private final InspectionTemplateRepository templateRepository;
        private final InspectionScoreRepository scoreRepository; // Add this repository

        @Transactional
        public InspectionDto scheduleInspection(Long applicationId, Long inspectorId, OffsetDateTime scheduledDate) {
                Application app = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                if (!"INSPECTION_SCHEDULED".equals(app.getStatus()) && !"UNDER_REVIEW".equals(app.getStatus())
                                && !"BLUEPRINT_REVIEW".equals(app.getStatus())) {
                        throw new RuntimeException("Application not ready for inspection scheduling");
                }

                Admin inspector = adminRepository.findById(inspectorId)
                                .orElseThrow(() -> new RuntimeException("Inspector not found"));

                Inspection inspection = Inspection.builder()
                                .application(app)
                                .scheduledDate(scheduledDate)
                                .inspector(inspector)
                                .status("SCHEDULED")
                                .build();
                inspection = inspectionRepository.save(inspection);

                // Dynamic Template Logic
                java.util.Optional<InspectionTemplate> template = templateRepository
                                .findByFacilityType(app.getFacilityType());
                if (template.isPresent()) {
                        for (InspectionTemplateItem item : template.get().getItems()) {
                                InspectionScore score = InspectionScore.builder()
                                                .inspection(inspection)
                                                .criterionCode(item.getCriterionCode())
                                                .description(item.getDescription())
                                                .maxScore(item.getMaxScore())
                                                .score(null) // Pending
                                                .build();
                                scoreRepository.save(score);
                        }
                }

                app.setStatus("INSPECTION_SCHEDULED");
                applicationRepository.save(app);

                auditService.log(inspectorId, null, "SCHEDULE_INSPECTION", "INSPECTION", inspection.getId(),
                                "Inspection scheduled for application: " + app.getApplicationNumber());

                notificationService.notifyUser(app.getSubmittedByUser().getId(),
                                "تم تحديد موعد التفتيش", "Inspection Scheduled",
                                "الموعد: " + scheduledDate.toLocalDate(), "Date: " + scheduledDate.toLocalDate(),
                                "INFO");

                return toDto(inspection);
        }

        @Transactional
        public InspectionDto completeInspection(Long inspectionId, InspectionDto dto) {
                Inspection inspection = inspectionRepository.findById(inspectionId)
                                .orElseThrow(() -> new RuntimeException("Inspection not found"));

                inspection.setActualVisitDate(OffsetDateTime.now());
                inspection.setStatus("COMPLETED");
                inspection.setOverallScore(dto.getOverallScore());
                inspection.setNotes(dto.getNotes());
                inspectionRepository.save(inspection);

                // Update Item Scores
                if (dto.getItems() != null) {
                        for (var itemDto : dto.getItems()) {
                                if (itemDto.getId() != null) {
                                        java.util.Optional<InspectionScore> scoreOpt = scoreRepository
                                                        .findById(itemDto.getId());
                                        if (scoreOpt.isPresent()) {
                                                InspectionScore score = scoreOpt.get();
                                                score.setScore(itemDto.getScore());
                                                scoreRepository.save(score);
                                        }
                                }
                        }
                }

                Application app = inspection.getApplication();
                app.setStatus("INSPECTION_COMPLETED");
                applicationRepository.save(app);

                Long inspectorId = inspection.getInspector() != null ? inspection.getInspector().getId() : null;
                auditService.log(inspectorId, null, "COMPLETE_INSPECTION", "INSPECTION", inspectionId,
                                "Inspection completed, score: " + dto.getOverallScore());

                return toDto(inspection);
        }

        @Transactional(readOnly = true)
        public Page<InspectionDto> getByInspector(Long inspectorId, Pageable pageable) {
                return inspectionRepository.findByInspectorId(inspectorId, pageable).map(this::toDto);
        }

        @Transactional(readOnly = true)
        public Page<InspectionDto> getByStatus(String status, Pageable pageable) {
                return inspectionRepository.findByStatus(status, pageable).map(this::toDto);
        }

        @Transactional
        public InspectionDto getById(Long id) {
                Inspection inspection = inspectionRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Inspection not found"));

                // Self-healing: If no scores exist, try to seed from template
                if (scoreRepository.findByInspectionId(id).isEmpty()) {
                        java.util.Optional<InspectionTemplate> template = templateRepository
                                        .findByFacilityType(inspection.getApplication().getFacilityType());
                        if (template.isPresent()) {
                                for (InspectionTemplateItem item : template.get().getItems()) {
                                        InspectionScore score = InspectionScore.builder()
                                                        .inspection(inspection)
                                                        .criterionCode(item.getCriterionCode())
                                                        .description(item.getDescription())
                                                        .maxScore(item.getMaxScore())
                                                        .score(null)
                                                        .build();
                                        scoreRepository.save(score);
                                }
                        }
                }
                return toDto(inspection);
        }

        @Transactional(readOnly = true)
        public InspectionDto getActiveForApplication(Long applicationId) {
                return inspectionRepository.findByApplicationId(applicationId).stream()
                                .filter(i -> "SCHEDULED".equals(i.getStatus()))
                                .findFirst()
                                .map(this::toDto)
                                .orElseThrow(() -> new RuntimeException(
                                                "No active (scheduled) inspection found for this application"));
        }

        @Transactional(readOnly = true)
        public java.util.List<ye.gov.sanaa.healthoffice.dto.AdminDto> getInspectors() {
                return adminRepository.findAll().stream()
                                .map(this::toAdminDto)
                                .collect(java.util.stream.Collectors.toList());
        }

        private ye.gov.sanaa.healthoffice.dto.AdminDto toAdminDto(Admin admin) {
                return ye.gov.sanaa.healthoffice.dto.AdminDto.builder()
                                .id(admin.getId())
                                .username(admin.getUsername())
                                .fullName(admin.getFullName())
                                .roles(admin.getRoles().stream().map(r -> r.getCode()).collect(java.util.stream.Collectors.toSet()))
                                .build();
        }

        private InspectionDto toDto(Inspection i) {
                java.util.List<ye.gov.sanaa.healthoffice.dto.InspectionScoreDto> items = scoreRepository
                                .findByInspectionId(i.getId()).stream() // Assuming this method exists
                                .map(s -> {
                                        ye.gov.sanaa.healthoffice.dto.InspectionScoreDto d = new ye.gov.sanaa.healthoffice.dto.InspectionScoreDto();
                                        d.setId(s.getId());
                                        d.setCriterionCode(s.getCriterionCode());
                                        d.setDescription(s.getDescription());
                                        d.setScore(s.getScore());
                                        d.setMaxScore(s.getMaxScore());
                                        return d;
                                })
                                .collect(java.util.stream.Collectors.toList());

                return InspectionDto.builder()
                                .id(i.getId())
                                .applicationId(i.getApplication().getId())
                                .applicationNumber(i.getApplication().getApplicationNumber())
                                .scheduledDate(i.getScheduledDate())
                                .actualVisitDate(i.getActualVisitDate())
                                .inspectorId(i.getInspector() != null ? i.getInspector().getId() : null)
                                .inspectorName(i.getInspector() != null ? i.getInspector().getFullName() : null)
                                .status(i.getStatus())
                                .overallScore(i.getOverallScore())
                                .notes(i.getNotes())
                                .items(items)
                                .build();
        }
}
