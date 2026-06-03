INSERT INTO past_questions (
    public_id, exam_type_id, subject_id, topic_id, exam_year,
    question_text, option_a, option_b, option_c, option_d, correct_option, explanation
)
SELECT UUID(), e.id, s.id, t.id, 2023,
       'Which of the following is the basic unit of life?',
       'Tissue', 'Cell', 'Organ', 'System', 'B',
       'The cell is the smallest structural and functional unit of living organisms.'
FROM exam_types e
JOIN subjects s ON s.slug = 'biology'
JOIN topics t ON t.subject_id = s.id AND t.slug = 'cell-biology'
WHERE e.slug = 'jamb'
  AND NOT EXISTS (SELECT 1 FROM past_questions WHERE topic_id = t.id AND question_text = 'Which of the following is the basic unit of life?');

INSERT INTO past_questions (
    public_id, exam_type_id, subject_id, topic_id, exam_year,
    question_text, option_a, option_b, option_c, option_d, correct_option, explanation
)
SELECT UUID(), e.id, s.id, t.id, 2022,
       'Solve for x: 2x + 5 = 13.',
       '3', '4', '5', '6', 'B',
       'Subtract 5 from both sides to get 2x = 8, then divide by 2 to get x = 4.'
FROM exam_types e
JOIN subjects s ON s.slug = 'mathematics'
JOIN topics t ON t.subject_id = s.id AND t.slug = 'algebra'
WHERE e.slug = 'jamb'
  AND NOT EXISTS (SELECT 1 FROM past_questions WHERE topic_id = t.id AND question_text = 'Solve for x: 2x + 5 = 13.');

INSERT INTO past_questions (
    public_id, exam_type_id, subject_id, topic_id, exam_year,
    question_text, option_a, option_b, option_c, option_d, correct_option, explanation
)
SELECT UUID(), e.id, s.id, t.id, 2021,
       'In a market, price is mainly determined by the interaction of',
       'demand and supply', 'tax and subsidy', 'money and banking', 'exports and imports', 'A',
       'Market price is determined by the interaction of demand and supply.'
FROM exam_types e
JOIN subjects s ON s.slug = 'economics'
JOIN topics t ON t.subject_id = s.id AND t.slug = 'price-determination'
WHERE e.slug = 'waec'
  AND NOT EXISTS (SELECT 1 FROM past_questions WHERE topic_id = t.id AND question_text = 'In a market, price is mainly determined by the interaction of');
