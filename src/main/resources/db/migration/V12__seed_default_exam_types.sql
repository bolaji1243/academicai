INSERT INTO exam_types (public_id, name, slug, description)
SELECT UUID(), 'JAMB', 'jamb', 'Joint Admissions and Matriculation Board exam'
WHERE NOT EXISTS (SELECT 1 FROM exam_types WHERE slug = 'jamb');

INSERT INTO exam_types (public_id, name, slug, description)
SELECT UUID(), 'WAEC', 'waec', 'West African Senior School Certificate Examination'
WHERE NOT EXISTS (SELECT 1 FROM exam_types WHERE slug = 'waec');

INSERT INTO exam_types (public_id, name, slug, description)
SELECT UUID(), 'NECO', 'neco', 'National Examinations Council exam'
WHERE NOT EXISTS (SELECT 1 FROM exam_types WHERE slug = 'neco');

INSERT INTO exam_types (public_id, name, slug, description)
SELECT UUID(), 'GCE', 'gce', 'General Certificate Examination'
WHERE NOT EXISTS (SELECT 1 FROM exam_types WHERE slug = 'gce');

INSERT INTO exam_types (public_id, name, slug, description)
SELECT UUID(), 'POST-UTME', 'post-utme', 'Post-Unified Tertiary Matriculation Examination'
WHERE NOT EXISTS (SELECT 1 FROM exam_types WHERE slug = 'post-utme');
