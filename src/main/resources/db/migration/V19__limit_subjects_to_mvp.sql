DELETE FROM subjects
WHERE slug NOT IN (
    'english-language',
    'mathematics',
    'biology',
    'chemistry',
    'physics',
    'economics',
    'government',
    'literature-in-english',
    'geography',
    'accounting',
    'commerce',
    'crs',
    'irs',
    'agricultural-science'
)
AND NOT EXISTS (
    SELECT 1
    FROM student_exam_profile_subjects seps
    WHERE seps.subject_id = subjects.id
);
