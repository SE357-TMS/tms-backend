package com.example.tms.service.impl;

import com.example.tms.dto.paging.PagedResponseDto;
import com.example.tms.dto.request.staff.AddStaffRequest;
import com.example.tms.dto.request.staff.StaffFilterRequest;
import com.example.tms.dto.request.staff.UpdateStaffRequest;
import com.example.tms.dto.response.staff.StaffDetailResponse;
import com.example.tms.dto.response.staff.StaffListResponse;
import com.example.tms.dto.response.staff.StaffStatisticsResponse;
import com.example.tms.enity.Cart;
import com.example.tms.enity.User;
import com.example.tms.repository.*;
import com.example.tms.service.interface_.EmailService;
import com.example.tms.service.interface_.StaffService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final TourBookingRepository tourBookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @PersistenceContext
    private EntityManager entityManager;

    private static final int MINIMUM_AGE = 18;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<StaffListResponse> getAllStaffs(StaffFilterRequest filter) {
        // Build specifications for filtering
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("role"), User.Role.STAFF);
        
        // Filter by keyword (username, full_name, email)
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("username")), keyword),
                cb.like(cb.lower(root.get("fullName")), keyword),
                cb.like(cb.lower(root.get("email")), keyword)
            ));
        }
        
        // Filter by phone number
        if (filter.getPhoneNumber() != null && !filter.getPhoneNumber().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("phoneNumber"), "%" + filter.getPhoneNumber() + "%"));
        }
        
        // Filter by lock status
        if (filter.getIsLock() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isLock"), filter.getIsLock()));
        }
        
        // Filter by gender
        if (filter.getGender() != null && !filter.getGender().trim().isEmpty()) {
            User.Gender gender = User.Gender.valueOf(filter.getGender());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("gender"), gender));
        }
        
        // Pagination and sorting
        Sort sort = Sort.by(
            filter.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
            filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        // Execute query
        Page<User> staffPage = userRepository.findAll(spec, pageable);
        
        // Map to response with statistics
        List<StaffListResponse> staffList = staffPage.getContent().stream()
            .map(this::mapToStaffListResponse)
            .toList();
        
        return PagedResponseDto.of(staffList, staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDetailResponse getStaffById(UUID staffId) {
        // Find staff
        User staff = userRepository.findById(staffId)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found or has been deleted"));
        
        // Get statistics
        StaffStatisticsResponse statistics = getStaffStatistics(staffId);
        
        // Get recent bookings
        List<StaffDetailResponse.RecentBookingResponse> recentBookings = getRecentBookings(staffId);
        
        return StaffDetailResponse.builder()
            .id(staff.getId())
            .username(staff.getUsername())
            .fullName(staff.getFullName())
            .email(staff.getEmail())
            .phoneNumber(staff.getPhoneNumber())
            .address(staff.getAddress())
            .birthday(staff.getBirthday())
            .gender(staff.getGender() != null ? staff.getGender().name() : null)
            .isLock(staff.getIsLock())
            .createdAt(staff.getCreatedAt())
            .updatedAt(staff.getUpdatedAt())
            .statistics(statistics)
            .recentBookings(recentBookings)
            .build();
    }

    @Override
    @Transactional
    public StaffDetailResponse addStaff(AddStaffRequest request) {
        log.info("Adding new staff with username: {}", request.getUsername());
        
        // Validate age (must be >= 18)
        validateAge(request.getBirthday());
        
        // Check username uniqueness
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists. Please choose another username");
        }
        
        // Check email uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered. Please use another email");
        }
        
        // Create staff user
        User staff = new User();
        staff.setUsername(request.getUsername());
        staff.setUserPassword(passwordEncoder.encode(request.getPassword()));
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setAddress(request.getAddress());
        staff.setBirthday(request.getBirthday());
        staff.setGender(request.getGender() != null ? User.Gender.valueOf(request.getGender()) : null);
        staff.setRole(User.Role.STAFF);
        staff.setIsLock(false);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());
        
        // Save staff
        staff = userRepository.save(staff);
        log.info("Staff created with ID: {}", staff.getId());
        
        // Create cart for staff
        Cart cart = new Cart();
        cart.setUser(staff);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        log.info("Cart created for staff ID: {}", staff.getId());
        
        // Send welcome email
        try {
            sendStaffWelcomeEmail(staff, request.getPassword());
        } catch (Exception e) {
            log.error("Failed to send welcome email to staff: {}", staff.getEmail(), e);
            // Don't fail the transaction, just log the error
        }
        
        return getStaffById(staff.getId());
    }

    @Override
    @Transactional
    public StaffDetailResponse updateStaff(UUID staffId, UpdateStaffRequest request) {
        log.info("Updating staff with ID: {}", staffId);
        
        // Find staff
        User staff = userRepository.findById(staffId)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found or has been deleted"));
        
        // Validate age
        validateAge(request.getBirthday());
        
        // Check email uniqueness (excluding current staff)
        userRepository.findByEmail(request.getEmail())
            .filter(u -> !u.getId().equals(staffId))
            .ifPresent(u -> {
                throw new RuntimeException("Email is already registered by another account");
            });
        
        boolean emailChanged = !staff.getEmail().equals(request.getEmail());
        boolean passwordChanged = request.getPassword() != null && !request.getPassword().trim().isEmpty();
        
        // Update staff information
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setAddress(request.getAddress());
        staff.setBirthday(request.getBirthday());
        staff.setGender(request.getGender() != null ? User.Gender.valueOf(request.getGender()) : null);
        
        // Update password if provided
        if (passwordChanged) {
            staff.setUserPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        staff.setUpdatedAt(LocalDateTime.now());
        staff = userRepository.save(staff);
        
        // Send notification email if email or password changed
        if (emailChanged || passwordChanged) {
            try {
                sendStaffUpdateNotificationEmail(staff, emailChanged, passwordChanged);
            } catch (Exception e) {
                log.error("Failed to send update notification to staff: {}", staff.getEmail(), e);
            }
        }
        
        log.info("Staff updated successfully: {}", staffId);
        return getStaffById(staffId);
    }

    @Override
    @Transactional
    public void toggleLockStaff(UUID staffId) {
        log.info("Toggling lock status for staff: {}", staffId);
        
        User staff = userRepository.findById(staffId)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        staff.setIsLock(!staff.getIsLock());
        staff.setUpdatedAt(LocalDateTime.now());
        userRepository.save(staff);
        
        log.info("Staff {} {} successfully", staffId, staff.getIsLock() ? "locked" : "unlocked");
    }

    @Override
    @Transactional
    public void deleteStaffPermanently(UUID staffId) {
        log.info("Attempting to permanently delete staff: {}", staffId);
        
        // Find staff
        User staff = userRepository.findById(staffId)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Check for pending/confirmed bookings
        long pendingBookings = tourBookingRepository.countByUserIdAndStatusIn(
            staffId, 
            List.of("PENDING", "CONFIRMED")
        );
        
        if (pendingBookings > 0) {
            throw new RuntimeException(
                "Cannot delete staff with " + pendingBookings + " pending/confirmed bookings. " +
                "Please reassign the work or lock the account instead."
            );
        }
        
        // Delete cascade: Cart_Item -> Cart -> Favorite_Tour -> User
        // Note: Cart_Item and Favorite_Tour will be deleted by cascade in JPA
        
        // Delete staff (cascade will handle related entities)
        userRepository.delete(staff);
        
        log.info("Staff {} deleted permanently", staffId);
    }

    // ==================== Private Helper Methods ====================

    private StaffListResponse mapToStaffListResponse(User staff) {
        Long totalBookings = tourBookingRepository.countByUserId(staff.getId());
        
        return StaffListResponse.builder()
            .id(staff.getId())
            .username(staff.getUsername())
            .fullName(staff.getFullName())
            .email(staff.getEmail())
            .phoneNumber(staff.getPhoneNumber())
            .address(staff.getAddress())
            .birthday(staff.getBirthday())
            .gender(staff.getGender() != null ? staff.getGender().name() : null)
            .isLock(staff.getIsLock())
            .createdAt(staff.getCreatedAt())
            .updatedAt(staff.getUpdatedAt())
            .totalManagedBookings(totalBookings)
            .build();
    }

    private StaffStatisticsResponse getStaffStatistics(UUID staffId) {
        // Note: Adjust queries based on your actual schema
        // These are example queries - modify according to your business logic
        
        Long totalBookings = tourBookingRepository.countByUserId(staffId);
        
        // TODO: Implement these queries based on your schema
        // Long totalTrips = tripRepository.countByCreatedByUserId(staffId);
        // Long totalRoutes = routeRepository.countByCreatedByUserId(staffId);
        
        return StaffStatisticsResponse.builder()
            .totalBookingsHandled(totalBookings)
            .totalTripsCreated(0L) // TODO: Implement
            .totalRoutesCreated(0L) // TODO: Implement
            .build();
    }

    private List<StaffDetailResponse.RecentBookingResponse> getRecentBookings(UUID staffId) {
        // Get 10 most recent bookings handled by this staff
        String jpql = """
            SELECT 
                tb.id, 
                r.name, 
                tb.createdAt, 
                tb.status, 
                tb.totalPrice,
                (SELECT COUNT(bt) FROM BookingTraveler bt WHERE bt.tourBooking.id = tb.id)
            FROM TourBooking tb
            JOIN tb.trip t
            JOIN t.route r
            WHERE tb.user.id = :staffId
            ORDER BY tb.createdAt DESC
            """;
        
        Query query = entityManager.createQuery(jpql);
        query.setParameter("staffId", staffId);
        query.setMaxResults(10);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        List<StaffDetailResponse.RecentBookingResponse> recentBookings = new ArrayList<>();
        for (Object[] row : results) {
            recentBookings.add(StaffDetailResponse.RecentBookingResponse.builder()
                .bookingId((UUID) row[0])
                .routeName((String) row[1])
                .bookingDate((LocalDateTime) row[2])
                .status((String) row[3])
                .totalPrice((Double) row[4])
                .numberOfTravelers(((Long) row[5]).intValue())
                .build());
        }
        
        return recentBookings;
    }

    private void validateAge(LocalDate birthday) {
        if (birthday == null) {
            throw new RuntimeException("Birthday is required");
        }
        
        int age = Period.between(birthday, LocalDate.now()).getYears();
        if (age < MINIMUM_AGE) {
            throw new RuntimeException("Staff must be at least " + MINIMUM_AGE + " years old");
        }
    }

    private void sendStaffWelcomeEmail(User staff, String temporaryPassword) {
        // TODO: Implement welcome email template
        log.info("Sending welcome email to staff: {}", staff.getEmail());
        // emailService.sendStaffWelcomeEmail(staff.getEmail(), staff.getFullName(), staff.getUsername(), temporaryPassword);
    }

    private void sendStaffUpdateNotificationEmail(User staff, boolean emailChanged, boolean passwordChanged) {
        // TODO: Implement update notification email
        log.info("Sending update notification to staff: {} (emailChanged={}, passwordChanged={})", 
            staff.getEmail(), emailChanged, passwordChanged);
    }
}
