'use strict';

const API = '/api/v1';
const WEEKDAYS = ['понедельник', 'вторник', 'среда', 'четверг', 'пятница', 'суббота', 'воскресенье'];

const HOSPITAL_COLUMNS = [
    { key: 'rank',          label: 'Звание',        type: 'text' },
    { key: 'fullName',      label: 'ФИО',           type: 'text' },
    { key: 'platoon',       label: 'Взвод / рота',  type: 'text' },
    { key: 'hospitalTitle', label: 'Госпиталь',     type: 'text' },
    { key: 'diagnosis',     label: 'Диагноз',       type: 'text' },
    { key: 'admittedAt',    label: 'С какого числа', type: 'date' }
];

let signerGroups = {};   // { bossGroup: [SignerDto] }
let currentSample = null;

// ---------------------------------------------------------------- утилиты

const $ = (id) => document.getElementById(id);

function csrfToken() {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}

function showAlert(text, kind = 'danger', details = []) {
    const box = $('alert');
    box.className = 'alert-bar ' + kind;
    box.innerHTML = '';
    box.append(text);
    if (details.length) {
        const ul = document.createElement('ul');
        details.forEach(d => {
            const li = document.createElement('li');
            li.textContent = d;
            ul.append(li);
        });
        box.append(ul);
    }
    box.hidden = false;
}

function clearAlert() { $('alert').hidden = true; }

/** Общий вызов API: ловит разлогин и разбирает ErrorResponse. */
async function api(path, options = {}) {
    const res = await fetch(API + path, {
        ...options,
        headers: { 'Accept': 'application/json', ...(options.headers || {}) }
    });

    // Сессия истекла — Security увёл на форму входа.
    if (res.redirected && res.url.includes('login')) {
        location.href = '/login.html';
        throw new Error('redirect to login');
    }

    if (!res.ok) {
        let message = 'Ошибка ' + res.status;
        let details = [];
        try {
            const body = await res.json();
            if (body.message) message = body.message;
            if (Array.isArray(body.errors)) details = body.errors;
        } catch (ignored) { /* тело не JSON — оставляем статус */ }
        const error = new Error(message);
        error.details = details;
        throw error;
    }

    return res;
}

// ---------------------------------------------------------------- загрузка списка

function isoDate(d) {
    return d.getFullYear() + '-'
        + String(d.getMonth() + 1).padStart(2, '0') + '-'
        + String(d.getDate()).padStart(2, '0');
}

async function loadDocuments(dateStr) {
    const list = $('doc-list');
    list.innerHTML = '<div class="empty">Загрузка…</div>';

    try {
        const [daily, monthly] = await Promise.all([
            api('/samples?date=' + dateStr).then(r => r.json()),
            api('/samples/monthly').then(r => r.json())
        ]);

        list.innerHTML = '';
        $('doc-count').textContent = (daily.length + monthly.length) + ' шт';

        if (!daily.length && !monthly.length) {
            list.innerHTML = '<div class="empty">На этот день документов нет</div>';
            return;
        }

        if (daily.length) {
            list.append(groupHeader('На выбранный день'));
            daily.forEach(s => list.append(docButton(s)));
        }
        if (monthly.length) {
            list.append(groupHeader('Ежемесячные'));
            monthly.forEach(s => list.append(docButton(s)));
        }
    } catch (e) {
        list.innerHTML = '<div class="empty">Не удалось загрузить список</div>';
        showAlert(e.message, 'danger', e.details || []);
    }
}

function groupHeader(text) {
    const div = document.createElement('div');
    div.className = 'doc-group';
    div.textContent = text;
    return div;
}

function docButton(sample) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'doc-item';
    btn.dataset.id = sample.id;
    btn.textContent = sample.publicName;

    if (sample.hasHospitalTable) {
        const note = document.createElement('span');
        note.className = 'doc-note';
        note.textContent = 'со списком госпитализированных';
        btn.append(note);
    }

    btn.addEventListener('click', () => {
        document.querySelectorAll('.doc-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        openSample(sample.id);
    });
    return btn;
}

// ---------------------------------------------------------------- форма

async function openSample(id) {
    clearAlert();
    const body = $('form-body');
    body.innerHTML = '<div class="empty">Загрузка…</div>';

    try {
        currentSample = await api('/samples/' + id).then(r => r.json());
        $('form-title').textContent = currentSample.publicName;
        $('prefill').disabled = false;
        renderForm(currentSample);
    } catch (e) {
        body.innerHTML = '<div class="empty">Не удалось открыть документ</div>';
        showAlert(e.message, 'danger', e.details || []);
    }
}

