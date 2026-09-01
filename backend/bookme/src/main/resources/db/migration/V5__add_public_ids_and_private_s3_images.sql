-- =========================================================
-- Public UUIDs
-- =========================================================

ALTER TABLE property
    ADD COLUMN public_id UUID;

UPDATE property
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE property
    ALTER COLUMN public_id SET DEFAULT gen_random_uuid();

ALTER TABLE property
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE property
    ADD CONSTRAINT uk_property_public_id UNIQUE (public_id);


ALTER TABLE bookable_unit
    ADD COLUMN public_id UUID;

UPDATE bookable_unit
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE bookable_unit
    ALTER COLUMN public_id SET DEFAULT gen_random_uuid();

ALTER TABLE bookable_unit
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE bookable_unit
    ADD CONSTRAINT uk_bookable_unit_public_id UNIQUE (public_id);


-- =========================================================
-- Stop storing permanent S3 URLs
-- The existing s3_key values remain untouched.
-- =========================================================



-- =========================================================
-- Image integrity
-- =========================================================

ALTER TABLE property_image
    ALTER COLUMN s3_key SET NOT NULL;

ALTER TABLE property_image
    ALTER COLUMN is_primary SET NOT NULL;

ALTER TABLE property_image
    ALTER COLUMN sort_order SET NOT NULL;

ALTER TABLE property_image
    ADD CONSTRAINT uk_property_image_s3_key UNIQUE (s3_key);

ALTER TABLE property_image
    ADD CONSTRAINT uk_property_image_sort_order
        UNIQUE (property_id, sort_order);


ALTER TABLE unit_image
    ALTER COLUMN s3_key SET NOT NULL;

ALTER TABLE unit_image
    ALTER COLUMN is_primary SET NOT NULL;

ALTER TABLE unit_image
    ALTER COLUMN sort_order SET NOT NULL;

ALTER TABLE unit_image
    ADD CONSTRAINT uk_unit_image_s3_key UNIQUE (s3_key);

ALTER TABLE unit_image
    ADD CONSTRAINT uk_unit_image_sort_order
        UNIQUE (unit_id, sort_order);


-- Only one primary image can exist per property.
CREATE UNIQUE INDEX uk_property_image_one_primary
    ON property_image (property_id)
    WHERE is_primary = TRUE;

-- Only one primary image can exist per unit.
CREATE UNIQUE INDEX uk_unit_image_one_primary
    ON unit_image (unit_id)
    WHERE is_primary = TRUE;