INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'English Language', 'english-language', 'Core subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'english-language');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Mathematics', 'mathematics', 'Core subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'mathematics');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Physics', 'physics', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'physics');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Chemistry', 'chemistry', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'chemistry');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Biology', 'biology', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'biology');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Further Mathematics', 'further-mathematics', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'further-mathematics');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Agricultural Science', 'agricultural-science', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'agricultural-science');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Health Science', 'health-science', 'Science subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'health-science');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Accounting', 'accounting', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'accounting');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Commerce', 'commerce', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'commerce');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Economics', 'economics', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'economics');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Office Practice', 'office-practice', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'office-practice');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Marketing', 'marketing', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'marketing');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Insurance', 'insurance', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'insurance');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Store Management', 'store-management', 'Commercial/business subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'store-management');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Literature in English', 'literature-in-english', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'literature-in-english');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Government', 'government', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'government');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'CRS', 'crs', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'crs');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'IRS', 'irs', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'irs');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'History', 'history', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'history');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Civic Education', 'civic-education', 'Arts/humanities subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'civic-education');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Geography', 'geography', 'Social/general studies subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'geography');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Computer Studies / ICT', 'computer-studies-ict', 'Social/general studies subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'computer-studies-ict');

INSERT INTO subjects (public_id, name, slug, description)
SELECT UUID(), 'Social Studies', 'social-studies', 'Social/general studies subject'
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE slug = 'social-studies');
