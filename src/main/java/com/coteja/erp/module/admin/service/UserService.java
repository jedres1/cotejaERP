package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.dto.UserRequest;
import com.coteja.erp.module.admin.dto.UserResponse;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.admin.entity.User;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.CustomerRepository;
import com.coteja.erp.module.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(
                userRepository.findAll(pageable).map(UserResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado: " + request.email());
        }
        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findByIdAndDeletedAtIsNull(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        }
        String encodedPassword = request.password() != null
                ? passwordEncoder.encode(request.password())
                : passwordEncoder.encode("Coteja2024!");

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(encodedPassword)
                .role(request.role())
                .active(request.isActive() != null ? request.isActive() : true)
                .customer(customer)
                .moduleAccesses(request.moduleAccesses())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        if (request.isActive() != null) user.setActive(request.isActive());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.moduleAccesses() != null) user.setModuleAccesses(request.moduleAccesses());
        if (request.customerId() != null) {
            Customer customer = customerRepository.findByIdAndDeletedAtIsNull(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
            user.setCustomer(customer);
        }
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }

    public void assignCompanies(Long userId, List<Long> companyIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<Company> companies = companyRepository.findAllById(companyIds);
        user.getCompanies().clear();
        user.getCompanies().addAll(companies);
        companies.forEach(c -> {
            if (!c.getUsers().contains(user)) c.getUsers().add(user);
        });
        userRepository.save(user);
    }
}
