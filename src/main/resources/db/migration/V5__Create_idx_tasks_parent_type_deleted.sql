-- V5__Create_idx_tasks_parent_type_deleted.sql
-- Migration to create composite index for optimizing task hierarchy queries
-- This index optimizes findByParentTaskIdAndTaskTypeAndDeletedAtIsNull repository method

DO $$
BEGIN
    RAISE NOTICE 'Starting V5 migration: Creating composite index for task hierarchy queries';

    -- =====================================================
    -- STEP 1: Verify target table exists
    -- =====================================================

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'project'
        AND table_name = 'tasks'
    ) THEN
        RAISE EXCEPTION 'Table project.tasks does not exist';
    END IF;

    RAISE NOTICE 'Verified: project.tasks table exists';

    -- =====================================================
    -- STEP 2: Check if index already exists
    -- =====================================================

    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'project'
        AND tablename = 'tasks'
        AND indexname = 'idx_tasks_parent_type_deleted'
    ) THEN
        RAISE NOTICE 'Index idx_tasks_parent_type_deleted already exists - skipping creation';
    ELSE
        -- =====================================================
        -- STEP 3: Create composite index
        -- =====================================================

        RAISE NOTICE 'Creating composite index on (parent_task_id, task_type, deleted_at)...';

        -- Create index with optimal column order:
        -- 1. parent_task_id: High selectivity, most commonly filtered
        -- 2. task_type: Medium selectivity, frequently used in hierarchy queries
        -- 3. deleted_at: Low selectivity but important for filtering active records
        CREATE INDEX idx_tasks_parent_type_deleted
        ON project.tasks (parent_task_id, task_type, deleted_at);

        RAISE NOTICE 'Successfully created composite index: idx_tasks_parent_type_deleted';
    END IF;

    -- =====================================================
    -- STEP 4: Verify index creation and provide statistics
    -- =====================================================

    -- Check index was created successfully
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'project'
        AND tablename = 'tasks'
        AND indexname = 'idx_tasks_parent_type_deleted'
    ) THEN
        RAISE EXCEPTION 'Failed to create index idx_tasks_parent_type_deleted';
    END IF;

    -- Get table statistics
    DECLARE
        total_tasks INTEGER;
        tasks_with_parent INTEGER;
        epic_count INTEGER;
        story_count INTEGER;
        task_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO total_tasks FROM project.tasks;
        SELECT COUNT(*) INTO tasks_with_parent FROM project.tasks WHERE parent_task_id IS NOT NULL;
        SELECT COUNT(*) INTO epic_count FROM project.tasks WHERE task_type = 'EPIC';
        SELECT COUNT(*) INTO story_count FROM project.tasks WHERE task_type = 'STORY';
        SELECT COUNT(*) INTO task_count FROM project.tasks WHERE task_type = 'TASK';

        RAISE NOTICE 'Index creation completed successfully!';
        RAISE NOTICE 'Table statistics:';
        RAISE NOTICE '  Total tasks: %', total_tasks;
        RAISE NOTICE '  Tasks with parent: %', tasks_with_parent;
        RAISE NOTICE '  EPICs: %, STORYs: %, TASKs: %', epic_count, story_count, task_count;
    END;

    -- =====================================================
    -- STEP 5: Provide usage information
    -- =====================================================

    RAISE NOTICE '';
    RAISE NOTICE 'Index Usage Information:';
    RAISE NOTICE 'This index optimizes the following repository method:';
    RAISE NOTICE '  TaskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull()';
    RAISE NOTICE '';
    RAISE NOTICE 'Query pattern optimized:';
    RAISE NOTICE '  WHERE parent_task_id = ? AND task_type = ? AND deleted_at IS NULL';
    RAISE NOTICE '';
    RAISE NOTICE 'Used by services:';
    RAISE NOTICE '  - TaskProgressServiceImpl (lines 119, 152)';
    RAISE NOTICE '  - TaskHierarchyServiceImpl';
    RAISE NOTICE '  - TaskHybridServiceImpl';
    RAISE NOTICE '';
    RAISE NOTICE 'To verify index usage, run:';
    RAISE NOTICE '  EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM project.tasks';
    RAISE NOTICE '  WHERE parent_task_id = 1 AND task_type = ''STORY'' AND deleted_at IS NULL;';

    RAISE NOTICE 'V5 migration completed successfully!';

END $$;

-- =====================================================
-- ADDITIONAL OPTIMIZATIONS
-- =====================================================

-- Add comment to document the index purpose
COMMENT ON INDEX project.idx_tasks_parent_type_deleted IS
'Composite index to optimize task hierarchy queries. Used by TaskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull() method in TaskProgressServiceImpl, TaskHierarchyServiceImpl, and TaskHybridServiceImpl.';

-- Update table statistics to help query planner
ANALYZE project.tasks;