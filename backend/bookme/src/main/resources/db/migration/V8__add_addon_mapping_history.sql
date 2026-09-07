-- ============================================================
-- ADDON MAPPING HISTORY
-- ============================================================


-- ------------------------------------------------------------
-- 1. Add active_from
--
-- Initially nullable because addon_mapping may already contain
-- rows in development/production.
-- ------------------------------------------------------------

ALTER TABLE public.addon_mapping
    ADD COLUMN active_from DATE;


-- ------------------------------------------------------------
-- 2. Add active_until
--
-- NULL means that this mapping is currently active.
-- ------------------------------------------------------------

ALTER TABLE public.addon_mapping
    ADD COLUMN active_until DATE;


-- ------------------------------------------------------------
-- 3. Backfill existing mappings
--
-- Existing mappings were already active before this feature
-- existed, but we don't know their true historical start date.
--
-- CURRENT_DATE therefore represents the first date from which
-- the new system knows they were active.
-- ------------------------------------------------------------

UPDATE public.addon_mapping
SET active_from = CURRENT_DATE
WHERE active_from IS NULL;


-- ------------------------------------------------------------
-- 4. active_from must always exist
--
-- Matches:
--
-- @Column(nullable = false)
-- private LocalDate activeFrom;
-- ------------------------------------------------------------

ALTER TABLE public.addon_mapping
    ALTER COLUMN active_from SET NOT NULL;


-- ------------------------------------------------------------
-- 5. Prevent invalid history
--
-- active_until may be NULL, meaning currently active.
--
-- If it exists, it cannot be before active_from.
-- ------------------------------------------------------------

ALTER TABLE public.addon_mapping
    ADD CONSTRAINT chk_addon_mapping_active_period
        CHECK (
            active_until IS NULL
                OR active_until >= active_from
            );


-- ============================================================
-- ONE ACTIVE ADDON MAPPING PER UNIT
-- ============================================================

-- Historical mappings for the same unit/addon are allowed:
--
-- unit 1 + Breakfast + Jan -> May
-- unit 1 + Breakfast + Aug -> Dec
-- unit 1 + Breakfast + Jan -> NULL
--
-- But only ONE mapping may currently have active_until = NULL.
--
-- PostgreSQL partial unique index is perfect for this.
-- ------------------------------------------------------------

CREATE UNIQUE INDEX uq_addon_mapping_active_unit_addon
    ON public.addon_mapping (unit_id, addon_id)
    WHERE active_until IS NULL;



-- ============================================================
-- BOOKING ADDON ITEM SNAPSHOT
-- ============================================================


-- ------------------------------------------------------------
-- 6. Add addon name snapshot
--
-- Initially nullable because existing bookings may already
-- contain addon items.
-- ------------------------------------------------------------

ALTER TABLE public.booking_addon_item
    ADD COLUMN addon_name_snapshot VARCHAR(255);


-- ------------------------------------------------------------
-- 7. Backfill existing booking addon items
--
-- We cannot recover the historical addon name if it was changed
-- previously, so the current addon name is the best available
-- value for old data.
-- ------------------------------------------------------------

UPDATE public.booking_addon_item bai
SET addon_name_snapshot = a.name
    FROM public.addon a
WHERE bai.addon_id = a.id
  AND bai.addon_name_snapshot IS NULL;


-- ------------------------------------------------------------
-- 8. Snapshot becomes mandatory for future bookings
-- ------------------------------------------------------------

ALTER TABLE public.booking_addon_item
    ALTER COLUMN addon_name_snapshot SET NOT NULL;