function renderForm(sample) {
    const body = $('form-body');
    body.innerHTML = '';

    const fields = [...sample.sampleFields].sort((a, b) => a.position - b.position);
    fields.forEach(f => body.append(renderField(f)));

    if (sample.hasHospitalTable) {
        body.append(renderHospitalBlock());
        addHospitalRow();
    }

    const actions = document.createElement('div');
    actions.className = 'form-actions';

    const submit = document.createElement('button');
    submit.type = 'button';
    submit.className = 'btn btn-primary';
    submit.id = 'generate';
    submit.textContent = 'Сформировать документ';
    submit.addEventListener('click', generate);

    actions.append(submit);
    body.append(actions);
}

function renderField(field) {
    const wrap = document.createElement('div');
    wrap.className = 'field';

    const label = document.createElement('label');
    label.textContent = field.formName;
    label.htmlFor = 'f' + field.id;
    wrap.append(label);

    let control;
    if (field.type === 'SIGNATORY') {
        control = document.createElement('select');
        control.append(new Option('— не выбрано —', ''));
        Object.entries(signerGroups).forEach(([group, signers]) => {
            const optgroup = document.createElement('optgroup');
            optgroup.label = group;
            signers.forEach(s => {
                optgroup.append(new Option(s.rankShort + ' ' + s.lastNameNom + ' ' + s.givenNamesNom + ' — ' + s.post, s.id));
            });
            control.append(optgroup);
        });
    } else {
        control = document.createElement('input');
        control.type = field.type === 'NUMBER' ? 'number'
                     : (field.type === 'DATE' || field.type === 'WEEK_START') ? 'date'
                     : 'text';
    }

    control.id = 'f' + field.id;
    control.dataset.fieldId = field.id;
    control.dataset.fieldType = field.type;
    wrap.append(control);

    if (field.type === 'WEEK_START') {
        const hint = document.createElement('div');
        hint.className = 'hint';
        hint.textContent = 'Первый день недели — остальные шесть проставятся автоматически';
        wrap.append(hint);
    }

    return wrap;
}

function renderHospitalBlock() {
    const block = document.createElement('div');
    block.className = 'hr-block';

    const head = document.createElement('div');
    head.className = 'hr-head';

    const title = document.createElement('div');
    title.className = 'hr-title';
    title.textContent = 'Госпитализированные';

    const add = document.createElement('button');
    add.type = 'button';
    add.className = 'btn btn-ghost';
    add.textContent = '+ Добавить строку';
    add.addEventListener('click', () => addHospitalRow());

    head.append(title, add);

    const scroll = document.createElement('div');
    scroll.className = 'hr-scroll';

    const table = document.createElement('table');
    table.className = 'tbl';

    const thead = document.createElement('thead');
    const tr = document.createElement('tr');
    tr.append(th('№'));
    HOSPITAL_COLUMNS.forEach(c => tr.append(th(c.label)));
    tr.append(th(''));
    thead.append(tr);

    const tbody = document.createElement('tbody');
    tbody.id = 'hr-body';

    table.append(thead, tbody);
    scroll.append(table);
    block.append(head, scroll);
    return block;
}

function th(text) {
    const el = document.createElement('th');
    el.textContent = text;
    return el;
}

function addHospitalRow(values = {}) {
    const tbody = $('hr-body');
    if (!tbody) return;

    const tr = document.createElement('tr');

    const num = document.createElement('td');
    num.className = 'num';
    tr.append(num);

    HOSPITAL_COLUMNS.forEach(col => {
        const td = document.createElement('td');
        const input = document.createElement('input');
        input.type = col.type;
        input.dataset.key = col.key;
        if (values[col.key] != null) input.value = values[col.key];
        td.append(input);
        tr.append(td);
    });

    const actions = document.createElement('td');
    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'btn btn-red';
    remove.textContent = '✕';
    remove.addEventListener('click', () => { tr.remove(); renumberRows(); });
    actions.append(remove);
    tr.append(actions);

    tbody.append(tr);
    renumberRows();
}

function renumberRows() {
    const tbody = $('hr-body');
    if (!tbody) return;
    [...tbody.rows].forEach((row, i) => { row.cells[0].textContent = i + 1; });
}

