package com.logistics.supply.event;

import com.logistics.supply.model.Employee;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleChangeEvent extends ApplicationEvent {

    private final Employee employee;
    private final boolean roleChanged;



    public RoleChangeEvent(Object source, Employee employee, boolean roleChanged) {
        super(source);
        this.employee = employee;
        this.roleChanged = roleChanged;
    }
}
