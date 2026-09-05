-- Needed for gen_random_uuid() if it isn't already available.
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- =========================================================
-- BOOKING PUBLIC UUID
-- =========================================================

ALTER TABLE booking
    ADD COLUMN public_id UUID;


-- Existing bookings may already exist in production,
-- so give every existing row a UUID.
UPDATE booking
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;


-- New rows inserted directly through SQL also get a UUID.
ALTER TABLE booking
    ALTER COLUMN public_id SET DEFAULT gen_random_uuid();


ALTER TABLE booking
    ALTER COLUMN public_id SET NOT NULL;


ALTER TABLE booking
    ADD CONSTRAINT uq_booking_public_id
        UNIQUE (public_id);


-- =========================================================
-- REVIEW REFACTOR
-- =========================================================

-- You said there are no review rows, so these can safely
-- be removed directly.
ALTER TABLE review
DROP COLUMN reviewer_id;

ALTER TABLE review
DROP COLUMN property_id;


-- Public identifier for Review.
ALTER TABLE review
    ADD COLUMN public_id UUID NOT NULL DEFAULT gen_random_uuid();


ALTER TABLE review
    ADD CONSTRAINT uq_review_public_id
        UNIQUE (public_id);


-- Review now belongs to the booking that produced it.
-- Because review table is empty this can immediately be NOT NULL.
ALTER TABLE review
    ADD COLUMN booking_id BIGINT NOT NULL;


ALTER TABLE review
    ADD CONSTRAINT fk_review_booking
        FOREIGN KEY (booking_id)
            REFERENCES booking(id);


-- One booking can only have one review.
ALTER TABLE review
    ADD CONSTRAINT uq_review_booking
        UNIQUE (booking_id);


-- Defensive DB-level rating validation.
ALTER TABLE review
    ADD CONSTRAINT chk_review_rating
        CHECK (rating >= 1 AND rating <= 5);