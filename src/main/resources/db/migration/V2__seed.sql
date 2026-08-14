-- Шаблоны
INSERT INTO samples (file_path, public_name, periodicity, day_of_week, has_hospital_table) VALUES
  ('monday-hospital.docx', 'Понедельник — документ 1.1', 'WEEKLY',  1, true),
  ('monday-plain.docx',    'Понедельник — документ 1.2', 'WEEKLY',  1, false),
  ('wednesday.docx',       'Среда — документ 1',                             'WEEKLY',  3, false),
  ('thursday.docx',        'Четверг — документ 1',                           'WEEKLY',  4, false),
  ('friday-2.docx',        'Пятница — документ 2',                           'WEEKLY',  5, false),
  ('friday-3.docx',        'Пятница — документ 3',                 'WEEKLY',  5, false),
  ('friday-4-1.docx',      'Пятница — документ 4.1 (вариант 1)',             'WEEKLY',  5, false),
  ('monthly.docx',         'Пятница — документ 1 (ежемесячный)',                           'MONTHLY', NULL, false);


-- ---------------------------------------------------------------------
-- Понедельник 1.1 — с госпитальной таблицей
-- Плейсхолдеры внутри таблицы (position, rank, fullName, platoon,
-- hospitalTitle, diagnosis, admittedAt) в sample_fields НЕ входят:
-- они приходят из hospital_rows отдельным списком.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',   'Дата документа',        'DATE',      1 FROM samples WHERE file_path = 'monday-hospital.docx'
UNION ALL
SELECT id, 'signer', 'Подписант',             'SIGNATORY', 2 FROM samples WHERE file_path = 'monday-hospital.docx';


-- ---------------------------------------------------------------------
-- Понедельник 1.2 — без таблицы
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',   'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'monday-plain.docx'
UNION ALL
SELECT id, 'signer', 'Подписант',      'SIGNATORY', 2 FROM samples WHERE file_path = 'monday-plain.docx';


-- ---------------------------------------------------------------------
-- Среда
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',     'Дата документа',   'DATE',      1 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'dateFrom', 'Период с',         'DATE',      2 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'dateTo',   'Период по',        'DATE',      3 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'amount1',  'Показатель 1',     'NUMBER',    4 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'amount2',  'Показатель 2',     'NUMBER',    5 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'amount3',  'Показатель 3',     'NUMBER',    6 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'amount4',  'Показатель 4',     'NUMBER',    7 FROM samples WHERE file_path = 'wednesday.docx'
UNION ALL
SELECT id, 'signer',   'Подписант',        'SIGNATORY', 8 FROM samples WHERE file_path = 'wednesday.docx';


-- ---------------------------------------------------------------------
-- Четверг
-- signer3 стоит в 19 строках таблицы, но это один и тот же плейсхолдер
-- -> одно значение подставляется во все места.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',      'Дата документа',            'DATE',      1 FROM samples WHERE file_path = 'thursday.docx'
UNION ALL
SELECT id, 'inputDate', 'Дата проведения',           'DATE',      2 FROM samples WHERE file_path = 'thursday.docx'
UNION ALL
SELECT id, 'signer2',   'Утверждающий',              'SIGNATORY', 3 FROM samples WHERE file_path = 'thursday.docx'
UNION ALL
SELECT id, 'signer3',   'Ответственный за работы',   'SIGNATORY', 4 FROM samples WHERE file_path = 'thursday.docx'
UNION ALL
SELECT id, 'signer4',   'Подписант',                 'SIGNATORY', 5 FROM samples WHERE file_path = 'thursday.docx';


-- ---------------------------------------------------------------------
-- Пятница, документ 2
-- У signer1 в шаблоне нет ${signer1.post} — генератор просто не найдёт
-- этот плейсхолдер в файле, лишняя пара в Map безвредна.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date1',    'Дата 1',        'DATE',      1 FROM samples WHERE file_path = 'friday-2.docx'
UNION ALL
SELECT id, 'date2',    'Дата 2',        'DATE',      2 FROM samples WHERE file_path = 'friday-2.docx'
UNION ALL
SELECT id, 'signDate', 'Дата подписи',  'DATE',      3 FROM samples WHERE file_path = 'friday-2.docx'
UNION ALL
SELECT id, 'signer1',  'Подписант 1',   'SIGNATORY', 4 FROM samples WHERE file_path = 'friday-2.docx'
UNION ALL
SELECT id, 'signer2',  'Подписант 2',   'SIGNATORY', 5 FROM samples WHERE file_path = 'friday-2.docx';


-- ---------------------------------------------------------------------
-- Пятница, документ 3 — расписание на неделю
-- 'day' типа WEEK_START: пользователь вводит одну дату (понедельник),
-- сервис разворачивает её в ${day1}..${day7}.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'dateFrom', 'Период с',              'DATE',       1 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'dateTo',   'Период по',             'DATE',       2 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'day',      'Первый день недели',    'WEEK_START', 3 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'theme1',   'Тема 1',                'TEXT',       4 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'theme2',   'Тема 2',                'TEXT',       5 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'theme3',   'Тема 3',                'TEXT',       6 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'theme4',   'Тема 4',                'TEXT',       7 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'theme5',   'Тема 5',                'TEXT',       8 FROM samples WHERE file_path = 'friday-3.docx'
UNION ALL
SELECT id, 'signer2',  'Подписант',             'SIGNATORY',  9 FROM samples WHERE file_path = 'friday-3.docx';


-- ---------------------------------------------------------------------
-- Пятница, документ 4.1
-- Варианты 4.2 и 4.3 добавляются отдельной миграцией, когда появятся:
-- плейсхолдеры у них те же, отличается только текст в файле.
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',    'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'friday-4-1.docx'
UNION ALL
SELECT id, 'signer2', 'Подписант',      'SIGNATORY', 2 FROM samples WHERE file_path = 'friday-4-1.docx';


-- ---------------------------------------------------------------------
-- Ежемесячный
-- ---------------------------------------------------------------------
INSERT INTO sample_fields (sample_id, placeholder, form_name, type, position)
SELECT id, 'date',   'Дата документа', 'DATE',      1 FROM samples WHERE file_path = 'monthly.docx'
UNION ALL
SELECT id, 'signer', 'Подписант',      'SIGNATORY', 2 FROM samples WHERE file_path = 'monthly.docx';
