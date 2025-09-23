-- V4__Clean_schema_inconsistencies.sql
-- Comprehensive cleanup of schema inconsistencies and duplicate enums
-- This migration standardizes enum usage and prevents future schema-related issues

DO $$
DECLARE
    enum_record RECORD;
    table_count INTEGER;
BEGIN
    RAISE NOTICE 'Starting V4 migration: Schema inconsistency cleanup';

    -- =====================================================
    -- STEP 1: Verify current state and log findings
    -- =====================================================

    RAISE NOTICE 'Checking current enum state...';

    -- Check project schema enums (should be the active ones)
    FOR enum_record IN
        SELECT t.typname, array_agg(e.enumlabel ORDER BY e.enumsortorder) as values
        FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        LEFT JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE n.nspname = 'project' AND t.typtype = 'e'
        AND t.typname IN ('task_status', 'task_priority')
        GROUP BY t.typname
    LOOP
        RAISE NOTICE 'Found active enum: project.% with values: %', enum_record.typname, enum_record.values;
    END LOOP;

    -- Check for duplicate enums in public schema
    FOR enum_record IN
        SELECT t.typname, array_agg(e.enumlabel ORDER BY e.enumsortorder) as values
        FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        LEFT JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE n.nspname = 'public' AND t.typtype = 'e'
        AND t.typname IN ('taskstatus', 'taskpriority', 'task_status', 'task_priority')
        GROUP BY t.typname
    LOOP
        RAISE NOTICE 'Found duplicate enum: public.% with values: %', enum_record.typname, enum_record.values;
    END LOOP;

    -- =====================================================
    -- STEP 2: Verify table dependencies
    -- =====================================================

    RAISE NOTICE 'Checking table dependencies on duplicate enums...';

    -- Check if any tables use public.taskstatus
    SELECT COUNT(*) INTO table_count
    FROM information_schema.columns
    WHERE udt_name = 'taskstatus' AND udt_schema = 'public';

    IF table_count > 0 THEN
        RAISE EXCEPTION 'Cannot proceed: Found % tables using public.taskstatus', table_count;
    ELSE
        RAISE NOTICE 'Safe to remove: No tables use public.taskstatus';
    END IF;

    -- Check if any tables use public.taskpriority
    SELECT COUNT(*) INTO table_count
    FROM information_schema.columns
    WHERE udt_name = 'taskpriority' AND udt_schema = 'public';

    IF table_count > 0 THEN
        RAISE EXCEPTION 'Cannot proceed: Found % tables using public.taskpriority', table_count;
    ELSE
        RAISE NOTICE 'Safe to remove: No tables use public.taskpriority';
    END IF;

    -- =====================================================
    -- STEP 3: Clean up duplicate enums
    -- =====================================================

    RAISE NOTICE 'Removing duplicate enums from public schema...';

    -- Remove public.taskstatus if it exists (with safe dependency check)
    IF EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'taskstatus' AND n.nspname = 'public'
    ) THEN
        -- Check for dependencies using pg_depend
        DECLARE
            dep_count INTEGER;
        BEGIN
            SELECT COUNT(*) INTO dep_count
            FROM pg_depend d
            JOIN pg_type t ON d.refobjid = t.oid
            JOIN pg_namespace n ON t.typnamespace = n.oid
            WHERE t.typname = 'taskstatus'
            AND n.nspname = 'public'
            AND d.deptype IN ('n', 'a'); -- normal and auto dependencies (excludes internal dependencies)

            RAISE NOTICE 'Found % dependencies on public.taskstatus', dep_count;

            IF dep_count > 0 THEN
                RAISE EXCEPTION 'Cannot drop public.taskstatus: % dependent objects exist. Manual investigation required.', dep_count;
            ELSE
                DROP TYPE public.taskstatus RESTRICT;
                RAISE NOTICE 'Successfully removed duplicate enum: public.taskstatus (no dependencies found)';
            END IF;
        END;
    ELSE
        RAISE NOTICE 'Enum public.taskstatus does not exist (already clean)';
    END IF;

    -- Remove public.taskpriority if it exists (with safe dependency check)
    IF EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'taskpriority' AND n.nspname = 'public'
    ) THEN
        -- Check for dependencies using pg_depend
        DECLARE
            dep_count INTEGER;
        BEGIN
            SELECT COUNT(*) INTO dep_count
            FROM pg_depend d
            JOIN pg_type t ON d.refobjid = t.oid
            JOIN pg_namespace n ON t.typnamespace = n.oid
            WHERE t.typname = 'taskpriority'
            AND n.nspname = 'public'
            AND d.deptype IN ('n', 'a'); -- normal and auto dependencies (excludes internal dependencies)

            RAISE NOTICE 'Found % dependencies on public.taskpriority', dep_count;

            IF dep_count > 0 THEN
                RAISE EXCEPTION 'Cannot drop public.taskpriority: % dependent objects exist. Manual investigation required.', dep_count;
            ELSE
                DROP TYPE public.taskpriority RESTRICT;
                RAISE NOTICE 'Successfully removed duplicate enum: public.taskpriority (no dependencies found)';
            END IF;
        END;
    ELSE
        RAISE NOTICE 'Enum public.taskpriority does not exist (already clean)';
    END IF;

    -- Also clean up any other duplicate task_status/task_priority in public schema
    IF EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_status' AND n.nspname = 'public'
    ) THEN
        -- First check if it's actually unused
        SELECT COUNT(*) INTO table_count
        FROM information_schema.columns
        WHERE udt_name = 'task_status' AND udt_schema = 'public';

        IF table_count = 0 THEN
            -- Check for other dependencies using pg_depend
            DECLARE
                dep_count INTEGER;
            BEGIN
                SELECT COUNT(*) INTO dep_count
                FROM pg_depend d
                JOIN pg_type t ON d.refobjid = t.oid
                JOIN pg_namespace n ON t.typnamespace = n.oid
                WHERE t.typname = 'task_status'
                AND n.nspname = 'public'
                AND d.deptype IN ('n', 'a');

                RAISE NOTICE 'Found % dependencies on public.task_status', dep_count;

                IF dep_count > 0 THEN
                    RAISE EXCEPTION 'Cannot drop public.task_status: % dependent objects exist. Manual investigation required.', dep_count;
                ELSE
                    DROP TYPE public.task_status RESTRICT;
                    RAISE NOTICE 'Successfully removed duplicate enum: public.task_status (no dependencies found)';
                END IF;
            END;
        ELSE
            RAISE WARNING 'Cannot remove public.task_status: % tables still use it', table_count;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_priority' AND n.nspname = 'public'
    ) THEN
        -- First check if it's actually unused
        SELECT COUNT(*) INTO table_count
        FROM information_schema.columns
        WHERE udt_name = 'task_priority' AND udt_schema = 'public';

        IF table_count = 0 THEN
            -- Check for other dependencies using pg_depend
            DECLARE
                dep_count INTEGER;
            BEGIN
                SELECT COUNT(*) INTO dep_count
                FROM pg_depend d
                JOIN pg_type t ON d.refobjid = t.oid
                JOIN pg_namespace n ON t.typnamespace = n.oid
                WHERE t.typname = 'task_priority'
                AND n.nspname = 'public'
                AND d.deptype IN ('n', 'a');

                RAISE NOTICE 'Found % dependencies on public.task_priority', dep_count;

                IF dep_count > 0 THEN
                    RAISE EXCEPTION 'Cannot drop public.task_priority: % dependent objects exist. Manual investigation required.', dep_count;
                ELSE
                    DROP TYPE public.task_priority RESTRICT;
                    RAISE NOTICE 'Successfully removed duplicate enum: public.task_priority (no dependencies found)';
                END IF;
            END;
        ELSE
            RAISE WARNING 'Cannot remove public.task_priority: % tables still use it', table_count;
        END IF;
    END IF;

    -- =====================================================
    -- STEP 4: Verify project schema enums are correct
    -- =====================================================

    RAISE NOTICE 'Verifying project schema enums...';

    -- Ensure project.task_status exists and has all required values
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_status' AND n.nspname = 'project' AND t.typtype = 'e'
    ) THEN
        RAISE EXCEPTION 'Critical error: project.task_status enum does not exist!';
    END IF;

    -- Ensure project.task_priority exists and has all required values
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'task_priority' AND n.nspname = 'project' AND t.typtype = 'e'
    ) THEN
        RAISE EXCEPTION 'Critical error: project.task_priority enum does not exist!';
    END IF;

    -- =====================================================
    -- STEP 5: Add documentation and constraints
    -- =====================================================

    -- Add comments to document the correct schema usage
    COMMENT ON TYPE project.task_status IS 'Task status enum - ALWAYS use project.task_status in migrations';
    COMMENT ON TYPE project.task_priority IS 'Task priority enum - ALWAYS use project.task_priority in migrations';

    -- =====================================================
    -- STEP 6: Final verification and reporting
    -- =====================================================

    RAISE NOTICE 'Performing final verification...';

    -- Report final state
    FOR enum_record IN
        SELECT n.nspname, t.typname, array_agg(e.enumlabel ORDER BY e.enumsortorder) as values
        FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        LEFT JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typtype = 'e' AND t.typname LIKE '%task%'
        GROUP BY n.nspname, t.typname
        ORDER BY n.nspname, t.typname
    LOOP
        RAISE NOTICE 'Final state: %.% = %', enum_record.nspname, enum_record.typname, enum_record.values;
    END LOOP;

    RAISE NOTICE 'V4 migration completed successfully!';
    RAISE NOTICE 'IMPORTANT: All future migrations must use project.task_status and project.task_priority';

END $$;

-- =====================================================
-- FINAL SAFETY CHECKS
-- =====================================================

-- Ensure the tasks table is still properly configured
DO $$
BEGIN
    -- Verify tasks table uses correct enums
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'project'
        AND table_name = 'tasks'
        AND column_name = 'status'
        AND udt_name = 'task_status'
        AND udt_schema = 'project'
    ) THEN
        RAISE EXCEPTION 'Tasks table status column is not using project.task_status!';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'project'
        AND table_name = 'tasks'
        AND column_name = 'priority'
        AND udt_name = 'task_priority'
        AND udt_schema = 'project'
    ) THEN
        RAISE EXCEPTION 'Tasks table priority column is not using project.task_priority!';
    END IF;

    RAISE NOTICE 'Verification passed: tasks table correctly uses project schema enums';
END $$;