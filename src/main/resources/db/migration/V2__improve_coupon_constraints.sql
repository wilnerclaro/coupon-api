ALTER TABLE coupons
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE coupons
    ALTER COLUMN published SET DEFAULT FALSE,
ALTER COLUMN redeemed SET DEFAULT FALSE,
    ALTER COLUMN deleted SET DEFAULT FALSE;

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_code_format
        CHECK (code ~ '^[A-Z0-9]{6}$');

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_discount_value_range
        CHECK (discount_value >= 0.50 AND discount_value <= 99999999.99);

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_status_values
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'));

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_deleted_lifecycle
        CHECK (
            (deleted = FALSE AND status IN ('ACTIVE', 'INACTIVE'))
                OR
            (deleted = TRUE AND status = 'DELETED' AND published = FALSE AND deleted_at IS NOT NULL)
            );

DROP INDEX IF EXISTS idx_coupons_id_deleted;

CREATE UNIQUE INDEX uk_coupons_code_not_deleted
    ON coupons (code)
    WHERE deleted = FALSE;

CREATE INDEX idx_coupons_status_deleted
    ON coupons (status, deleted);

CREATE INDEX idx_coupons_expiration_date
    ON coupons (expiration_date);