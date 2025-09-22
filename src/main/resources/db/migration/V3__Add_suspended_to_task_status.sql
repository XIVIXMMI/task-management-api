-- V3__Add_suspended_to_task_status.sql
-- Migration to add 'suspended' value to task_status enum
-- Note: This migration runs outside a transaction (see .conf file)

ALTER TYPE project.task_status ADD VALUE 'suspended';