(() => {
  const API_ROOT = "https://api.frankfurter.dev/v2";
  const HISTORY_KEY = "currency-converter.pages.history.v1";
  const MAX_HISTORY = 100;
  const REQUEST_TIMEOUT_MS = 8000;
  const SUPPORTED_CURRENCIES = new Set(["BRL", "USD", "EUR", "GBP", "JPY", "CAD", "ARS", "CNY"]);
  let latestResult = null;

  function isValidDate(value) {
    if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
    const parsed = new Date(`${value}T00:00:00Z`);
    return Number.isFinite(parsed.getTime()) && parsed.toISOString().slice(0, 10) === value;
  }

  function normalizeEntry(entry) {
    if (!entry || typeof entry !== "object" || Array.isArray(entry)) return null;
    const source = String(entry.source || "");
    const target = String(entry.target || "");
    const original = Number(entry.original);
    const converted = Number(entry.converted);
    const rate = Number(entry.rate);
    const date = String(entry.date || "");
    const created = String(entry.created || "");

    if (
      !SUPPORTED_CURRENCIES.has(source) ||
      !SUPPORTED_CURRENCIES.has(target) ||
      source === target ||
      !Number.isFinite(original) ||
      original <= 0 ||
      !Number.isFinite(converted) ||
      converted < 0 ||
      !Number.isFinite(rate) ||
      rate <= 0 ||
      !isValidDate(date) ||
      !Number.isFinite(Date.parse(created))
    ) {
      return null;
    }

    return { source, target, original, converted, rate, date, created };
  }

  function loadHistory() {
    try {
      const stored = JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
      if (!Array.isArray(stored)) return [];
      return stored
        .map(normalizeEntry)
        .filter(Boolean)
        .sort((left, right) => Date.parse(right.created) - Date.parse(left.created))
        .slice(0, MAX_HISTORY);
    } catch {
      return [];
    }
  }

  function saveHistory(entries) {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(entries.slice(0, MAX_HISTORY)));
      return true;
    } catch {
      return false;
    }
  }

  async function requestJson(url) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
    try {
      const response = await fetch(url, { signal: controller.signal });
      if (!response.ok) throw new Error("Rate service returned an error");
      return await response.json();
    } finally {
      clearTimeout(timeout);
    }
  }

  async function getRateHistory(source, target, days) {
    if (
      !SUPPORTED_CURRENCIES.has(source) ||
      !SUPPORTED_CURRENCIES.has(target) ||
      source === target ||
      ![7, 30, 90].includes(Number(days))
    ) {
      throw new Error("Invalid rate history request");
    }

    const start = new Date();
    start.setUTCHours(0, 0, 0, 0);
    start.setUTCDate(start.getUTCDate() - Number(days));
    const query = new URLSearchParams({
      base: source,
      quotes: target,
      from: start.toISOString().slice(0, 10),
    });
    const response = await requestJson(`${API_ROOT}/rates?${query}`);
    if (!Array.isArray(response)) throw new Error("Rate service returned invalid history");

    return response
      .filter(
        (point) =>
          point &&
          isValidDate(String(point.date)) &&
          Number.isFinite(Number(point.rate)) &&
          Number(point.rate) > 0,
      )
      .map((point) => ({ date: String(point.date), rate: Number(point.rate) }));
  }

  function convertedCents(amountText, rate) {
    const [wholePart, fractionPart = ""] = amountText.split(".");
    const amountUnits = BigInt(`${wholePart}${fractionPart}`);
    const rateText = rate.toFixed(16);
    const [rateWhole, rateFraction = ""] = rateText.split(".");
    const rateUnits = BigInt(`${rateWhole}${rateFraction}`);
    const scale = fractionPart.length + rateFraction.length;
    const divisor = 10n ** BigInt(scale - 2);
    const cents = (amountUnits * rateUnits + divisor / 2n) / divisor;
    return Number(cents) / 100;
  }

  function setBusy(isBusy) {
    const form = document.getElementById("converterForm");
    const button = document.getElementById("convertButton");
    const status = document.getElementById("conversionStatus");
    if (!form || !button || !status) return;

    form.setAttribute("aria-busy", String(isBusy));
    button.setAttribute("aria-busy", String(isBusy));
    button.disabled = isBusy;
    button.querySelector("[data-i18n]").textContent = window.CurrencyApp.t(
      isBusy ? "converting" : "convertButton",
    );
    status.textContent = isBusy ? window.CurrencyApp.t("converting") : "";
  }

  function displayResult(entry) {
    const panel = document.getElementById("resultPanel");
    if (!panel) return;
    panel.dataset.source = entry.source;
    panel.dataset.target = entry.target;
    panel.dataset.original = String(entry.original);
    panel.dataset.converted = String(entry.converted);
    panel.dataset.rate = String(entry.rate);
    panel.dataset.date = entry.date;
    panel.hidden = false;
    window.CurrencyApp.formatData();
  }

  function createRecentItem(entry) {
    const item = document.createElement("div");
    item.className = "recent-item";
    item.dataset.source = entry.source;
    item.dataset.target = entry.target;
    item.dataset.value = String(entry.converted);
    item.dataset.created = entry.created;

    const icon = document.createElement("div");
    icon.className = "recent-icon";
    icon.setAttribute("aria-hidden", "true");
    icon.textContent = entry.target;

    const description = document.createElement("div");
    description.className = "recent-description";
    const pair = document.createElement("strong");
    pair.className = "recent-pair";
    const date = document.createElement("span");
    date.className = "recent-date";
    description.append(pair, date);

    const value = document.createElement("strong");
    value.className = "recent-value";
    item.append(icon, description, value);
    return item;
  }

  function createHistoryRow(entry) {
    const row = document.createElement("tr");
    row.dataset.source = entry.source;
    row.dataset.target = entry.target;
    row.dataset.original = String(entry.original);
    row.dataset.value = String(entry.converted);
    row.dataset.rate = String(entry.rate);
    row.dataset.date = entry.date;
    row.dataset.created = entry.created;

    for (const [className, label] of [
      ["history-created", "Data"],
      ["history-original", "Valor original"],
      ["history-pair", "Moedas"],
      ["history-rate", "Taxa de câmbio"],
      ["history-result", "Resultado"],
    ]) {
      const cell = document.createElement("td");
      cell.className = className;
      cell.dataset.label = label;
      row.append(cell);
    }
    return row;
  }

  function refreshView() {
    const entries = loadHistory();
    const recentList = document.getElementById("recentList");
    const recentEmpty = document.getElementById("recentEmpty");
    if (recentList && recentEmpty) {
      recentList.replaceChildren(...entries.slice(0, 3).map(createRecentItem));
      recentList.hidden = entries.length === 0;
      recentEmpty.hidden = entries.length !== 0;
    }

    const rows = document.getElementById("historyRows");
    const historyCard = document.getElementById("historyCard");
    const historyEmpty = document.getElementById("historyEmpty");
    if (rows && historyCard && historyEmpty) {
      rows.replaceChildren(...entries.map(createHistoryRow));
      historyCard.hidden = entries.length === 0;
      historyEmpty.hidden = entries.length !== 0;
      const clearButton = document.querySelector('#clearHistoryForm button[type="submit"]');
      if (clearButton) clearButton.disabled = entries.length === 0;
    }

    if (latestResult) displayResult(latestResult);
    window.CurrencyApp?.formatData();
  }

  async function convert() {
    const amountInput = document.getElementById("amount");
    const sourceInput = document.getElementById("sourceCurrency");
    const targetInput = document.getElementById("targetCurrency");
    const amountText = amountInput.value.trim();
    const source = sourceInput.value;
    const target = targetInput.value;
    if (
      !SUPPORTED_CURRENCIES.has(source) ||
      !SUPPORTED_CURRENCIES.has(target) ||
      source === target ||
      !window.CurrencyInput.isValidConversionAmount(amountText)
    ) {
      return;
    }

    setBusy(true);
    try {
      const pair = [source, target]
        .map((currency) => encodeURIComponent(currency.toLowerCase()))
        .join("/");
      const rateResponse = await requestJson(`${API_ROOT}/rate/${pair}`);
      const rate = Number(rateResponse?.rate);
      const date = String(rateResponse?.date || "");
      if (!Number.isFinite(rate) || rate <= 0 || !isValidDate(date)) {
        throw new Error("Rate service returned an invalid quote");
      }

      const entry = {
        source,
        target,
        original: Number(amountText),
        converted: convertedCents(amountText, rate),
        rate,
        date,
        created: new Date().toISOString(),
      };
      latestResult = entry;
      const saved = saveHistory([entry, ...loadHistory()]);
      refreshView();
      window.CurrencyApp.toast(
        window.CurrencyApp.t(saved ? "conversionDone" : "pagesStorageUnavailable"),
      );
    } catch {
      window.CurrencyApp.toast(window.CurrencyApp.t("conversionUnavailable"));
    } finally {
      setBusy(false);
    }
  }

  function clearHistory() {
    try {
      localStorage.removeItem(HISTORY_KEY);
    } catch {
      // Keep the view usable when browser storage is disabled.
    }
    latestResult = null;
    const status = document.getElementById("clearHistoryStatus");
    if (status) status.textContent = window.CurrencyApp.t("clearedMessage");
    refreshView();
    window.CurrencyApp.toast(window.CurrencyApp.t("clearedMessage"));
  }

  window.CurrencyPages = Object.freeze({ getRateHistory, convert, clearHistory, refreshView });
})();
