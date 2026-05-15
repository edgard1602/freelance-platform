-- Ajouter updated_at à la table skills
-- pour aligner avec BaseEntity (createdAt + updatedAt)
ALTER TABLE skills ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE skills SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE skills ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE skills ALTER COLUMN updated_at SET DEFAULT NOW();

-- Ajouter le trigger updated_at automatique
CREATE TRIGGER trg_skills_updated_at
    BEFORE UPDATE ON skills FOR EACH ROW EXECUTE FUNCTION set_updated_at();