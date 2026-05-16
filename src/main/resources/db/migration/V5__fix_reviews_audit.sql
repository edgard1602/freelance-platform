ALTER TABLE reviews ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE reviews SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE reviews ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN updated_at SET DEFAULT NOW();

CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON reviews FOR EACH ROW EXECUTE FUNCTION set_updated_at();