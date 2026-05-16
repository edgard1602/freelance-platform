ALTER TABLE notifications ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE notifications SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE notifications ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN updated_at SET DEFAULT NOW();

CREATE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION set_updated_at();