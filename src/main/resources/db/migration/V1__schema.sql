CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    customer_email  VARCHAR(320) NOT NULL,
    product_sku     VARCHAR(64)  NOT NULL,
    quantity        INT          NOT NULL CHECK (quantity > 0),
    status          VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    event_type      VARCHAR(64)  NOT NULL,
    payload         JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at)
    WHERE published_at IS NULL;

CREATE TABLE processed_operations (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    order_id        UUID         NOT NULL,
    outcome         VARCHAR(32)  NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE dlq_records (
    id              UUID PRIMARY KEY,
    order_id        UUID,
    sqs_message_id  VARCHAR(128),
    failure_reason  TEXT         NOT NULL,
    raw_body        TEXT         NOT NULL,
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
