package com.example.quanlytourdl;

import java.util.List;

public class EmployeePermission {
    private String employeeName;
    private String employeeRole;
    private List<Permission> permissions;

    public EmployeePermission(String employeeName, String employeeRole, List<Permission> permissions) {
        this.employeeName = employeeName;
        this.employeeRole = employeeRole;
        this.permissions = permissions;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(String employeeRole) {
        this.employeeRole = employeeRole;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}