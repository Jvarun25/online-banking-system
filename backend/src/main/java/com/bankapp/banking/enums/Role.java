package com.bankapp.banking.enums;

/**
 * User roles for Role-Based Access Control (RBAC).
 * ROLE_USER  -> normal customer: can manage own accounts, transfer funds, view own history
 * ROLE_ADMIN -> bank staff/admin: can view all users, all accounts, freeze/unfreeze accounts
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
