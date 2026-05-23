CREATE TABLE coupons (
                         id UUID NOT NULL,
                         code VARCHAR(6) NOT NULL,
                         description VARCHAR(255) NOT NULL,
                         discount_value NUMERIC(10, 2) NOT NULL,
                         expiration_date TIMESTAMP WITH TIME ZONE NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         published BOOLEAN NOT NULL,
                         redeemed BOOLEAN NOT NULL,
                         deleted BOOLEAN NOT NULL,

                         CONSTRAINT pk_coupons PRIMARY KEY (id)
);

CREATE INDEX idx_coupons_id_deleted
    ON coupons (id, deleted);