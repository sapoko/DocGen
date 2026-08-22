-- =====================================================================
-- V5__wednesday_amounts.sql
--
--
-- Нулевой показатель должен исчезать вместе со своей подписью и запятой.
-- Поэтому тип OPTIONAL_NUMBER: значение уходит в документ не голым числом,
-- а фрагментом «, командировка – 2», либо пустой строкой при нуле.
-- Разделитель стоит ПЕРЕД фрагментом — «по списку» заполнен всегда,
-- поэтому запятая никогда не окажется ни в начале, ни перед точкой.
--
-- Подпись фрагмента берётся из form_name — текст остаётся в базе,
-- а не переезжает в Java.
--
-- =====================================================================

-- Понятные названия вместо «Показатель N» + порядок по предложению
UPDATE sample_fields SET form_name = 'По списку',   type = 'NUMBER',          position = 4
WHERE placeholder = 'amount1'
  AND sample_id = (SELECT id FROM samples WHERE file_path = 'wednesday-meals.docx');

UPDATE sample_fields SET form_name = 'Командировка', type = 'OPTIONAL_NUMBER', position = 5
WHERE placeholder = 'amount2'
  AND sample_id = (SELECT id FROM samples WHERE file_path = 'wednesday-meals.docx');

UPDATE sample_fields SET form_name = 'Налицо',       type = 'OPTIONAL_NUMBER', position = 6
WHERE placeholder = 'amount3'
  AND sample_id = (SELECT id FROM samples WHERE file_path = 'wednesday-meals.docx');

UPDATE sample_fields SET form_name = 'Медпункт',     type = 'OPTIONAL_NUMBER', position = 7
WHERE placeholder = 'amount4'
  AND sample_id = (SELECT id FROM samples WHERE file_path = 'wednesday-meals.docx');

-- Пятый показатель, которого раньше не было
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'amount5', 'Госпиталь', 'OPTIONAL_NUMBER', 8
FROM samples WHERE file_path = 'wednesday-meals.docx';

-- Подписант уезжает в конец формы
UPDATE sample_fields SET position = 9
WHERE placeholder = 'signer'
  AND sample_id = (SELECT id FROM samples WHERE file_path = 'wednesday-meals.docx');
