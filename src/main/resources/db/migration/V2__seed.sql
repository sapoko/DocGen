-- =====================================================================
-- V2__seed.sql  (переписан под фактические шаблоны)
--
-- Инвентаризация снята по сырому XML всех десяти файлов, включая надписи
-- (w:txbxContent) — блоки подписи лежат именно там.
--
-- ВАЖНО: файлы шаблонов переименовать в латиницу по file_path ниже
-- и положить в папку templates/.
--
-- Плейсхолдеры госпитальной таблицы (position, rank, fullName, platoon,
-- hospitalTitle, diagnosis, admittedAt) в sample_fields НЕ входят:
-- они приходят из hospital_rows отдельным списком.
-- =====================================================================

INSERT INTO samples (file_path, public_name, periodicity, day_of_week, has_hospital_table) VALUES
  ('monday-hospital.docx',  'Рапорт по госпиталю (есть госпитализированные)', 'WEEKLY',  1, true),
  ('monday-plain.docx',     'Рапорт по госпиталю (нет госпитализированных)',  'WEEKLY',  1, false),
  ('wednesday-meals.docx',  'Сверка по питанию',                              'WEEKLY',  3, false),
  ('thursday-phd.docx',     'План ПХД',                                       'WEEKLY',  4, false),
  ('friday-zgt.docx',       'Рапорт ЗГТ',                                     'WEEKLY',  5, false),
  ('friday-kv.docx',        'Планы КВ',                                       'WEEKLY',  5, false),
  ('friday-sport-1.docx',   'План-конспект спортмасс (вариант 1)',            'WEEKLY',  5, false),
  ('friday-sport-2.docx',   'План-конспект спортмасс (вариант 2)',            'WEEKLY',  5, false),
  ('friday-sport-3.docx',   'План-конспект спортмасс (вариант 3)',            'WEEKLY',  5, false),
  ('monthly-weapons.docx',  'Сверка по оружию',                               'MONTHLY', NULL, false);


-- ---------------------------------------------------------------------
-- Понедельник: с госпитальной таблицей
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',   'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'monday-hospital.docx'
UNION ALL
SELECT id, 'signer', 'Подписант',      'SIGNATORY', 2 FROM samples WHERE file_path = 'monday-hospital.docx';

-- ---------------------------------------------------------------------
-- Понедельник: без таблицы
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',   'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'monday-plain.docx'
UNION ALL
SELECT id, 'signer', 'Подписант',      'SIGNATORY', 2 FROM samples WHERE file_path = 'monday-plain.docx';

-- ---------------------------------------------------------------------
-- Среда: сверка по питанию
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',     'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'dateFrom', 'Период с',       'DATE',      2 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'dateTo',   'Период по',      'DATE',      3 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'amount1',  'Показатель 1',   'NUMBER',    4 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'amount2',  'Показатель 2',   'NUMBER',    5 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'amount3',  'Показатель 3',   'NUMBER',    6 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'amount4',  'Показатель 4',   'NUMBER',    7 FROM samples WHERE file_path = 'wednesday-meals.docx'
UNION ALL
SELECT id, 'signer',   'Подписант',      'SIGNATORY', 8 FROM samples WHERE file_path = 'wednesday-meals.docx';

-- ---------------------------------------------------------------------
-- Четверг: План ПХД
-- signer3 стоит в 19 строках таблицы (smallRank + midName) — один человек.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',      'Дата документа',          'DATE',      1 FROM samples WHERE file_path = 'thursday-phd.docx'
UNION ALL
SELECT id, 'inputDate', 'Дата проведения',         'DATE',      2 FROM samples WHERE file_path = 'thursday-phd.docx'
UNION ALL
SELECT id, 'signer1',   'Утверждающий',            'SIGNATORY', 3 FROM samples WHERE file_path = 'thursday-phd.docx'
UNION ALL
SELECT id, 'signer2',   'Согласующий',             'SIGNATORY', 4 FROM samples WHERE file_path = 'thursday-phd.docx'
UNION ALL
SELECT id, 'signer3',   'Ответственный за работы', 'SIGNATORY', 5 FROM samples WHERE file_path = 'thursday-phd.docx'
UNION ALL
SELECT id, 'signer4',   'Исполнитель',             'SIGNATORY', 6 FROM samples WHERE file_path = 'thursday-phd.docx';

-- ---------------------------------------------------------------------
-- Пятница: Рапорт ЗГТ
-- signer1 используется в творительном падеже внутри текста.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date1',    'Дата 1',               'DATE',      1 FROM samples WHERE file_path = 'friday-zgt.docx'
UNION ALL
SELECT id, 'date2',    'Дата получения баз',   'DATE',      2 FROM samples WHERE file_path = 'friday-zgt.docx'
UNION ALL
SELECT id, 'signDate', 'Дата подписи',         'DATE',      3 FROM samples WHERE file_path = 'friday-zgt.docx'
UNION ALL
SELECT id, 'signer1',  'Ответственный за ОБИ', 'SIGNATORY', 4 FROM samples WHERE file_path = 'friday-zgt.docx'
UNION ALL
SELECT id, 'signer2',  'Подписант',            'SIGNATORY', 5 FROM samples WHERE file_path = 'friday-zgt.docx';

-- ---------------------------------------------------------------------
-- Пятница: Планы КВ
-- signer2 и signer3 используются и в именительном, и в родительном.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',     'Дата документа',     'DATE',       1 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'dateFrom', 'Период с',           'DATE',       2 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'dateTo',   'Период по',          'DATE',       3 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'day',      'Первый день недели', 'WEEK_START', 4 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'theme1',   'Тема 1',             'TEXT',       5 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'theme2',   'Тема 2',             'TEXT',       6 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'theme3',   'Тема 3',             'TEXT',       7 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'theme4',   'Тема 4',             'TEXT',       8 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'theme5',   'Тема 5',             'TEXT',       9 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'signer1',  'Утверждающий',       'SIGNATORY', 10 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'signer2',  'Подписант (лист 1)', 'SIGNATORY', 11 FROM samples WHERE file_path = 'friday-kv.docx'
UNION ALL
SELECT id, 'signer3',  'Подписант (лист 2)', 'SIGNATORY', 12 FROM samples WHERE file_path = 'friday-kv.docx';

-- ---------------------------------------------------------------------
-- Пятница: план-конспект спортмасс, три варианта с одинаковыми полями
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',    'Дата документа', 'DATE',      1 FROM samples WHERE file_path LIKE 'friday-sport-_.docx'
UNION ALL
SELECT id, 'signer1', 'Утверждающий',   'SIGNATORY', 2 FROM samples WHERE file_path LIKE 'friday-sport-_.docx'
UNION ALL
SELECT id, 'signer2', 'Подписант',      'SIGNATORY', 3 FROM samples WHERE file_path LIKE 'friday-sport-_.docx';

-- ---------------------------------------------------------------------
-- Ежемесячный: сверка по оружию
-- В шаблоне только блок подписи, дат нет.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'signer', 'Подписант', 'SIGNATORY', 1 FROM samples WHERE file_path = 'monthly-weapons.docx';
