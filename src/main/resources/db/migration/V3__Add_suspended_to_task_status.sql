-- V3__add_suspended_to_task_status.sql
-- Migration to add 'suspended' value to task_status enum

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_status' AND t.typtype = 'e' AND n.nspname = 'project'
    ) THEN
        RAISE EXCEPTION 'Enum type project.task_status does not exist';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_status' AND n.nspname = 'project'
        AND e.enumlabel = 'suspended'
    ) THEN
        ALTER TYPE project.task_status ADD VALUE 'suspended';
        RAISE NOTICE 'Successfully added suspended value to project.task_status enum';
    ELSE
        RAISE NOTICE 'suspended value already exists in project.task_status enum';
    END IF;
END $$;