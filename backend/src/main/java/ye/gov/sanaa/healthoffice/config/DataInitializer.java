package ye.gov.sanaa.healthoffice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ye.gov.sanaa.healthoffice.entity.Admin;
import ye.gov.sanaa.healthoffice.entity.Role;
import ye.gov.sanaa.healthoffice.repository.AdminRepository;
import ye.gov.sanaa.healthoffice.repository.RoleRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final ye.gov.sanaa.healthoffice.repository.FacilityRepository facilityRepository;
    private final ye.gov.sanaa.healthoffice.repository.FacilityUserRepository facilityUserRepository;
    private final ye.gov.sanaa.healthoffice.repository.SystemSettingRepository systemSettingRepository;
    private final ye.gov.sanaa.healthoffice.repository.InspectionTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Ensure Admin Role exists
            Role adminRole = roleRepository.findByCode("ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ADMIN");
                r.setNameAr("مدير النظام");
                r.setNameEn("System Administrator");
                return roleRepository.save(r);
            });

            roleRepository.findByCode("INSPECTOR").orElseGet(() -> {
                Role r = new Role();
                r.setCode("INSPECTOR");
                r.setNameAr("مفتش");
                r.setNameEn("Inspector");
                return roleRepository.save(r);
            });

            roleRepository.findByCode("REVIEWER").orElseGet(() -> {
                Role r = new Role();
                r.setCode("REVIEWER");
                r.setNameAr("مراجع");
                r.setNameEn("Reviewer");
                return roleRepository.save(r);
            });

            roleRepository.findByCode("FINANCE").orElseGet(() -> {
                Role r = new Role();
                r.setCode("FINANCE");
                r.setNameAr("مالية");
                r.setNameEn("Finance");
                return roleRepository.save(r);
            });

            roleRepository.findByCode("MEDIA").orElseGet(() -> {
                Role r = new Role();
                r.setCode("MEDIA");
                r.setNameAr("إعلام");
                r.setNameEn("Media");
                return roleRepository.save(r);
            });

            roleRepository.findByCode("VIOLATION").orElseGet(() -> {
                Role r = new Role();
                r.setCode("VIOLATION");
                r.setNameAr("شؤون المخالفات");
                r.setNameEn("Violations Department");
                return roleRepository.save(r);
            });

            // Ensure Admin User exists
            if (adminRepository.findByUsername("admin").isEmpty()) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("password"));
                admin.setFullName("System Administrator");
                admin.getRoles().add(adminRole);
                admin.setEnabled(true);
                adminRepository.save(admin);
                System.out.println("Default Admin created: username=admin, password=password");
            }

            // Ensure Default Facility and User exist
            if (facilityRepository.count() == 0) {
                ye.gov.sanaa.healthoffice.entity.Facility facility = new ye.gov.sanaa.healthoffice.entity.Facility();
                facility.setFacilityCode("FAC-001");
                facility.setNameAr("مستشفى العاصمة النموذجي");
                facility.setNameEn("Capital Model Hospital");
                facility.setFacilityType("HOSPITAL");
                facility.setLicenseType("NEW");
                facility.setIsActive(true);
                facility = facilityRepository.save(facility);

                ye.gov.sanaa.healthoffice.entity.FacilityUser user = new ye.gov.sanaa.healthoffice.entity.FacilityUser();
                user.setFirstName("أحمد");
                user.setMiddleName("محمد");
                user.setLastName("العزي");
                user.setPhoneNumber("777777777");
                user.setPasswordHash(passwordEncoder.encode("password"));
                user.setUserType("OWNER");
                user.setFacility(facility);
                user.setIsActive(true);
                facilityUserRepository.save(user);

                // إضافة بيانات تجريبية إضافية
                ye.gov.sanaa.healthoffice.entity.Facility facility2 = new ye.gov.sanaa.healthoffice.entity.Facility();
                facility2.setFacilityCode("FAC-002");
                facility2.setNameAr("مجمع الثورة الطبي");
                facility2.setNameEn("Al-Thawra Medical Complex");
                facility2.setFacilityType("CLINIC");
                facility2.setLicenseType("RENEW");
                facility2.setIsActive(true);
                facilityRepository.save(facility2);

                ye.gov.sanaa.healthoffice.entity.Facility facility3 = new ye.gov.sanaa.healthoffice.entity.Facility();
                facility3.setFacilityCode("FAC-003");
                facility3.setNameAr("صيدلية الشفاء");
                facility3.setNameEn("Al-Shifa Pharmacy");
                facility3.setFacilityType("PHARMACY");
                facility3.setLicenseType("NEW");
                facility3.setIsActive(true);
                facilityRepository.save(facility3);

                System.out.println("Default Seed Data created");
            }

            // Ensure Default Settings exist
            if (systemSettingRepository.count() == 0) {
                systemSettingRepository.save(ye.gov.sanaa.healthoffice.entity.SystemSetting.builder()
                        .category("FEES").settingKey("LICENSE_FEE_HOSPITAL").settingValue("150000.00").build());
                systemSettingRepository.save(ye.gov.sanaa.healthoffice.entity.SystemSetting.builder()
                        .category("FEES").settingKey("LICENSE_FEE_CLINIC").settingValue("50000.00").build());
                systemSettingRepository.save(ye.gov.sanaa.healthoffice.entity.SystemSetting.builder()
                        .category("FEES").settingKey("LICENSE_FEE_PHARMACY").settingValue("75000.00").build());
                System.out.println("Default System Settings initialized");
            }

            // Ensure Inspection Template for Hospitals exists
            if (templateRepository.findByFacilityType("HOSPITAL").isEmpty()) {
                var template = ye.gov.sanaa.healthoffice.entity.InspectionTemplate.builder()
                        .name("نموذج تفتيش المستشفيات")
                        .facilityType("HOSPITAL")
                        .items(new java.util.ArrayList<>())
                        .build();

                template.getItems().add(ye.gov.sanaa.healthoffice.entity.InspectionTemplateItem.builder()
                        .template(template)
                        .criterionCode("H-001")
                        .description("توفر أجهزة التعقيم المركزية")
                        .maxScore(new java.math.BigDecimal("10.00"))
                        .itemOrder(1)
                        .build());

                template.getItems().add(ye.gov.sanaa.healthoffice.entity.InspectionTemplateItem.builder()
                        .template(template)
                        .criterionCode("H-002")
                        .description("كفاءة الطاقم الطبي المناوب")
                        .maxScore(new java.math.BigDecimal("20.00"))
                        .itemOrder(2)
                        .build());

                template.getItems().add(ye.gov.sanaa.healthoffice.entity.InspectionTemplateItem.builder()
                        .template(template)
                        .criterionCode("H-003")
                        .description("الالتزام بمعايير السلامة المهنية")
                        .maxScore(new java.math.BigDecimal("15.00"))
                        .itemOrder(3)
                        .build());

                templateRepository.save(template);
                System.out.println("Default Inspection Template for HOSPITAL created");
            }
        };
    }
}
