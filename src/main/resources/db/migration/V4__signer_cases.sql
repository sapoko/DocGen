-- =====================================================================
-- V4__signer_cases.sql
--
-- Раскладываем подписанта на части и добавляем падежные формы.
-- Падежи, которые реально встречаются в шаблонах:
--   именительный  — «сержант И. Иванов»              (почти везде)
--   родительный   — «сержанта Иванова Ивана Ивановича» (Пт-3)
--   творительный  — «сержантом Ивановым И.И.»          (Пт-2)
--
-- rank_short — сокращённое звание («ст. с-т»), нужно для ${signerN.smallRank}
-- в ПЛАН ПХД. Из полного звания не выводится, поэтому отдельная колонка.
--
-- Инициалы НЕ храним: они не склоняются и собираются из given_names_nom.
--
-- given_names — имя и отчество одной строкой («Иван Иванович»).
-- Разделять их на два поля незачем: по отдельности они нигде не нужны.
-- =====================================================================

-- rank -> rank_nom: старая колонка и была именительным падежом
ALTER TABLE signers RENAME COLUMN rank TO rank_nom;

ALTER TABLE signers
    ADD COLUMN rank_gen         VARCHAR(50),
    ADD COLUMN rank_short       VARCHAR(30),
    ADD COLUMN rank_ins         VARCHAR(50),
    ADD COLUMN last_name_nom    VARCHAR(60),
    ADD COLUMN last_name_gen    VARCHAR(60),
    ADD COLUMN last_name_ins    VARCHAR(60),
    ADD COLUMN given_names_nom  VARCHAR(80),
    ADD COLUMN given_names_gen  VARCHAR(80),
    ADD COLUMN given_names_ins  VARCHAR(80);

-- Заполнение существующих строк — грубое, чтобы пройти NOT NULL.
-- Падежные формы совпадут с именительным: их придётся поправить руками
-- через форму подписантов. Записей в справочнике сейчас единицы.
UPDATE signers SET
    rank_gen        = rank_nom,
    rank_short      = rank_nom,
    rank_ins        = rank_nom,
    last_name_nom   = split_part(full_name, ' ', 1),
    last_name_gen   = split_part(full_name, ' ', 1),
    last_name_ins   = split_part(full_name, ' ', 1),
    given_names_nom = trim(substring(full_name from position(' ' in full_name || ' '))),
    given_names_gen = trim(substring(full_name from position(' ' in full_name || ' '))),
    given_names_ins = trim(substring(full_name from position(' ' in full_name || ' ')));

ALTER TABLE signers
    ALTER COLUMN rank_gen        SET NOT NULL,
    ALTER COLUMN rank_short      SET NOT NULL,
    ALTER COLUMN rank_ins        SET NOT NULL,
    ALTER COLUMN last_name_nom   SET NOT NULL,
    ALTER COLUMN last_name_gen   SET NOT NULL,
    ALTER COLUMN last_name_ins   SET NOT NULL,
    ALTER COLUMN given_names_nom SET NOT NULL,
    ALTER COLUMN given_names_gen SET NOT NULL,
    ALTER COLUMN given_names_ins SET NOT NULL;

-- Полное имя теперь собирается из частей и как отдельная колонка не нужно.
ALTER TABLE signers DROP COLUMN full_name;
