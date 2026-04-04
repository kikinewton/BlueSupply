package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CommentResponseTest {

    private static Employee employee() {
        Department dept = new Department();
        dept.setId(1);
        dept.setName("Finance");
        dept.setDescription("Finance dept");
        Employee emp = new Employee();
        emp.setId(10);
        emp.setFirstName("Alice");
        emp.setLastName("Smith");
        emp.setEmail("alice@example.com");
        emp.setPhoneNo("0244000001");
        emp.setDepartment(dept);
        emp.setRoles(List.of(new Role("ROLE_EMPLOYEE")));
        return emp;
    }

    private static RequestItemComment comment(long id, String description, RequestProcess process) {
        RequestItem item = new RequestItem();
        item.setId(5);
        item.setName("Pen");
        item.setQuantity(1);
        item.setSuppliers(Set.of());
        return RequestItemComment.builder()
                .id(id)
                .description(description)
                .processWithComment(process)
                .employee(employee())
                .requestItem(item)
                .build();
    }

    @Test
    void from_mapsId() {
        RequestItemComment c = comment(42L, "desc", RequestProcess.GRN_STORES);
        CommentResponse<RequestItemDto> r = CommentResponse.from(
                c, EmployeeMinorDto.toDto(c.getEmployee()), RequestItemDto.toDto(c.getRequestItem()));
        assertThat(r.getId()).isEqualTo(42L);
    }

    @Test
    void from_mapsDescription() {
        RequestItemComment c = comment(1L, "approve this", RequestProcess.GRN_STORES);
        CommentResponse<RequestItemDto> r = CommentResponse.from(
                c, EmployeeMinorDto.toDto(c.getEmployee()), RequestItemDto.toDto(c.getRequestItem()));
        assertThat(r.getDescription()).isEqualTo("approve this");
    }

    @Test
    void from_mapsProcessWithComment() {
        RequestItemComment c = comment(1L, "d", RequestProcess.REVIEW_GRN_HOD);
        CommentResponse<RequestItemDto> r = CommentResponse.from(
                c, EmployeeMinorDto.toDto(c.getEmployee()), RequestItemDto.toDto(c.getRequestItem()));
        assertThat(r.getProcessWithComment()).isEqualTo(RequestProcess.REVIEW_GRN_HOD);
    }

    @Test
    void from_mapsCommentBy() {
        RequestItemComment c = comment(1L, "d", RequestProcess.GRN_STORES);
        EmployeeMinorDto employeeDto = EmployeeMinorDto.toDto(c.getEmployee());
        CommentResponse<RequestItemDto> r = CommentResponse.from(
                c, employeeDto, RequestItemDto.toDto(c.getRequestItem()));
        assertThat(r.getCommentBy()).isSameAs(employeeDto);
        assertThat(r.getCommentBy().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void from_mapsItem() {
        RequestItemComment c = comment(1L, "d", RequestProcess.GRN_STORES);
        RequestItemDto itemDto = RequestItemDto.toDto(c.getRequestItem());
        CommentResponse<RequestItemDto> r = CommentResponse.from(
                c, EmployeeMinorDto.toDto(c.getEmployee()), itemDto);
        assertThat(r.getItem()).isSameAs(itemDto);
        assertThat(r.getItem().getName()).isEqualTo("Pen");
    }

    @Test
    void from_streamPatternMapsAllEntries() {
        RequestItemComment c1 = comment(1L, "first", RequestProcess.GRN_STORES);
        RequestItemComment c2 = comment(2L, "second", RequestProcess.GRN_STORES);
        List<CommentResponse<RequestItemDto>> results = Stream.of(c1, c2)
                .map(c -> CommentResponse.from(
                        c,
                        EmployeeMinorDto.toDto(c.getEmployee()),
                        RequestItemDto.toDto(c.getRequestItem())))
                .toList();
        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(r -> {
            assertThat(r.getCommentBy()).isNotNull();
            assertThat(r.getItem()).isNotNull();
        });
    }
}
