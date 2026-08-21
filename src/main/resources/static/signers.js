'use strict';

const API = '/api/v1';

/**
 * Поля формы. Три падежные тройки плюс отдельные поля.
 * key — имя поля в SignerRequest, оно же приходит обратно в SignerDto.
 */
const FIELDS = [
    { key: 'lastNameNom',   label: 'Фамилия (именительный)',      hint: 'Иванов' },
    { key: 'lastNameGen',   label: 'Фамилия (родительный)',       hint: 'Иванова' },
    { key: 'lastNameIns',   label: 'Фамилия (творительный)',      hint: 'Ивановым' },
    { key: 'givenNamesNom', label: 'Имя и отчество (именительный)', hint: 'Иван Иванович' },
    { key: 'givenNamesGen', label: 'Имя и отчество (родительный)',  hint: 'Ивана Ивановича' },
    { key: 'givenNamesIns', label: 'Имя и отчество (творительный)', hint: 'Иваном Ивановичем' },
    { key: 'rankNom',       label: 'Звание (именительный)',       hint: 'старший сержант' },
    { key: 'rankGen',       label: 'Звание (родительный)',        hint: 'старшего сержанта' },
    { key: 'rankIns',       label: 'Звание (творительный)',       hint: 'старшим сержантом' },
    { key: 'rankShort',     label: 'Звание сокращённо',           hint: 'ст. с-т' },
    { key: 'post',          label: 'Должность',                   hint: 'Начальник отдела' }
];

let groups = [];        // [{name, displayName}]
let editingId = null;

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

async function api(path, options = {}) {
    const token = csrfToken();
    const res = await fetch(API + path, {
        ...options,
        headers: {
            'Accept': 'application/json',
            ...(token ? { 'X-XSRF-TOKEN': token } : {}),
            ...(options.headers || {})
        }
    });

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
        } catch (ignored) { /* тело не JSON */ }
        const error = new Error(message);
        error.details = details;
        throw error;
    }

    return res;
}

// ---------------------------------------------------------------- список

function groupLabel(name) {
    const found = groups.find(g => g.name === name);
    return found ? found.displayName : name;
}

async function loadSigners() {
    const list = $('signer-list');
    list.innerHTML = '<div class="empty">Загрузка…</div>';

    try {
        const grouped = await api('/signers').then(r => r.json());
        const entries = Object.entries(grouped);

        list.innerHTML = '';
        $('signer-count').textContent = entries.reduce((n, [, v]) => n + v.length, 0) + ' чел';

        if (!entries.length) {
            list.innerHTML = '<div class="empty">Справочник пуст</div>';
            return;
        }

        entries.forEach(([group, signers]) => {
            const head = document.createElement('div');
            head.className = 'doc-group';
            head.textContent = groupLabel(group);
            list.append(head);

            signers.forEach(s => list.append(signerButton(s)));
        });
    } catch (e) {
        list.innerHTML = '<div class="empty">Не удалось загрузить справочник</div>';
        showAlert(e.message, 'danger', e.details || []);
    }
}

function signerButton(signer) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'doc-item';
    btn.dataset.id = signer.id;
    btn.textContent = signer.rankShort + ' ' + signer.lastNameNom + ' ' + signer.givenNamesNom;

    const note = document.createElement('span');
    note.className = 'doc-note';
    note.textContent = signer.post;
    btn.append(note);

    btn.addEventListener('click', () => {
        document.querySelectorAll('.doc-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        openSigner(signer);
    });
    return btn;
}

// ---------------------------------------------------------------- форма

function renderForm(signer = null) {
    editingId = signer ? signer.id : null;
    $('form-title').textContent = signer
        ? 'Редактирование: ' + signer.lastNameNom
        : 'Новый подписант';

    const body = $('form-body');
    body.innerHTML = '';

    // Группа — выпадающий список из справочника enum'а на сервере.
    const groupWrap = document.createElement('div');
    groupWrap.className = 'field';
    const groupLabelEl = document.createElement('label');
    groupLabelEl.textContent = 'Раздел';
    groupLabelEl.htmlFor = 'f-bossGroup';
    const groupSelect = document.createElement('select');
    groupSelect.id = 'f-bossGroup';
    groupSelect.dataset.key = 'bossGroup';
    groups.forEach(g => groupSelect.append(new Option(g.displayName, g.name)));
    if (signer) groupSelect.value = signer.bossGroup;
    groupWrap.append(groupLabelEl, groupSelect);
    body.append(groupWrap);

    FIELDS.forEach(f => {
        const wrap = document.createElement('div');
        wrap.className = 'field';

        const label = document.createElement('label');
        label.textContent = f.label;
        label.htmlFor = 'f-' + f.key;

        const input = document.createElement('input');
        input.type = 'text';
        input.id = 'f-' + f.key;
        input.dataset.key = f.key;
        input.placeholder = f.hint;
        if (signer && signer[f.key] != null) input.value = signer[f.key];

        wrap.append(label, input);
        body.append(wrap);
    });

    const actions = document.createElement('div');
    actions.className = 'form-actions';

    const save = document.createElement('button');
    save.type = 'button';
    save.className = 'btn btn-primary';
    save.id = 'save';
    save.textContent = signer ? 'Сохранить' : 'Создать';
    save.addEventListener('click', save_);
    actions.append(save);

    if (signer) {
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'btn btn-red';
        remove.textContent = 'Удалить';
        remove.addEventListener('click', () => removeSigner(signer));
        actions.append(remove);
    }

    body.append(actions);
}

function openSigner(signer) {
    clearAlert();
    renderForm(signer);
}

function collectForm() {
    const payload = {};
    document.querySelectorAll('#form-body [data-key]').forEach(el => {
        payload[el.dataset.key] = el.value;
    });
    return payload;
}

async function save_() {
    clearAlert();
    const button = $('save');
    button.disabled = true;

    const path = editingId ? '/signers/' + editingId : '/signers';
    const method = editingId ? 'PUT' : 'POST';

    try {
        await api(path, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(collectForm())
        });

        showAlert(editingId ? 'Подписант обновлён' : 'Подписант добавлен', 'success');
        await loadSigners();
        renderForm();
    } catch (e) {
        showAlert(e.message, 'danger', e.details || []);
    } finally {
        button.disabled = false;
    }
}

async function removeSigner(signer) {
    const name = signer.lastNameNom + ' ' + signer.givenNamesNom;
    if (!confirm('Убрать из справочника: ' + name + '?\nВ уже созданных документах он останется.')) {
        return;
    }
    clearAlert();
    try {
        await api('/signers/' + signer.id, { method: 'DELETE' });
        showAlert('Подписант убран из справочника', 'success');
        await loadSigners();
        renderForm();
    } catch (e) {
        showAlert(e.message, 'danger', e.details || []);
    }
}

// ---------------------------------------------------------------- старт

async function init() {
    try {
        groups = await api('/signers/groups').then(r => r.json());
    } catch (e) {
        showAlert('Не удалось загрузить список разделов: ' + e.message);
    }

    renderForm();
    await loadSigners();

    $('new').addEventListener('click', () => {
        document.querySelectorAll('.doc-item').forEach(b => b.classList.remove('active'));
        clearAlert();
        renderForm();
    });

    $('logout').addEventListener('click', async () => {
        const token = csrfToken();
        await fetch('/logout', {
            method: 'POST',
            headers: token ? { 'X-XSRF-TOKEN': token } : {}
        });
        location.href = '/login.html?logout';
    });
}

init();
