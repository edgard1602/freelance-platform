-- ================================================================
--  V1 — Schéma initial de la plateforme freelance
--  Flyway gère le DDL. Hibernate en mode "validate" uniquement.
-- ================================================================

-- Extension UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── ENUMS ────────────────────────────────────────────────────────

CREATE TYPE user_role AS ENUM ('FREELANCER', 'CLIENT', 'ADMIN');
CREATE TYPE user_status AS ENUM ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'DELETED');
CREATE TYPE project_status AS ENUM ('DRAFT', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE application_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN');
CREATE TYPE contract_status AS ENUM ('ACTIVE', 'COMPLETED', 'DISPUTED', 'CANCELLED');
CREATE TYPE notification_type AS ENUM (
    'APPLICATION_RECEIVED', 'APPLICATION_ACCEPTED', 'APPLICATION_REJECTED',
    'PROJECT_AWARDED', 'NEW_MESSAGE', 'REVIEW_RECEIVED', 'ACCOUNT_VERIFIED'
);

-- ── TABLE : users ─────────────────────────────────────────────────
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            user_role NOT NULL DEFAULT 'FREELANCER',
    status          user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    avatar_url      VARCHAR(500),
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    -- Audit
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ     -- Soft delete
);

CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);
CREATE INDEX idx_users_deleted_at  ON users(deleted_at) WHERE deleted_at IS NULL;

-- ── TABLE : refresh_tokens ────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(255),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ── TABLE : email_verification_tokens ────────────────────────────
CREATE TABLE email_verification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── TABLE : freelancer_profiles ──────────────────────────────────
CREATE TABLE freelancer_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio             TEXT,
    hourly_rate     DECIMAL(10, 2),
    availability    BOOLEAN NOT NULL DEFAULT TRUE,
    experience_years INTEGER,
    portfolio_url   VARCHAR(500),
    location        VARCHAR(255),
    -- Statistiques dénormalisées pour performance (mis à jour via trigger)
    avg_rating      DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews   INTEGER NOT NULL DEFAULT 0,
    completed_jobs  INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── TABLE : skills ────────────────────────────────────────────────
CREATE TABLE skills (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    category    VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── TABLE : freelancer_skills (N:M) ──────────────────────────────
CREATE TABLE freelancer_skills (
    freelancer_profile_id UUID NOT NULL REFERENCES freelancer_profiles(id) ON DELETE CASCADE,
    skill_id              UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (freelancer_profile_id, skill_id)
);

-- ── TABLE : projects ─────────────────────────────────────────────
CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID NOT NULL REFERENCES users(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    budget_min      DECIMAL(12, 2),
    budget_max      DECIMAL(12, 2),
    deadline        DATE,
    status          project_status NOT NULL DEFAULT 'DRAFT',
    -- Full-text search
    search_vector   TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('french', coalesce(title, '') || ' ' || coalesce(description, ''))
    ) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_projects_client_id     ON projects(client_id);
CREATE INDEX idx_projects_status        ON projects(status);
CREATE INDEX idx_projects_search_vector ON projects USING GIN(search_vector);
CREATE INDEX idx_projects_deleted_at    ON projects(deleted_at) WHERE deleted_at IS NULL;

-- ── TABLE : project_skills (N:M) ─────────────────────────────────
CREATE TABLE project_skills (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    skill_id   UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, skill_id)
);

-- ── TABLE : applications ─────────────────────────────────────────
CREATE TABLE applications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    freelancer_id   UUID NOT NULL REFERENCES users(id),
    cover_letter    TEXT,
    proposed_rate   DECIMAL(10, 2),
    status          application_status NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, freelancer_id)  -- Un freelancer ne postule qu'une fois
);

CREATE INDEX idx_applications_project_id   ON applications(project_id);
CREATE INDEX idx_applications_freelancer_id ON applications(freelancer_id);
CREATE INDEX idx_applications_status        ON applications(status);

-- ── TABLE : contracts ────────────────────────────────────────────
CREATE TABLE contracts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL UNIQUE REFERENCES projects(id),
    application_id  UUID NOT NULL UNIQUE REFERENCES applications(id),
    client_id       UUID NOT NULL REFERENCES users(id),
    freelancer_id   UUID NOT NULL REFERENCES users(id),
    agreed_rate     DECIMAL(10, 2) NOT NULL,
    status          contract_status NOT NULL DEFAULT 'ACTIVE',
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contracts_client_id     ON contracts(client_id);
CREATE INDEX idx_contracts_freelancer_id ON contracts(freelancer_id);

-- ── TABLE : reviews ──────────────────────────────────────────────
CREATE TABLE reviews (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    reviewer_id UUID NOT NULL REFERENCES users(id),
    reviewee_id UUID NOT NULL REFERENCES users(id),
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (contract_id, reviewer_id)  -- Une review par personne par contrat
);

CREATE INDEX idx_reviews_reviewee_id ON reviews(reviewee_id);

-- ── TABLE : messages ─────────────────────────────────────────────
CREATE TABLE messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    project_id  UUID REFERENCES projects(id),
    content     TEXT NOT NULL,
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_sender_receiver ON messages(sender_id, receiver_id);
CREATE INDEX idx_messages_receiver_unread ON messages(receiver_id, read_at) WHERE read_at IS NULL;

-- ── TABLE : notifications ─────────────────────────────────────────
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        notification_type NOT NULL,
    title       VARCHAR(255) NOT NULL,
    body        TEXT,
    payload     JSONB,           -- données contextuelles (ex: project_id)
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_at) WHERE read_at IS NULL;

-- ── TRIGGER : updated_at automatique ─────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_applications_updated_at
    BEFORE UPDATE ON applications FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_contracts_updated_at
    BEFORE UPDATE ON contracts FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_freelancer_profiles_updated_at
    BEFORE UPDATE ON freelancer_profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── TRIGGER : stats freelancer recalculées après review ──────────
CREATE OR REPLACE FUNCTION update_freelancer_stats()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE freelancer_profiles fp
    SET
        avg_rating    = (SELECT ROUND(AVG(r.rating)::NUMERIC, 2) FROM reviews r WHERE r.reviewee_id = fp.user_id),
        total_reviews = (SELECT COUNT(*) FROM reviews r WHERE r.reviewee_id = fp.user_id)
    WHERE fp.user_id = NEW.reviewee_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_freelancer_stats
    AFTER INSERT OR UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_freelancer_stats();

-- ── DATA : skills de base ─────────────────────────────────────────
INSERT INTO skills (name, category) VALUES
    ('Java',             'Backend'),
    ('Spring Boot',      'Backend'),
    ('Python',           'Backend'),
    ('Node.js',          'Backend'),
    ('PostgreSQL',       'Database'),
    ('MongoDB',          'Database'),
    ('React',            'Frontend'),
    ('Angular',          'Frontend'),
    ('Vue.js',           'Frontend'),
    ('Docker',           'DevOps'),
    ('Kubernetes',       'DevOps'),
    ('AWS',              'Cloud'),
    ('Machine Learning', 'AI/ML'),
    ('Figma',            'Design'),
    ('UI/UX Design',     'Design');