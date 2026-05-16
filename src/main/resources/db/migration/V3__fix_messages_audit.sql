-- Ajouter updated_at à la table messages
ALTER TABLE messages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE messages SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE messages ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE messages ALTER COLUMN updated_at SET DEFAULT NOW();

CREATE TRIGGER trg_messages_updated_at
    BEFORE UPDATE ON messages FOR EACH ROW EXECUTE FUNCTION set_updated_at();