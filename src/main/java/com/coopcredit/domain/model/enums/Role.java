package com.coopcredit.domain.model.enums;

/**
 * Represents the different roles within the CoopCredit system.
 * <p>
 * This enumeration defines the authorization levels and permissions
 * for users interacting with the credit application system.
 * </p>
 *
 * <h2>Role Descriptions:</h2>
 * <ul>
 *   <li><b>ROLE_AFFILIATE:</b> Standard user who can submit and view their own credit applications</li>
 *   <li><b>ROLE_ANALYST:</b> Credit analyst who can evaluate pending applications</li>
 *   <li><b>ROLE_ADMIN:</b> System administrator with full access to all operations</li>
 * </ul>
 */
public enum Role {
    
    /**
     * Affiliate role for standard users.
     * <p>
     * Permissions:
     * <ul>
     *   <li>Create credit applications</li>
     *   <li>View own applications</li>
     *   <li>Update personal information</li>
     * </ul>
     * </p>
     */
    ROLE_AFFILIATE,
    
    /**
     * Analyst role for credit evaluators.
     * <p>
     * Permissions:
     * <ul>
     *   <li>View all pending applications</li>
     *   <li>Evaluate credit applications</li>
     *   <li>Approve or reject applications</li>
     * </ul>
     * </p>
     */
    ROLE_ANALYST,
    
    /**
     * Administrator role with full system access.
     * <p>
     * Permissions:
     * <ul>
     *   <li>Full CRUD operations on all entities</li>
     *   <li>User management</li>
     *   <li>System configuration</li>
     *   <li>Access to all reports and metrics</li>
     * </ul>
     * </p>
     */
    ROLE_ADMIN
}