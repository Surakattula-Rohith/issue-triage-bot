-- Runs at every startup; IF NOT EXISTS makes it safe to run again.
CREATE TABLE IF NOT EXISTS issues (
    number     INTEGER     PRIMARY KEY,
    title      TEXT        NOT NULL,
    body       TEXT,
    state      TEXT        NOT NULL,
    labels     TEXT[]      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    html_url   TEXT        NOT NULL
);
