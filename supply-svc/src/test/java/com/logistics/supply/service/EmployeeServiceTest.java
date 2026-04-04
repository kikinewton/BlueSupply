package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDto;
import com.logistics.supply.exception.EmployeeNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock RoleRepository roleRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock PasswordEncoder bCryptPasswordEncoder;
    @Mock ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setEmail("jane@example.com");
        employee.setEnabled(true);
        employee.setRoles(List.of());
    }

    // ── Task 1: Critical Bugs ─────────────────────────────────────────────────

    @Test
    void deleteById_callsRepository() {
        employeeService.deleteById(1);
        verify(employeeRepository).deleteById(1);
    }

    @Test
    void count_returnsExactRepositoryValue() {
        when(employeeRepository.countAll()).thenReturn(5L);
        assertThat(employeeService.count()).isEqualTo(5L);
    }

    // ── Task 2: Duplicate Lookup Methods ─────────────────────────────────────

    @Test
    void findEmployeeById_delegatesToGetEmployeeById() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        Employee result = employeeService.findEmployeeById(1);
        assertThat(result).isSameAs(employee);
        verify(employeeRepository, times(1)).findById(1);
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getEmployeeById(99))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    // ── Task 3: Duplicate Save Methods ───────────────────────────────────────

    @Test
    void create_persistsAndReturnsEmployee() {
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee result = employeeService.create(employee);
        assertThat(result).isSameAs(employee);
        verify(employeeRepository).save(employee);
    }

    // ── Task 4: N+1 Queries — roleRepository never called ────────────────────

    @Test
    void getGeneralManager_singleQuery_roleRepositoryNotCalled() {
        when(employeeRepository.findManagerByRoleName("ROLE_GENERAL_MANAGER"))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getGeneralManager();

        assertThat(result).isSameAs(employee);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void getGeneralManager_throwsWhenNotFound() {
        when(employeeRepository.findManagerByRoleName("ROLE_GENERAL_MANAGER"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getGeneralManager())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getManagerByRoleName_singleQuery_roleRepositoryNotCalled() {
        when(employeeRepository.findManagerByRoleName("ROLE_PROCUREMENT"))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getManagerByRoleName("ROLE_PROCUREMENT");

        assertThat(result).isSameAs(employee);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void getDepartmentHOD_singleQuery_roleRepositoryNotCalled() {
        Department dept = new Department();
        dept.setId(2);
        dept.setName("IT");

        when(employeeRepository.findDepartmentHodByRoleName(2, "ROLE_HOD"))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getDepartmentHOD(dept);

        assertThat(result).isSameAs(employee);
        verifyNoInteractions(roleRepository);
    }

    // ── Task 5: sameRole Set comparison ──────────────────────────────────────

    @Test
    void update_noRoleChangeEvent_whenRolesUnchanged() {
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
        employee.setRoles(List.of(role));

        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        Role dtoRole = new Role();
        dtoRole.setId(1);
        dtoRole.setName("ROLE_USER");
        when(roleRepository.findById(1)).thenReturn(Optional.of(dtoRole));

        EmployeeDto dto = new EmployeeDto();
        dto.setRole(List.of(dtoRole));

        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.update(1, dto);

        // event publisher should never be called (no role change)
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void update_publishesRoleChangeEvent_whenRolesDiffer() throws InterruptedException {
        Role oldRole = new Role();
        oldRole.setId(1);
        oldRole.setName("ROLE_USER");
        employee.setRoles(List.of(oldRole));

        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        Role newRole = new Role();
        newRole.setId(2);
        newRole.setName("ROLE_ADMIN");
        when(roleRepository.findById(2)).thenReturn(Optional.of(newRole));

        EmployeeDto dto = new EmployeeDto();
        dto.setRole(List.of(newRole));

        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.update(1, dto);

        // Give the async CompletableFuture time to fire
        Thread.sleep(200);
        verify(applicationEventPublisher).publishEvent(any());
    }

    // ── Task 6: changePassword uses native query ──────────────────────────────

    @Test
    void changePassword_usesNativeQueryNotFullSave() {
        when(bCryptPasswordEncoder.encode("newpass")).thenReturn("encoded");
        when(employeeRepository.findByEmailAndEnabledIsTrue("jane@example.com"))
                .thenReturn(Optional.of(employee));

        employeeService.changePassword("newpass", "jane@example.com");

        verify(employeeRepository).updatePasswordByEmail("encoded", "jane@example.com");
        verify(employeeRepository, never()).save(any());
    }
}
