-- V4__Clean_schema_inconsistencies.sql
-- Simplified migration that only removes orphaned enums if safe to do so

DO $$
BEGIN
    RAISE NOTICE 'Starting V4 migration: Schema inconsistency cleanup';

    -- Try to drop public.taskstatus if it exists and has no dependencies
    BEGIN
        IF EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'taskstatus' AND n.nspname = 'public') THEN
            DROP TYPE IF EXISTS public.taskstatus;
            RAISE NOTICE 'Removed enum: public.taskstatus';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Could not remove public.taskstatus: %', SQLERRM;
    END;

    -- Try to drop public.taskpriority if it exists and has no dependencies
    BEGIN
        IF EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'taskpriority' AND n.nspname = 'public') THEN
            DROP TYPE IF EXISTS public.taskpriority;
            RAISE NOTICE 'Removed enum: public.taskpriority';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Could not remove public.taskpriority: %', SQLERRM;
    END;

    -- Try to drop public.task_status if it exists
    BEGIN
        IF EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'task_status' AND n.nspname = 'public') THEN
            DROP TYPE IF EXISTS public.task_status;
            RAISE NOTICE 'Removed enum: public.task_status';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Could not remove public.task_status: %', SQLERRM;
    END;

    -- Try to drop public.task_priority if it exists
    BEGIN
        IF EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'task_priority' AND n.nspname = 'public') THEN
            DROP TYPE IF EXISTS public.task_priority;
            RAISE NOTICE 'Removed enum: public.task_priority';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Could not remove public.task_priority: %', SQLERRM;
    END;

    -- Verify project schema enums exist
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'task_status' AND n.nspname = 'project') THEN
        RAISE EXCEPTION 'Critical: project.task_status enum does not exist!';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid
                   WHERE t.typname = 'task_priority' AND n.nspname = 'project') THEN
        RAISE EXCEPTION 'Critical: project.task_priority enum does not exist!';
    END IF;

    RAISE NOTICE 'V4 migration completed';
END $$;
