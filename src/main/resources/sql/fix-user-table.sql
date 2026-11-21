USE security_defense;

ALTER TABLE sys_user ADD COLUMN two_factor_secret VARCHAR(255) COMMENT 'Two Factor Secret' AFTER two_factor_enabled;