// ---------------------------------------------------------------- сбор и отправка

function collectRequest() {
    const valueFromForm = [];

    document.querySelectorAll('[data-field-id]').forEach(control => {
        const id = Number(control.dataset.fieldId);
        if (control.dataset.fieldType === 'SIGNATORY') {
            valueFromForm.push({ sampleFieldId: id, signerId: control.value ? Number(control.value) : null });
        } else {
            valueFromForm.push({ sampleFieldId: id, value: control.value });
        }
    });

    const hospitalRowFromForm = [];
    const tbody = $('hr-body');
    if (tbody) {
        [...tbody.rows].forEach(row => {
            const entry = {};
            row.querySelectorAll('[data-key]').forEach(input => { entry[input.dataset.key] = input.value; });
            hospitalRowFromForm.push(entry);
        });
    }

    return { sampleId: currentSample.id, valueFromForm, hospitalRowFromForm };
}

/** Имя файла лежит в Content-Disposition; кириллица приходит в filename*=UTF-8''… */
function fileNameFrom(header, fallback) {
    if (!header) return fallback;
    const encoded = header.match(/filename\*=UTF-8''([^;]+)/i);
    if (encoded) {
        try { return decodeURIComponent(encoded[1]); } catch (ignored) { /* падать из-за имени не будем */ }
    }
    const plain = header.match(/filename="?([^";]+)"?/i);
    return plain ? plain[1] : fallback;
}

async function generate() {
    clearAlert();
    const button = $('generate');
    button.disabled = true;
    button.textContent = 'Формирую…';

    try {
        const token = csrfToken();
        const res = await api('/documents', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'X-XSRF-TOKEN': token } : {})
            },
            body: JSON.stringify(collectRequest())
        });

        const blob = await res.blob();
        const name = fileNameFrom(res.headers.get('Content-Disposition'), 'document.docx');

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = name;
        document.body.append(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);

        showAlert('Документ «' + name + '» сформирован', 'success');
    } catch (e) {
        showAlert(e.message, 'danger', e.details || []);
    } finally {
        button.disabled = false;
        button.textContent = 'Сформировать документ';
    }
}

// ---------------------------------------------------------------- «как в прошлый раз»

async function prefill() {
    if (!currentSample) return;
    clearAlert();

    try {
        const last = await api('/documents/last?sampleId=' + currentSample.id).then(r => r.json());

        last.inputs.forEach(input => {
            const control = document.querySelector('[data-field-id="' + input.sampleField.id + '"]');
            if (!control) return;
            control.value = input.signerDto ? input.signerDto.id : (input.value || '');
        });

        const tbody = $('hr-body');
        if (tbody) {
            tbody.innerHTML = '';
            last.hospitalRows.forEach(row => addHospitalRow(row));
            if (!last.hospitalRows.length) addHospitalRow();
        }

        showAlert('Подставлены значения от ' + new Date(last.createdAt).toLocaleDateString('ru-RU')
            + '. Проверьте даты перед отправкой.', 'success');
    } catch (e) {
        showAlert(e.message, 'danger', e.details || []);
    }
}

// ---------------------------------------------------------------- старт

async function init() {
    const dateInput = $('date');
    dateInput.value = isoDate(new Date());
    updateWeekday();

    try {
        signerGroups = await api('/signers').then(r => r.json());
    } catch (e) {
        showAlert('Не удалось загрузить справочник подписантов: ' + e.message);
    }

    await loadDocuments(dateInput.value);

    dateInput.addEventListener('change', () => {
        updateWeekday();
        loadDocuments(dateInput.value);
    });

    $('today').addEventListener('click', () => {
        dateInput.value = isoDate(new Date());
        updateWeekday();
        loadDocuments(dateInput.value);
    });

    $('prefill').addEventListener('click', prefill);

    $('logout').addEventListener('click', async () => {
        const token = csrfToken();
        await fetch('/logout', {
            method: 'POST',
            headers: token ? { 'X-XSRF-TOKEN': token } : {}
        });
        location.href = '/login.html?logout';
    });
}

function updateWeekday() {
    const value = $('date').value;
    if (!value) return;
    const day = new Date(value + 'T00:00:00').getDay(); // 0 = воскресенье
    $('weekday').textContent = WEEKDAYS[(day + 6) % 7].toUpperCase();
}

init();
