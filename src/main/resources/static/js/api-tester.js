(() => {
  'use strict';

  const STORAGE_HISTORY = 'opsconsole-api-history';
  const STORAGE_COLLECTIONS = 'opsconsole-api-collections';
  const STORAGE_DRAFT = 'opsconsole-api-draft';
  const STORAGE_PANELS = 'opsconsole-api-panels';
  const STORAGE_SPLIT = 'opsconsole-api-split';
  const MAX_HISTORY = 50;

  const $ = id => document.getElementById(id);

  const methodSelect = $('atMethod');
  const urlInput = $('atUrl');
  const sendBtn = $('atSendBtn');
  const builderTabs = document.querySelectorAll('[data-builder-tab]');
  const builderPanels = document.querySelectorAll('[data-builder-panel]');
  const responseTabs = document.querySelectorAll('[data-response-tab]');
  const paramsBody = $('atParamsBody');
  const headersBody = $('atHeadersBody');
  const bodyEditor = $('atBodyEditor');
  const bodyType = $('atBodyType');
  const authType = $('atAuthType');
  const authBearer = $('atAuthBearer');
  const authBasicUser = $('atAuthBasicUser');
  const authBasicPass = $('atAuthBasicPass');
  const authBearerWrap = $('atAuthBearerWrap');
  const authBasicWrap = $('atAuthBasicWrap');
  const responseStatus = $('atResponseStatus');
  const responseTime = $('atResponseTime');
  const responseSize = $('atResponseSize');
  const responseBody = $('atResponseBody');
  const responseHeaders = $('atResponseHeaders');
  const responseHeadersWrap = $('atResponseHeadersWrap');
  const historyList = $('atHistoryList');
  const collectionsList = $('atCollectionsList');
  const collectionSearch = $('atCollectionSearch');
  const paramsBadge = $('atParamsBadge');
  const headersBadge = $('atHeadersBadge');
  const requestPane = $('atRequestPane');
  const responsePane = $('atResponsePane');
  const splitter = $('atSplitter');
  const collectionsPanel = $('atCollectionsPanel');
  const historyPanel = $('atHistoryPanel');
  const saveModal = $('atSaveModal');
  const toastEl = $('atToast');
  const clearHistoryBtn = $('atClearHistoryBtn');
  const copyResponseBtn = $('atCopyResponseBtn');

  let lastResponseRaw = '';
  let lastResponsePretty = '';
  let lastResponseHtml = '';
  let activeResponseTab = 'pretty';
  let activeItemId = null;
  let draftTimer = null;

  const DEFAULT_COLLECTIONS = [
    {
      id: 'col-ops',
      name: 'OpsConsole',
      items: [
        { id: 'req-health', method: 'GET', url: `${location.origin}/actuator/health`, label: 'Health check' },
        { id: 'req-dash', method: 'GET', url: `${location.origin}/`, label: 'Dashboard' },
      ],
    },
    {
      id: 'col-examples',
      name: 'Examples',
      items: [
        { id: 'req-httpbin-get', method: 'GET', url: 'https://httpbin.org/get', label: 'httpbin GET' },
        {
          id: 'req-httpbin-post',
          method: 'POST',
          url: 'https://httpbin.org/post',
          label: 'httpbin POST',
          body: '{\n  "hello": "world"\n}',
          bodyType: 'json',
          headers: [{ key: 'Content-Type', value: 'application/json', enabled: true }],
        },
      ],
    },
  ];

  /* ── Helpers ── */
  function uid() {
    return `id-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 7)}`;
  }

  function el(tag, cls, html) {
    const n = document.createElement(tag);
    if (cls) n.className = cls;
    if (html != null) n.innerHTML = html;
    return n;
  }

  function loadJson(key, fallback) {
    try {
      const raw = localStorage.getItem(key);
      return raw ? JSON.parse(raw) : fallback;
    } catch {
      return fallback;
    }
  }

  function saveJson(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  }

  function escapeAttr(s) {
    return String(s).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');
  }

  function escapeHtml(s) {
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function methodClass(m) {
    return `at-method-${(m || 'GET').toLowerCase()}`;
  }

  function syncMethodWrap(method) {
    const wrap = $('atMethodWrap');
    if (!wrap) return;
    wrap.className = `at-method-wrap ${methodClass(method || methodSelect?.value)}`;
  }

  let toastTimer;
  function showToast(msg, type = 'success') {
    if (!toastEl) return;
    toastEl.className = `at-toast at-toast-${type} at-toast-visible`;
    toastEl.textContent = msg;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toastEl.classList.remove('at-toast-visible'), 2400);
  }

  function switchTabs(tabs, panels, activeName, tabAttr, panelAttr) {
    tabs.forEach(t => t.classList.toggle('at-tab-active', t.dataset[tabAttr] === activeName));
    panels.forEach(p => p.classList.toggle('at-panel-active', p.dataset[panelAttr] === activeName));
  }

  /* ── KV rows ── */
  function createKvRow(key = '', value = '', enabled = true) {
    const tr = el('tr');
    tr.innerHTML = `
      <td><input type="checkbox" class="at-kv-enabled rounded border-outline text-secondary focus:ring-secondary" ${enabled ? 'checked' : ''}></td>
      <td><input type="text" class="at-kv-key" placeholder="Key" value="${escapeAttr(key)}"></td>
      <td><input type="text" class="at-kv-val" placeholder="Value" value="${escapeAttr(value)}"></td>
      <td><button type="button" class="at-kv-remove" title="Remove">×</button></td>`;
    tr.querySelector('.at-kv-remove').addEventListener('click', () => {
      tr.remove();
      updateBadges();
      scheduleDraftSave();
    });
    tr.querySelectorAll('input').forEach(inp => {
      inp.addEventListener('input', () => {
        updateBadges();
        scheduleDraftSave();
      });
      inp.addEventListener('change', () => {
        updateBadges();
        scheduleDraftSave();
      });
    });
    return tr;
  }

  function fillKvTable(tbody, rows, addEmpty = true) {
    tbody.innerHTML = '';
    (rows || []).forEach(r => tbody.appendChild(createKvRow(r.key, r.value, r.enabled !== false)));
    if (addEmpty) tbody.appendChild(createKvRow());
  }

  function readKvRows(tbody) {
    return [...tbody.querySelectorAll('tr')].map(tr => ({
      enabled: tr.querySelector('.at-kv-enabled')?.checked ?? false,
      key: tr.querySelector('.at-kv-key')?.value?.trim() ?? '',
      value: tr.querySelector('.at-kv-val')?.value ?? '',
    })).filter(r => r.key);
  }

  function updateBadges() {
    const pCount = readKvRows(paramsBody).filter(r => r.enabled).length;
    const hCount = readKvRows(headersBody).filter(r => r.enabled).length;
    if (paramsBadge) {
      paramsBadge.textContent = pCount;
      paramsBadge.classList.toggle('hidden', pCount === 0);
    }
    if (headersBadge) {
      headersBadge.textContent = hCount;
      headersBadge.classList.toggle('hidden', hCount === 0);
    }
  }

  /* ── URL / params sync ── */
  function syncUrlParams() {
    const base = urlInput.value.split('?')[0];
    const params = readKvRows(paramsBody).filter(r => r.enabled);
    if (!params.length) {
      urlInput.value = base;
      return;
    }
    const qs = new URLSearchParams();
    params.forEach(p => qs.append(p.key, p.value));
    urlInput.value = `${base}?${qs.toString()}`;
  }

  function parseUrlToParams() {
    try {
      const u = new URL(urlInput.value);
      fillKvTable(paramsBody, [...u.searchParams.entries()].map(([key, value]) => ({ key, value, enabled: true })));
      urlInput.value = u.origin + u.pathname;
      updateBadges();
    } catch { /* keep */ }
  }

  /* ── Request state ── */
  function captureRequest() {
    syncUrlParams();
    return {
      id: activeItemId,
      method: methodSelect.value,
      url: urlInput.value.trim(),
      params: readKvRows(paramsBody),
      headers: readKvRows(headersBody),
      authType: authType.value,
      authBearer: authBearer.value,
      authBasicUser: authBasicUser.value,
      authBasicPass: authBasicPass.value,
      bodyType: bodyType.value,
      body: bodyEditor.value,
    };
  }

  function applyRequest(req, itemId = null) {
    activeItemId = itemId;
    methodSelect.value = req.method || 'GET';
    syncMethodWrap(req.method);
    urlInput.value = req.url || '';

    fillKvTable(paramsBody, req.params);
    fillKvTable(headersBody, req.headers);

    authType.value = req.authType || 'none';
    authBearer.value = req.authBearer || '';
    authBasicUser.value = req.authBasicUser || '';
    authBasicPass.value = req.authBasicPass || '';
    updateAuthVisibility();

    bodyType.value = req.bodyType || (req.body ? 'json' : 'none');
    bodyEditor.value = req.body || '';
    updateBodyEditorState();

    if (req.url?.includes('?') && (!req.params || !req.params.length)) {
      parseUrlToParams();
    }
    updateBadges();
    highlightActiveItems();
    scheduleDraftSave();
  }

  function newRequest(showNotice = false) {
    activeItemId = null;
    applyRequest({
      method: 'GET',
      url: `${location.origin}/actuator/health`,
      params: [{ key: 'format', value: 'json', enabled: false }],
      headers: [{ key: 'Accept', value: 'application/json', enabled: true }],
      authType: 'none',
      bodyType: 'none',
      body: '',
    });
    if (showNotice) showToast('New request', 'info');
  }

  function scheduleDraftSave() {
    clearTimeout(draftTimer);
    draftTimer = setTimeout(() => saveJson(STORAGE_DRAFT, captureRequest()), 400);
  }

  /* ── Auth / body UI ── */
  function updateAuthVisibility() {
    const t = authType.value;
    authBearerWrap?.classList.toggle('hidden', t !== 'bearer');
    authBasicWrap?.classList.toggle('hidden', t !== 'basic');
    $('atAuthNoneHint')?.classList.toggle('hidden', t !== 'none');
  }

  function updateBodyEditorState() {
    const t = bodyType.value;
    const hasBody = t !== 'none';
    bodyEditor.disabled = !hasBody;
    bodyEditor.classList.toggle('at-body-disabled', !hasBody);
    bodyEditor.placeholder = hasBody
      ? (t === 'json' ? '{\n  "key": "value"\n}' : 'Raw request body')
      : 'Select JSON or Raw text above to add a body';
    const showFormat = t === 'json';
    $('atFormatBodyBtn')?.classList.toggle('hidden', !showFormat);
  }

  function buildHeaders() {
    const headers = readKvRows(headersBody)
      .filter(r => r.enabled && r.key)
      .map(r => ({ key: r.key, value: r.value }));

    if (authType.value === 'bearer' && authBearer.value.trim()) {
      if (!headers.some(h => h.key.toLowerCase() === 'authorization')) {
        headers.push({ key: 'Authorization', value: `Bearer ${authBearer.value.trim()}` });
      }
    } else if (authType.value === 'basic' && authBasicUser.value) {
      if (!headers.some(h => h.key.toLowerCase() === 'authorization')) {
        const token = btoa(`${authBasicUser.value}:${authBasicPass.value || ''}`);
        headers.push({ key: 'Authorization', value: `Basic ${token}` });
      }
    }

    const method = methodSelect.value;
    const body = getRequestBody();
    if (['POST', 'PUT', 'PATCH'].includes(method) && body) {
      const ct = bodyType.value === 'json' ? 'application/json' : 'text/plain';
      if (!headers.some(h => h.key.toLowerCase() === 'content-type')) {
        headers.push({ key: 'Content-Type', value: ct });
      }
    }
    return headers;
  }

  function getRequestBody() {
    if (bodyType.value === 'none') return null;
    const text = bodyEditor.value;
    return ['POST', 'PUT', 'PATCH'].includes(methodSelect.value) ? text : null;
  }

  function formatBodyJson() {
    try {
      bodyEditor.value = JSON.stringify(JSON.parse(bodyEditor.value), null, 2);
      showToast('JSON formatted', 'success');
      scheduleDraftSave();
    } catch {
      showToast('Invalid JSON', 'error');
    }
  }

  /* ── Response ── */
  function formatBytes(n) {
    if (n < 1024) return `${n} B`;
    if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
    return `${(n / (1024 * 1024)).toFixed(1)} MB`;
  }

  function prettyJson(text) {
    try {
      return JSON.stringify(JSON.parse(text), null, 2);
    } catch {
      return text;
    }
  }

  function highlightJson(text) {
    const pretty = prettyJson(text);
    if (pretty === text && !text.trim().startsWith('{') && !text.trim().startsWith('[')) {
      return escapeHtml(text);
    }
    return pretty
      .replace(/("(\\u[\dA-Fa-f]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, match => {
        let cls = 'at-json-number';
        if (/^"/.test(match)) {
          cls = /:$/.test(match) ? 'at-json-key' : 'at-json-string';
        } else if (/true|false/.test(match)) {
          cls = 'at-json-bool';
        } else if (/null/.test(match)) {
          cls = 'at-json-null';
        }
        return `<span class="${cls}">${escapeHtml(match)}</span>`;
      });
  }

  function statusBadgeClass(code) {
    if (!code) return 'at-status-err';
    if (code >= 200 && code < 300) return 'at-status-ok';
    if (code >= 400 && code < 500) return 'at-status-warn';
    return 'at-status-err';
  }

  function statusText(code) {
    const map = {
      200: 'OK', 201: 'Created', 204: 'No Content',
      400: 'Bad Request', 401: 'Unauthorized', 403: 'Forbidden',
      404: 'Not Found', 422: 'Unprocessable', 500: 'Server Error', 502: 'Bad Gateway', 503: 'Unavailable',
    };
    return map[code] || '';
  }

  function applyResponseView() {
    const showHeaders = activeResponseTab === 'headers';
    responseBody.classList.toggle('hidden', showHeaders);
    responseHeadersWrap.classList.toggle('hidden', !showHeaders);
    if (!showHeaders) {
      if (activeResponseTab === 'raw') {
        responseBody.textContent = lastResponseRaw;
      } else {
        responseBody.innerHTML = lastResponseHtml || escapeHtml(lastResponsePretty);
      }
    }
  }

  function showResponse(status, body, headers, durationMs, sizeBytes, error) {
    if (error) {
      responseStatus.textContent = 'Error';
      responseStatus.className = `at-status-badge ${statusBadgeClass(0)}`;
      lastResponseRaw = error;
      lastResponsePretty = error;
      lastResponseHtml = escapeHtml(error);
    } else {
      responseStatus.textContent = `${status} ${statusText(status)}`.trim();
      responseStatus.className = `at-status-badge ${statusBadgeClass(status)}`;
      lastResponseRaw = body || '';
      lastResponsePretty = prettyJson(lastResponseRaw);
      lastResponseHtml = highlightJson(lastResponseRaw);
    }
    applyResponseView();
    responseTime.textContent = durationMs != null ? `${durationMs} ms` : '—';
    responseSize.textContent = sizeBytes != null ? formatBytes(sizeBytes) : '—';

    responseHeaders.innerHTML = '';
    const entries = Object.entries(headers || {});
    if (!entries.length) {
      responseHeaders.appendChild(el('div', 'at-empty', 'No response headers'));
      return;
    }
    const table = el('table', 'at-kv-table');
    table.innerHTML = '<thead><tr><th>Header</th><th>Value</th></tr></thead>';
    const tbody = el('tbody');
    entries.forEach(([k, v]) => {
      const tr = el('tr');
      tr.innerHTML = `<td class="font-label-md at-header-name">${escapeHtml(k)}</td><td class="font-label-md break-all">${escapeHtml(v)}</td>`;
      tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    responseHeaders.appendChild(table);
  }

  /* ── History ── */
  function relativeTime(ts) {
    const sec = Math.round((Date.now() - ts) / 1000);
    if (sec < 60) return `${sec}s ago`;
    if (sec < 3600) return `${Math.round(sec / 60)}m ago`;
    if (sec < 86400) return `${Math.round(sec / 3600)}h ago`;
    return `${Math.round(sec / 86400)}d ago`;
  }

  function safePath(url) {
    try { return new URL(url).pathname; } catch { return url; }
  }

  function pushHistory(entry) {
    const history = loadJson(STORAGE_HISTORY, []);
    history.unshift({ ...captureRequest(), ...entry, time: Date.now() });
    saveJson(STORAGE_HISTORY, history.slice(0, MAX_HISTORY));
    renderHistory();
  }

  function renderHistory() {
    const history = loadJson(STORAGE_HISTORY, []);
    historyList.innerHTML = '';
    if (!history.length) {
      historyList.appendChild(el('div', 'at-empty', 'No requests yet'));
      return;
    }
    history.forEach((item, idx) => {
      const row = el('div', `at-history-item${item.id === activeItemId ? ' at-active' : ''}`);
      row.dataset.idx = idx;
      row.innerHTML = `
        <div class="at-history-row">
          <span class="at-method-chip ${methodClass(item.method)}">${item.method}</span>
          <span class="at-history-path">${escapeHtml(item.path || safePath(item.url))}</span>
          <span class="at-history-time">${relativeTime(item.time)}</span>
        </div>
        <div class="at-history-meta">
          <span class="at-history-dot ${item.ok ? 'at-dot-ok' : 'at-dot-err'}"></span>
          <span>${escapeHtml(item.statusLabel || 'Error')}</span>
        </div>`;
      row.addEventListener('click', () => {
        activeItemId = item.id || null;
        applyRequest(item);
        renderHistory();
      });
      historyList.appendChild(row);
    });
  }

  /* ── Collections ── */
  function ensureCollectionIds(collections) {
    return collections.map(col => ({
      ...col,
      id: col.id || uid(),
      items: (col.items || []).map(it => ({ ...it, id: it.id || uid() })),
    }));
  }

  function getCollections() {
    const raw = loadJson(STORAGE_COLLECTIONS, null);
    if (!raw) {
      const seeded = ensureCollectionIds(DEFAULT_COLLECTIONS);
      saveJson(STORAGE_COLLECTIONS, seeded);
      return seeded;
    }
    return ensureCollectionIds(raw);
  }

  function saveCollections(collections) {
    saveJson(STORAGE_COLLECTIONS, collections);
  }

  function highlightActiveItems() {
    collectionsList.querySelectorAll('.at-collection-item').forEach(el => {
      el.classList.toggle('at-active', el.dataset.id === activeItemId);
    });
    historyList.querySelectorAll('.at-history-item').forEach(el => {
      el.classList.remove('at-active');
    });
  }

  function renderCollections(filter = '') {
    const collections = getCollections();
    const q = filter.toLowerCase().trim();
    collectionsList.innerHTML = '';

    collections.forEach(col => {
      const items = col.items.filter(it =>
        !q || it.label?.toLowerCase().includes(q) || it.url.toLowerCase().includes(q) || it.method.toLowerCase().includes(q)
      );
      if (!items.length && q) return;

      const folder = el('div', 'at-collection-folder');
      const head = el('div', 'at-collection-head');
      head.innerHTML = `
        <span class="material-symbols-outlined at-folder-icon">folder_open</span>
        <span class="at-collection-name">${escapeHtml(col.name)}</span>
        <span class="at-collection-count">${col.items.length}</span>`;
      folder.appendChild(head);

      const list = el('div', 'at-collection-list');
      if (!items.length) {
        list.appendChild(el('div', 'at-empty at-empty-sm', 'Empty collection'));
      }
      items.forEach(item => {
        const row = el('div', `at-collection-item${item.id === activeItemId ? ' at-active' : ''}`);
        row.dataset.id = item.id;
        row.innerHTML = `
          <span class="at-method-chip ${methodClass(item.method)}">${item.method}</span>
          <span class="at-collection-label">${escapeHtml(item.label || item.url)}</span>
          <button type="button" class="at-item-delete" title="Remove from collection" aria-label="Remove">×</button>`;
        row.querySelector('.at-collection-label').addEventListener('click', () => {
          activeItemId = item.id;
          applyRequest(item, item.id);
          renderCollections(collectionSearch?.value || '');
        });
        row.querySelector('.at-method-chip').addEventListener('click', () => {
          activeItemId = item.id;
          applyRequest(item, item.id);
          renderCollections(collectionSearch?.value || '');
        });
        row.querySelector('.at-item-delete').addEventListener('click', e => {
          e.stopPropagation();
          deleteCollectionItem(col.id, item.id);
        });
        list.appendChild(row);
      });
      folder.appendChild(list);
      collectionsList.appendChild(folder);
    });

    if (!collectionsList.children.length) {
      collectionsList.appendChild(el('div', 'at-empty', 'No matching endpoints'));
    }
  }

  function deleteCollectionItem(colId, itemId) {
    const collections = getCollections();
    const col = collections.find(c => c.id === colId);
    if (!col) return;
    col.items = col.items.filter(i => i.id !== itemId);
    saveCollections(collections);
    if (activeItemId === itemId) activeItemId = null;
    renderCollections(collectionSearch?.value || '');
    showToast('Removed from collection', 'info');
  }

  function openSaveModal() {
    const collections = getCollections();
    const sel = $('atSaveCollection');
    sel.innerHTML = collections.map(c => `<option value="${escapeAttr(c.id)}">${escapeHtml(c.name)}</option>`).join('');
    $('atSaveLabel').value = '';
    $('atSaveNewCollection').checked = false;
    $('atSaveCollectionName').classList.add('hidden');
    $('atSaveCollectionName').value = '';
    saveModal.classList.remove('hidden');
    saveModal.setAttribute('aria-hidden', 'false');
    $('atSaveLabel').focus();
  }

  function closeSaveModal() {
    saveModal.classList.add('hidden');
    saveModal.setAttribute('aria-hidden', 'true');
  }

  function confirmSave() {
    const label = $('atSaveLabel').value.trim();
    if (!label) {
      showToast('Enter a label', 'error');
      return;
    }
    const req = captureRequest();
    req.label = label;
    req.id = uid();

    const collections = getCollections();
    let colId = $('atSaveCollection').value;

    if ($('atSaveNewCollection').checked) {
      const name = $('atSaveCollectionName').value.trim();
      if (!name) {
        showToast('Enter collection name', 'error');
        return;
      }
      colId = uid();
      collections.push({ id: colId, name, items: [] });
    }

    const col = collections.find(c => c.id === colId);
    if (!col) return;
    col.items.push(req);
    saveCollections(collections);
    activeItemId = req.id;
    closeSaveModal();
    renderCollections(collectionSearch?.value || '');
    showToast(`Saved to ${col.name}`, 'success');
  }

  function addCollection() {
    const name = prompt('Collection name');
    if (!name?.trim()) return;
    const collections = getCollections();
    collections.push({ id: uid(), name: name.trim(), items: [] });
    saveCollections(collections);
    renderCollections(collectionSearch?.value || '');
    showToast('Collection created', 'success');
  }

  /* ── cURL export ── */
  function buildCurl() {
    syncUrlParams();
    const method = methodSelect.value;
    const url = urlInput.value.trim();
    if (!url) return null;

    const parts = [`curl -X ${method}`];
    buildHeaders().forEach(h => {
      parts.push(`-H ${shellQuote(`${h.key}: ${h.value}`)}`);
    });
    const body = getRequestBody();
    if (body) {
      parts.push(`-d ${shellQuote(body)}`);
    }
    parts.push(shellQuote(url));
    return parts.join(' \\\n  ');
  }

  function shellQuote(s) {
    return `'${String(s).replace(/'/g, `'\\''`)}'`;
  }

  async function copyCurl() {
    const curl = buildCurl();
    if (!curl) {
      showToast('Enter a URL first', 'error');
      return;
    }
    try {
      await navigator.clipboard.writeText(curl);
      showToast('cURL copied', 'success');
    } catch {
      showToast('Copy failed', 'error');
    }
  }

  /* ── Send ── */
  async function sendRequest() {
    syncUrlParams();
    const method = methodSelect.value;
    const url = urlInput.value.trim();
    if (!url) {
      showToast('Enter a URL', 'error');
      urlInput.focus();
      return;
    }

    sendBtn.disabled = true;
    sendBtn.classList.add('at-send-loading');
    const label = sendBtn.querySelector('.at-send-label');
    if (label) label.textContent = 'Sending…';

    try {
      const res = await fetch('/api/api-tester/proxy', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          method,
          url,
          headers: buildHeaders(),
          body: getRequestBody(),
        }),
      });

      const data = res.ok ? await res.json() : null;
      if (!res.ok) {
        const errText = data ? JSON.stringify(data, null, 2) : await res.text();
        showResponse(0, errText || `Proxy error ${res.status}`, {}, 0, 0, res.statusText);
        pushHistory({
          path: safePath(url), ok: false, statusLabel: `${res.status} ${res.statusText}`,
        });
        showToast('Request failed', 'error');
        return;
      }

      showResponse(data.status, data.body, data.headers, data.durationMs, data.sizeBytes, data.error);
      const ok = !data.error && data.status >= 200 && data.status < 400;
      pushHistory({
        path: safePath(url), ok, statusLabel: data.error ? 'Failed' : `${data.status} ${statusText(data.status) || 'OK'}`,
      });
      showToast(ok ? `${data.status} in ${data.durationMs} ms` : 'Request completed with error', ok ? 'success' : 'error');
    } catch (e) {
      showResponse(0, e.message || 'Request failed', {}, 0, 0, e.message);
      showToast('Network error', 'error');
    } finally {
      sendBtn.disabled = false;
      sendBtn.classList.remove('at-send-loading');
      if (label) label.textContent = 'Send';
    }
  }

  /* ── Splitter ── */
  function initSplitter() {
    const saved = loadJson(STORAGE_SPLIT, { ratio: 0.48 });
    let ratio = saved.ratio ?? 0.48;

    function applyRatio() {
      ratio = Math.min(0.72, Math.max(0.28, ratio));
      requestPane.style.flex = `${ratio} 1 0%`;
      responsePane.style.flex = `${1 - ratio} 1 0%`;
    }
    applyRatio();

    let dragging = false;
    splitter.addEventListener('mousedown', e => {
      dragging = true;
      document.body.classList.add('at-dragging');
      e.preventDefault();
    });
    document.addEventListener('mousemove', e => {
      if (!dragging) return;
      const section = requestPane.parentElement;
      const rect = section.getBoundingClientRect();
      ratio = (e.clientY - rect.top) / rect.height;
      applyRatio();
    });
    document.addEventListener('mouseup', () => {
      if (!dragging) return;
      dragging = false;
      document.body.classList.remove('at-dragging');
      saveJson(STORAGE_SPLIT, { ratio });
    });
  }

  /* ── Panel collapse ── */
  function initPanels() {
    const state = loadJson(STORAGE_PANELS, { collections: false, history: false });
    collectionsPanel?.classList.toggle('at-collapsed', state.collections);
    historyPanel?.classList.toggle('at-collapsed', state.history);

    $('atToggleCollections')?.addEventListener('click', () => {
      const collapsed = collectionsPanel.classList.toggle('at-collapsed');
      state.collections = collapsed;
      saveJson(STORAGE_PANELS, state);
    });
    $('atToggleHistory')?.addEventListener('click', () => {
      const collapsed = historyPanel.classList.toggle('at-collapsed');
      state.history = collapsed;
      saveJson(STORAGE_PANELS, state);
    });
  }

  /* ── Init ── */
  builderTabs.forEach(tab => {
    tab.addEventListener('click', () => switchTabs(
      [...builderTabs], [...builderPanels], tab.dataset.builderTab, 'builderTab', 'builderPanel'
    ));
  });
  responseTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      activeResponseTab = tab.dataset.responseTab;
      responseTabs.forEach(t => t.classList.toggle('at-tab-active', t === tab));
      applyResponseView();
    });
  });

  methodSelect?.addEventListener('change', () => {
    syncMethodWrap(methodSelect.value);
    scheduleDraftSave();
  });

  [urlInput, bodyEditor, authBearer, authBasicUser, authBasicPass].forEach(elm => {
    elm?.addEventListener('input', scheduleDraftSave);
  });
  urlInput?.addEventListener('change', () => { parseUrlToParams(); scheduleDraftSave(); });
  authType?.addEventListener('change', () => { updateAuthVisibility(); scheduleDraftSave(); });
  bodyType?.addEventListener('change', () => { updateBodyEditorState(); scheduleDraftSave(); });

  $('atAddParamBtn')?.addEventListener('click', () => {
    paramsBody.appendChild(createKvRow());
    updateBadges();
  });
  $('atAddHeaderBtn')?.addEventListener('click', () => {
    headersBody.appendChild(createKvRow());
    updateBadges();
  });
  $('atPresetHeadersBtn')?.addEventListener('click', () => {
    fillKvTable(headersBody, [
      { key: 'Accept', value: 'application/json', enabled: true },
      { key: 'Content-Type', value: 'application/json', enabled: true },
    ]);
    updateBadges();
    showToast('JSON headers applied', 'info');
  });
  $('atFormatBodyBtn')?.addEventListener('click', formatBodyJson);
  $('atPasteBodyBtn')?.addEventListener('click', async () => {
    try {
      bodyEditor.value = await navigator.clipboard.readText();
      scheduleDraftSave();
      showToast('Pasted', 'success');
    } catch {
      showToast('Paste failed', 'error');
    }
  });
  $('atClearBodyBtn')?.addEventListener('click', () => {
    bodyEditor.value = '';
    scheduleDraftSave();
  });

  sendBtn?.addEventListener('click', sendRequest);
  $('atNewBtn')?.addEventListener('click', () => newRequest(true));
  $('atSaveBtn')?.addEventListener('click', openSaveModal);
  $('atCurlBtn')?.addEventListener('click', copyCurl);
  $('atSaveConfirmBtn')?.addEventListener('click', confirmSave);
  $('atAddCollectionBtn')?.addEventListener('click', addCollection);
  saveModal?.querySelectorAll('[data-close-modal]').forEach(btn => btn.addEventListener('click', closeSaveModal));

  $('atSaveNewCollection')?.addEventListener('change', e => {
    $('atSaveCollectionName').classList.toggle('hidden', !e.target.checked);
    $('atSaveCollection').disabled = e.target.checked;
  });

  collectionSearch?.addEventListener('input', () => renderCollections(collectionSearch.value));
  clearHistoryBtn?.addEventListener('click', () => {
    saveJson(STORAGE_HISTORY, []);
    renderHistory();
    showToast('History cleared', 'info');
  });

  copyResponseBtn?.addEventListener('click', async () => {
    const text = activeResponseTab === 'raw' ? lastResponseRaw : lastResponsePretty;
    try {
      await navigator.clipboard.writeText(text);
      showToast('Response copied', 'success');
    } catch {
      showToast('Copy failed', 'error');
    }
  });

  $('atDownloadResponseBtn')?.addEventListener('click', () => {
    const text = activeResponseTab === 'raw' ? lastResponseRaw : lastResponsePretty;
    if (!text) {
      showToast('Nothing to download', 'info');
      return;
    }
    const blob = new Blob([text], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `response-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(a.href);
    showToast('Download started', 'success');
  });

  document.addEventListener('keydown', e => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      sendRequest();
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      openSaveModal();
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      urlInput?.focus();
      urlInput?.select();
    }
    if (e.key === 'Escape' && !saveModal.classList.contains('hidden')) {
      closeSaveModal();
    }
  });

  const draft = loadJson(STORAGE_DRAFT, null);
  if (draft?.url) {
    applyRequest(draft);
  } else {
    newRequest();
  }

  showResponse(0, 'Send a request to see the response here.', {}, null, null, null);
  lastResponseRaw = 'Send a request to see the response here.';
  lastResponsePretty = lastResponseRaw;
  lastResponseHtml = escapeHtml(lastResponseRaw);
  responseStatus.textContent = '—';
  responseStatus.className = 'at-status-badge at-status-idle';

  initSplitter();
  initPanels();
  renderCollections();
  renderHistory();
})();
