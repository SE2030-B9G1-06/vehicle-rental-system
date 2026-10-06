(function () {
  "use strict";
  const esc = (v) =>
    String(v ?? "").replace(
      /[&<>"']/g,
      (c) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[c],
    );
  const pencil =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="m15 5 4 4M4 20l4-1L20 7a2.8 2.8 0 0 0-4-4L4 15l-1 6 5-2"/></svg>';
  const trash =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7M14 10v7"/></svg>';
  const get = (o, p) => p.split(".").reduce((a, k) => a?.[k], o);
  const set = (o, p, v) => {
    const parts = p.split(".");
    let x = o;
    parts.slice(0, -1).forEach((k) => (x = x[k] ??= {}));
    x[parts.at(-1)] = v;
  };
  const money = (v) =>
    new Intl.NumberFormat("en-LK", {
      style: "currency",
      currency: "LKR",
    }).format(Number(v || 0));
  const terminal = (r) =>
    ["CANCELLED", "COMPLETED", "VOIDED", "INACTIVE"].includes(r.status) ||
    r.active === false ||
    r.isActive === false;
  let cfg,
    records = [],
    visible = [],
    editing = null,
    refs = {},
    currentUser = null,
    segment = "all",
    generation = 0;
  async function request(path, options = {}) {
    const response = await AppAuth.apiFetch(path, options);
    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem("loggedInUser");
        throw new Error("Your session expired. Please sign in again.");
      }
      throw new Error(await AppAuth.responseMessage(response));
    }
    const text = await response.text();
    if (!text) return null;
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }
  function message(text, type = "error", target = "crudNotice") {
    const e = document.getElementById(target);
    e.textContent = text;
    e.className = "notice " + type;
  }
  function cell(r, c) {
    let v = get(r, c.path);
    if (c.join) v = c.join.map((k) => get(r, k) || "").join(" ");
    if (c.type === "money") return esc(money(v));
    if (c.type === "date")
      return esc(v ? String(v).replace("T", " ").slice(0, 16) : "—");
    if (c.type === "role")
      return esc(
        String(v || "")
          .replace("ROLE_", "")
          .replaceAll("_", " "),
      );
    if (c.type === "active") v = v === false ? "INACTIVE" : "ACTIVE";
    if (c.type === "status" || c.type === "active") {
      const good = ["AVAILABLE", "ACTIVE", "CONFIRMED", "COMPLETED"].includes(
          v,
        ),
        bad = ["CANCELLED", "INACTIVE", "VOIDED"].includes(v);
      return `<span class="status ${good ? "good" : bad ? "bad" : "warn"}">${esc(String(v || "—").replaceAll("_", " "))}</span>`;
    }
    return esc(v ?? "—");
  }
  function render() {
    const query = document.getElementById("crudSearch").value.toLowerCase();
    const show = document.getElementById("showArchived").checked;
    visible = records.filter(
      (r) =>
        (show || !terminal(r)) &&
        (!query || JSON.stringify(r).toLowerCase().includes(query)) &&
        (segment === "all" ||
          (segment === "customers"
            ? r.role === "ROLE_CUSTOMER"
            : r.role !== "ROLE_CUSTOMER")),
    );
    document.getElementById("totalStat").textContent = records.length;
    document.getElementById("activeStat").textContent = records.filter(
      (r) => !terminal(r),
    ).length;
    document.getElementById("archivedStat").textContent =
      records.filter(terminal).length;
    document.getElementById("recordCount").textContent =
      `Showing ${visible.length} of ${records.length} records`;
    document.getElementById("crudRows").innerHTML = visible.length
      ? visible
          .map(
            (r) =>
              `<tr>${cfg.columns.map((c) => `<td>${cell(r, c)}</td>`).join("")}<td><div class="row-actions">${!cfg.canEdit || cfg.canEdit(r) ? `<button class="control small" data-edit="${r.id}" aria-label="Edit record ${r.id}">${pencil} Edit</button>` : ""}${(cfg.actions?.(r) || []).map((a) => `<button class="control small" data-state="${esc(a.status)}" data-id="${r.id}">${esc(a.label)}</button>`).join("")}${!cfg.canDelete || cfg.canDelete(r) ? `<button class="control small danger" data-delete="${r.id}">${trash} ${esc(typeof cfg.deleteLabel === "function" ? cfg.deleteLabel(r) : cfg.deleteLabel || "Delete")}</button>` : ""}</div></td></tr>`,
          )
          .join("")
      : `<tr><td class="empty" colspan="${cfg.columns.length + 1}">No matching records. Try changing your search or showing all statuses.</td></tr>`;
  }
  async function refresh() {
    const load = ++generation;
    document.getElementById("refreshRecords").disabled = true;
    try {
      const result = await request(cfg.endpoint);
      if (load !== generation) return;
      records = result;
      render();
    } catch (e) {
      message(e.message);
      document.getElementById("crudRows").innerHTML =
        `<tr><td class="empty" colspan="${cfg.columns.length + 1}">Unable to load records. Use Refresh to try again.</td></tr>`;
    } finally {
      document.getElementById("refreshRecords").disabled = false;
    }
  }
  async function openForm(r = null) {
    editing = r;
    message("", "", "formNotice");
    document.getElementById("formTitle").textContent =
      (r ? "Edit " : "Create ") +
      (r ? cfg.editSingular || cfg.singular : cfg.singular);
    document.getElementById("saveRecord").textContent = r
      ? "Save changes"
      : "Create " + cfg.singular;
    document.getElementById("fields").innerHTML = "<p>Loading form…</p>";
    document.getElementById("saveRecord").disabled = true;
    document.getElementById("editDialog").showModal();
    try {
      const sources = [
        ...new Set(cfg.fields.filter((f) => f.source).map((f) => f.source)),
      ];
      const data = await Promise.all(
        sources.map(async (source) => [source, await request(source)]),
      );
      refs = Object.fromEntries(data);
      document.getElementById("fields").innerHTML = cfg.fields
        .filter((f) => !f.onlyCreate || !r)
        .map((f) => {
          let value = r
            ? get(r, f.key)
            : typeof f.default === "function"
              ? f.default()
              : f.default;
          if (value == null && f.default != null)
            value = typeof f.default === "function" ? f.default() : f.default;
          if (f.type === "password") value = "";
          if (f.type === "datetime-local" && value)
            value = String(value).slice(0, 16);
          const id = "field-" + f.key.replaceAll(".", "-");
          const required = f.required && !(f.type === "password" && r);
          const disabled = !!(r && f.lockOnEdit);
          let attrs = `id="${id}" name="${f.key}" ${required ? "required" : ""} ${disabled ? "disabled" : ""} ${f.min != null ? `min="${f.min}"` : ""} ${f.max != null ? `max="${f.max}"` : ""} ${f.step ? `step="${f.step}"` : ""} ${f.maxlength ? `maxlength="${f.maxlength}"` : ""}`;
          let input;
          if (f.type === "select") {
            let options = f.options || [];
            if (f.source)
              options = refs[f.source]
                .filter((x) => !f.filter || f.filter(x, r))
                .map((x) => [x.id, f.optionLabel(x)]);
            if (typeof options === "function") options = options(r);
            if (
              r &&
              value != null &&
              !options.some(([v]) => String(v) === String(value))
            )
              options = [[value, `${value} (current)`], ...options];
            input = `<select ${attrs}><option value="">Choose ${esc(f.label.toLowerCase())}</option>${options.map(([v, l]) => `<option value="${esc(v)}" ${String(v) === String(value) ? "selected" : ""}>${esc(l)}</option>`).join("")}</select>`;
          } else if (f.type === "textarea")
            input = `<textarea ${attrs} rows="3">${esc(value || "")}</textarea>`;
          else
            input = `<input type="${f.type || "text"}" ${attrs} value="${esc(value ?? "")}" ${f.type === "password" ? 'autocomplete="new-password"' : ""}>`;
          return `<div class="field ${f.wide ? "wide" : ""}"><label for="${id}">${esc(f.label)}${required ? " *" : ""}</label>${input}${f.hint ? `<small>${esc(f.hint)}</small>` : ""}</div>`;
        })
        .join("");
      document.getElementById("saveRecord").disabled = false;
      document
        .querySelector(
          "#fields input:not(:disabled),#fields select:not(:disabled)",
        )
        ?.focus();
    } catch (e) {
      message(e.message, "error", "formNotice");
    }
  }
  async function save(event) {
    event.preventDefault();
    const button = document.getElementById("saveRecord");
    button.disabled = true;
    document
      .querySelectorAll("[data-close]")
      .forEach((b) => (b.disabled = true));
    message("", "", "formNotice");
    try {
      const payload = {};
      for (const f of cfg.fields) {
        const el = document.getElementById(
          "field-" + f.key.replaceAll(".", "-"),
        );
        if (!el) continue;
        let v = el.value;
        if (f.type !== "password") v = v.trim();
        if (f.numeric || f.type === "number") v = v === "" ? null : Number(v);
        if (f.boolean) v = v === "" ? true : v === "true";
        if (v === "" && f.nullable) v = null;
        set(payload, f.key, v);
      }
      const data = cfg.transform ? cfg.transform(payload, editing) : payload;
      await request(
        editing
          ? `${cfg.endpoint}/${editing.id}`
          : cfg.createEndpoint || cfg.endpoint,
        { method: editing ? "PUT" : "POST", body: JSON.stringify(data) },
      );
      document.getElementById("editDialog").close();
      message(
        editing
          ? "Changes saved successfully."
          : "Record created successfully.",
        "success",
      );
      await refresh();
    } catch (e) {
      message(e.message, "error", "formNotice");
    } finally {
      button.disabled = false;
      document
        .querySelectorAll("[data-close]")
        .forEach((b) => (b.disabled = false));
    }
  }
  function confirmAction(text) {
    return new Promise((resolve) => {
      const dialog = document.getElementById("confirmDialog");
      document.getElementById("confirmText").textContent = text;
      dialog.returnValue = "cancel";
      dialog.addEventListener(
        "close",
        () => resolve(dialog.returnValue === "confirm"),
        { once: true },
      );
      dialog.showModal();
    });
  }
  async function remove(r) {
    if (
      !(await confirmAction(
        typeof cfg.confirm === "function"
          ? cfg.confirm(r)
          : cfg.confirm || "Delete this record?",
      ))
    )
      return;
    try {
      const reply = await request(`${cfg.endpoint}/${r.id}`, {
        method: "DELETE",
      });
      message(
        typeof reply === "string"
          ? reply
          : reply?.message || "Record updated successfully.",
        "success",
      );
      await refresh();
    } catch (e) {
      message(e.message);
    }
  }
  async function state(id, status) {
    if (
      !(await confirmAction(
        `Change booking #${id} to ${status.replaceAll("_", " ").toLowerCase()}?`,
      ))
    )
      return;
    try {
      await request(
        `${cfg.endpoint}/${id}/status?status=${encodeURIComponent(status)}`,
        { method: "PUT", body: "{}" },
      );
      message("Booking status updated.", "success");
      await refresh();
    } catch (e) {
      message(e.message);
    }
  }
  function exportCsv() {
    const rows = [
      cfg.columns.map((c) => c.label),
      ...visible.map((r) =>
        cfg.columns.map((c) =>
          c.join
            ? c.join.map((p) => get(r, p) || "").join(" ")
            : (get(r, c.path) ?? ""),
        ),
      ),
    ];
    const csv = rows
      .map((row) =>
        row
          .map(
            (v) =>
              '"' +
              String(/^[=+@-]/.test(String(v)) ? "'" + v : v).replaceAll(
                '"',
                '""',
              ) +
              '"',
          )
          .join(","),
      )
      .join("\r\n");
    const a = document.createElement("a"),
      url = URL.createObjectURL(
        new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8" }),
      );
    a.href = url;
    a.download = cfg.key + "-report.csv";
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  window.CrudUI = {
    start: async function (config) {
      cfg = config;
      currentUser = await AppAuth.requireRole(cfg.roles);
      if (!currentUser) return;
      await loadComponent("navbar-placeholder", "../components/navbar.html");
      document.getElementById("crudSearch").addEventListener("input", render);
      document
        .getElementById("showArchived")
        .addEventListener("change", render);
      document
        .getElementById("refreshRecords")
        .addEventListener("click", refresh);
      document
        .getElementById("exportRecords")
        .addEventListener("click", exportCsv);
      document
        .getElementById("createRecord")
        .addEventListener("click", () =>
          cfg.createHref ? location.assign(cfg.createHref) : openForm(),
        );
      document.getElementById("recordForm").addEventListener("submit", save);
      document.getElementById("editDialog").addEventListener("cancel", (e) => {
        if (document.getElementById("saveRecord").disabled) e.preventDefault();
      });
      document
        .querySelectorAll("[data-close]")
        .forEach((b) =>
          b.addEventListener("click", () =>
            document.getElementById("editDialog").close(),
          ),
        );
      document.getElementById("crudRows").addEventListener("click", (e) => {
        const b = e.target.closest("button");
        if (!b) return;
        if (b.dataset.edit)
          openForm(records.find((x) => String(x.id) === b.dataset.edit));
        if (b.dataset.delete)
          remove(records.find((x) => String(x.id) === b.dataset.delete));
        if (b.dataset.state) state(b.dataset.id, b.dataset.state);
      });
      if (cfg.users) {
        document.getElementById("userTabs").hidden = false;
        document.querySelectorAll("[data-segment]").forEach((b) =>
          b.addEventListener("click", () => {
            segment = b.dataset.segment;
            document
              .querySelectorAll("[data-segment]")
              .forEach((x) => x.classList.toggle("selected", x === b));
            render();
          }),
        );
      }
      await refresh();
    },
    esc,
    money,
  };
})();
