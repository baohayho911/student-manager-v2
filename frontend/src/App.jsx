import React, { useEffect, useState } from "react";
import { api, token, login, logout, parseCsv, exportCsv } from "./api";
import {
  catalogs,
  sectionFields,
  passwordFields,
  validatePassword,
  labels,
} from "./config";
import {
  Icon,
  Badge,
  PageTitle,
  DataTable,
  Editor,
  Confirm,
  Modal,
} from "./components";

function useData(path, revision = 0) {
  const [data, setData] = useState(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    api(path)
      .then((d) => {
        if (active) setData(d);
      })
      .catch((e) => {
        if (active) setError(e.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [path, revision]);
  return { data, error, loading };
}
function State({ error, loading }) {
  return error ? (
    <div className="alert error" role="alert">
      {error}
    </div>
  ) : loading ? (
    <div className="loading">Đang tải dữ liệu…</div>
  ) : null;
}
const small = (label, onClick, danger = false, disabled = false) => (
  <button
    className={"small " + (danger ? "outline-danger" : "secondary")}
    disabled={disabled}
    onClick={onClick}
  >
    {label}
  </button>
);
const scoreColumns = [
  { key: "tx1", label: "TX1" },
  { key: "tx2", label: "TX2" },
  { key: "final_score", label: "KTHP" },
  { key: "total", label: "Tổng kết" },
  { key: "letter", label: "Điểm chữ" },
];
const sectionColumns = [
  { key: "code", label: "Mã lớp" },
  { key: "subject_name", label: "Môn học" },
  { key: "credits", label: "Tín chỉ" },
  { key: "semester_name", label: "Học kỳ" },
  { key: "teacher_name", label: "Giảng viên" },
  { key: "schedule_text", label: "Lịch học" },
  { key: "room", label: "Phòng" },
  {
    key: "enrolled",
    label: "Sĩ số",
    value: (r) => r.enrolled + "/" + r.capacity,
  },
  {
    key: "status",
    label: "Trạng thái",
    value: (r) => labels[r.status],
    render: (r) => <Badge value={r.status} />,
  },
];

function Login({ onLogin }) {
  const [username, setUsername] = useState(""),
    [password, setPassword] = useState(""),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      onLogin(await login(username.trim().toLowerCase(), password));
    } catch (e) {
      setError(e.message || "Không thể đăng nhập. Kiểm tra backend.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="login-page">
      <div className="login-story">
        <div className="brand">
          <img className="brand-mark" src="/haui-logo.png" alt="Logo HaUI" />
          <strong>UniDesk</strong>
        </div>
        <div className="story-copy">
          <span className="eyebrow">KẾT NỐI HỌC TẬP · QUẢN LÝ HIỆU QUẢ</span>
          <h1>
            Một không gian.
            <br />
            Mọi hành trình
            <br />
            <em>học tập.</em>
          </h1>
          <p>
            Cổng thông tin dành cho sinh viên,
            <br />
            giảng viên và cán bộ quản lý.
          </p>
        </div>
        <svg className="campus" viewBox="0 0 640 290" aria-hidden="true">
          <path d="M0 265h640v25H0z" fill="#c4b393" />
          <path d="M92 83h330v182H92z" fill="#e4d8bb" />
          <path d="M74 78h366v16H74zM110 58h300v20H110z" fill="#baa47e" />
          <path d="M251 22h14v36h-14z" fill="#786849" />
          <path d="M265 24h42v24h-42z" fill="#d9a333" />
          {[0, 1, 2, 3, 4, 5].map((x) =>
            [0, 1, 2].map((y) => (
              <rect
                key={x + "-" + y}
                x={115 + x * 49}
                y={112 + y * 44}
                width="27"
                height="28"
                fill="#7c8b7b"
              />
            )),
          )}
          <path d="M220 220h70v45h-70z" fill="#665d4b" />
          <path d="M444 143h124v122H444z" fill="#d2c2a4" />
          {[0, 1, 2].map((x) => (
            <path
              key={x}
              d={"M" + (460 + x * 32) + " 165h18v65h-18z"}
              fill="#829380"
            />
          ))}
          <path d="M36 194v71m562-96v96" stroke="#746d55" strokeWidth="9" />
          <circle cx="36" cy="174" r="34" fill="#91a087" />
          <circle cx="598" cy="151" r="45" fill="#829778" />
        </svg>
        <div className="story-footer">
          HỆ THỐNG QUẢN LÝ SINH VIÊN <span>PHIÊN BẢN 2.0</span>
        </div>
      </div>
      <div className="login-panel">
        <form className="login-form" onSubmit={submit}>
          <img className="brand-mark" src="/haui-logo.png" alt="Logo HaUI" />
          <h2>Chào mừng trở lại</h2>
          <p>Đăng nhập để truy cập cổng thông tin của bạn.</p>
          {error && (
            <div className="alert error" role="alert">
              {error}
            </div>
          )}
          <label>
            Tên đăng nhập
            <input
              autoComplete="username"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Nhập mã sinh viên hoặc tài khoản"
            />
          </label>
          <label>
            Mật khẩu
            <input
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Nhập mật khẩu"
            />
          </label>
          <button className="login-submit" disabled={busy}>
            {busy ? "Đang đăng nhập…" : "Đăng nhập →"}
          </button>
          <p className="login-help">
            Quên mật khẩu? Liên hệ quản trị viên để được cấp lại.
          </p>
        </form>
        <div className="login-bottom">UniDesk · Quản lý sinh viên</div>
      </div>
    </div>
  );
}

function Catalog({ kind, lookups, refresh, notice }) {
  const config = catalogs[kind];
  const [editor, setEditor] = useState(null),
    [confirm, setConfirm] = useState(null),
    [importing, setImporting] = useState(false),
    [filter, setFilter] = useState("");
  const profile = kind === "students" || kind === "teachers";
  const cols = [
    ...(kind === "classes" ? [{ key: "id", label: "ID" }] : []),
    ...config.columns.map((k) => {
      const f = config.fields.find((f) => f.name === k);
      return {
        key: k,
        label: f.label,
        value: (r) =>
          f.source
            ? (lookups[f.source] || []).find((x) => x.id === r[k])?.name || "—"
            : labels[r[k]] || r[k],
        render: k === "status" ? (r) => <Badge value={r.status} /> : undefined,
      };
    }),
  ];
  const rows = (lookups[kind] || []).filter(
    (r) => !filter || String(r.class_id || r.department_id) === filter,
  );
  const save = async (body) => {
    let b = { ...body };
    if (profile && !editor.id) {
      validatePassword(b, "initialPassword");
    }
    await api(
      "/admin/catalog/" + kind + (editor.id ? "/" + editor.id : ""),
      editor.id ? "PUT" : "POST",
      b,
    );
    refresh();
    notice(
      editor.id
        ? "Đã cập nhật dữ liệu."
        : "Đã thêm " +
            config.single +
            (profile
              ? " và tài khoản. Người dùng cần đổi mật khẩu khi đăng nhập."
              : "."),
    );
  };
  let fields = config.fields;
  if (profile && editor && !editor.id)
    fields = [
      ...fields,
      ...passwordFields.map((f) => ({
        ...f,
        name: f.name === "newPassword" ? "initialPassword" : f.name,
        label: f.name === "newPassword" ? "Mật khẩu ban đầu" : f.label,
      })),
    ];
  return (
    <>
      <PageTitle title={config.title} description={config.description}>
        {kind === "students" && (
          <button className="secondary" onClick={() => setImporting(true)}>
            ↑ Nhập CSV
          </button>
        )}
        <button onClick={() => setEditor({})}>+ Thêm {config.single}</button>
      </PageTitle>
      <DataTable
        columns={cols}
        rows={rows}
        exportName={kind}
        filters={
          profile && (
            <select
              aria-label="Lọc hồ sơ"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            >
              <option value="">
                {kind === "students" ? "Tất cả lớp" : "Tất cả khoa"}
              </option>
              {(
                lookups[kind === "students" ? "classes" : "departments"] || []
              ).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          )
        }
        actions={(r) => (
          <>
            {small("Sửa", () => setEditor(r))}
            {small("Xóa", () => setConfirm(r), true)}
          </>
        )}
      />
      {editor && (
        <Editor
          title={(editor.id ? "Sửa " : "Thêm ") + config.single}
          fields={fields}
          initial={editor}
          lookups={lookups}
          onClose={() => setEditor(null)}
          onSave={save}
          description={
            profile
              ? editor.id
                ? "Sửa hồ sơ không thay tên đăng nhập và mật khẩu."
                : "Tên đăng nhập lấy từ mã chuyển thành chữ thường. Tài khoản được tạo cùng hồ sơ."
              : undefined
          }
        />
      )}{" "}
      {confirm && (
        <Confirm
          title={"Xóa " + config.single + " " + confirm.code + "?"}
          description={
            profile
              ? "Hồ sơ và tài khoản liên kết sẽ bị xóa vĩnh viễn. Hệ thống sẽ từ chối nếu hồ sơ còn dữ liệu học tập hoặc lớp phụ trách."
              : "Dữ liệu sẽ bị xóa vĩnh viễn. Không thể xóa nếu đang được danh mục khác sử dụng."
          }
          label={profile ? "Xóa hồ sơ và tài khoản" : "Xóa"}
          danger
          onClose={() => setConfirm(null)}
          onConfirm={async () => {
            await api("/admin/catalog/" + kind + "/" + confirm.id, "DELETE");
            refresh();
            notice("Đã xóa dữ liệu.");
          }}
        />
      )}
      {importing && (
        <ImportStudents
          onClose={() => setImporting(false)}
          refresh={refresh}
          notice={notice}
        />
      )}
    </>
  );
}

function ImportStudents({ onClose, refresh, notice }) {
  const [rows, setRows] = useState([]),
    [error, setError] = useState(""),
    [password, setPassword] = useState(""),
    [busy, setBusy] = useState(false);
  async function read(file) {
    try {
      if (!file) return;
      if (file.size > 1024 * 1024) throw new Error("Tệp tối đa 1 MB.");
      const r = parseCsv(await file.text());
      if (r.length > 200) throw new Error("Mỗi lần nhập tối đa 200 sinh viên.");
      setRows(r);
      setError("");
    } catch (e) {
      setError(e.message);
      setRows([]);
    }
  }
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      validatePassword({ newPassword: password, confirmPassword: password });
      await api("/admin/import/students", "POST", {
        rows,
        initialPassword: password,
      });
      refresh();
      notice("Đã nhập " + rows.length + " sinh viên và tạo tài khoản.");
      onClose();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal
      title="Nhập danh sách sinh viên"
      description="CSV UTF-8, dấu phẩy phân cách. Dữ liệu được kiểm tra và nhập trong một giao dịch; nếu một dòng lỗi, toàn bộ lần nhập được hoàn tác."
      onClose={onClose}
      busy={busy}
    >
      <button
        className="secondary"
        onClick={() =>
          exportCsv(
            "mau-sinh-vien",
            [
              ...[
                "code",
                "full_name",
                "class_id",
                "email",
                "phone",
                "birth_date",
                "gender",
                "status",
              ].map((key) => ({ key, label: key })),
            ],
            [
              {
                code: "SV100",
                full_name: "Sinh viên mẫu",
                class_id: 1,
                email: "demo@example.com",
                phone: "0901234567",
                birth_date: "2005-01-01",
                gender: "MALE",
                status: "STUDYING",
              },
            ],
          )
        }
      >
        ↓ Tải CSV mẫu
      </button>
      <p className="muted">
        class_id là ID lớp trong dữ liệu; ngày sinh yyyy-mm-dd. Mẫu dùng lớp ID
        1; hãy thay bằng ID thực tế. Giới tính MALE/FEMALE/OTHER; trạng thái
        STUDYING/PAUSED/LEFT/GRADUATED.
      </p>
      <form onSubmit={submit}>
        <label>
          Chọn tệp CSV
          <input
            type="file"
            accept=".csv"
            disabled={busy}
            onChange={(e) => read(e.target.files[0])}
          />
        </label>
        <p>
          Đã đọc <strong>{rows.length}</strong> dòng.{" "}
          {rows
            .slice(0, 3)
            .map((r) => r.code)
            .join(", ")}
        </p>
        <label>
          Mật khẩu ban đầu cho các tài khoản
          <input
            type="password"
            required
            minLength={12}
            maxLength={72}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        <p className="muted">
          Tất cả tài khoản mới bắt buộc đổi mật khẩu ở lần đăng nhập đầu.
        </p>
        {error && <div className="alert error">{error}</div>}
        <div className="modal-footer">
          <button
            type="button"
            className="secondary"
            disabled={busy}
            onClick={onClose}
          >
            Hủy
          </button>
          <button disabled={busy || !rows.length}>
            {busy ? "Đang nhập…" : "Nhập " + rows.length + " sinh viên"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

function Sections({ user, lookups, revision, refresh, notice, onRoster }) {
  const { data, error, loading } = useData("/sections", revision);
  const [editor, setEditor] = useState(null),
    [confirm, setConfirm] = useState(null),
    [term, setTerm] = useState("");
  const admin = user.role === "ADMIN",
    student = user.role === "STUDENT";
  const terms = [
    ...new Map(
      (data || []).map((s) => [s.semester_id, s.semester_name]),
    ).entries(),
  ];
  const rows = (data || []).filter(
    (r) => !term || String(r.semester_id) === term,
  );
  return (
    <>
      <PageTitle
        title={
          student
            ? "Đăng ký học phần"
            : admin
              ? "Lớp học phần"
              : "Lớp được phân công"
        }
        description={
          student
            ? "Tra cứu lớp đang mở và đăng ký học phần. Lịch hiện là mô tả; chưa kiểm tra trùng lịch."
            : "Quản lý lớp, phân công giảng dạy và theo dõi sĩ số."
        }
      >
        {admin && (
          <button onClick={() => setEditor({})}>+ Mở lớp học phần</button>
        )}
      </PageTitle>
      <State error={error} loading={loading} />
      {data && (
        <DataTable
          rows={rows}
          columns={sectionColumns}
          exportName="lop-hoc-phan"
          filters={
            <select
              aria-label="Học kỳ"
              value={term}
              onChange={(e) => setTerm(e.target.value)}
            >
              <option value="">Tất cả học kỳ</option>
              {terms.map(([id, name]) => (
                <option key={id} value={id}>
                  {name}
                </option>
              ))}
            </select>
          }
          actions={(r) => (
            <>
              {!student && small("Danh sách / Điểm", () => onRoster(r))}
              {admin && small("Sửa", () => setEditor(r))}
              {admin &&
                small(
                  "Xóa",
                  () => setConfirm({ type: "delete", row: r }),
                  true,
                )}
              {student &&
                small(
                  "Đăng ký",
                  () => setConfirm({ type: "enroll", row: r }),
                  false,
                  r.status !== "OPEN" ||
                    r.enrolled >= r.capacity ||
                    r.grade_locked,
                )}
            </>
          )}
        />
      )}{" "}
      {editor && (
        <Editor
          title={editor.id ? "Sửa lớp học phần" : "Mở lớp học phần"}
          fields={sectionFields}
          lookups={lookups}
          initial={editor}
          onClose={() => setEditor(null)}
          onSave={async (b) => {
            if (+b.weight1 + +b.weight2 + +b.weight_final !== 100)
              throw new Error("Tổng tỷ trọng phải bằng 100%.");
            await api(
              "/admin/sections" + (editor.id ? "/" + editor.id : ""),
              editor.id ? "PUT" : "POST",
              b,
            );
            refresh();
            notice("Đã lưu lớp học phần.");
          }}
        />
      )}
      {confirm && (
        <Confirm
          title={
            confirm.type === "delete"
              ? "Xóa lớp " + confirm.row.code + "?"
              : "Đăng ký lớp " + confirm.row.code + "?"
          }
          description={
            confirm.type === "delete"
              ? "Chỉ xóa được lớp chưa có lịch sử đăng ký."
              : "Hệ thống sẽ kiểm tra sĩ số, trạng thái học tập và môn đã đăng ký trong học kỳ."
          }
          label={confirm.type === "delete" ? "Xóa lớp" : "Xác nhận đăng ký"}
          danger={confirm.type === "delete"}
          onClose={() => setConfirm(null)}
          onConfirm={async () => {
            await api(
              confirm.type === "delete"
                ? "/admin/sections/" + confirm.row.id
                : "/sections/" + confirm.row.id + "/enroll",
              confirm.type === "delete" ? "DELETE" : "POST",
              confirm.type === "delete" ? undefined : {},
            );
            refresh();
            notice(
              confirm.type === "delete"
                ? "Đã xóa lớp."
                : "Đăng ký học phần thành công.",
            );
          }}
        />
      )}
    </>
  );
}

function Roster({ user, section, lookups, revision, refresh, notice, onBack }) {
  const { data, error, loading } = useData(
    "/sections/" + section.id + "/roster",
    revision,
  );
  const { data: all } = useData("/sections", revision);
  const current = all?.find((s) => s.id === section.id) || section;
  const [editing, setEditing] = useState(null),
    [confirm, setConfirm] = useState(null),
    [adding, setAdding] = useState(false);
  const locked = current.grade_locked;
  const admin = user.role === "ADMIN";
  return (
    <>
      <button className="back-button" onClick={onBack}>
        ← Quay lại danh sách lớp
      </button>
      <PageTitle
        title={current.code + " · " + current.subject_name}
        description={
          "Tỷ trọng TX1 " +
          current.weight1 +
          "% · TX2 " +
          current.weight2 +
          "% · KTHP " +
          current.weight_final +
          "%. Điểm chỉ công bố sau khi chốt."
        }
      >
        {admin && (
          <button
            className="secondary"
            onClick={() => setAdding(true)}
            disabled={current.status !== "OPEN" || locked}
          >
            + Thêm sinh viên
          </button>
        )}
        {(!locked || admin) && (
          <button
            onClick={() => setConfirm({ type: locked ? "unlock" : "lock" })}
          >
            {locked ? "Mở lại điểm" : "Chốt và công bố điểm"}
          </button>
        )}
      </PageTitle>
      <div className="section-strip">
        <span>
          <Badge value={current.status} />
        </span>
        <span>{current.teacher_name}</span>
        <span>{current.semester_name}</span>
        <strong>{locked ? "● Điểm đã chốt" : "○ Điểm đang nhập"}</strong>
      </div>
      <State error={error} loading={loading} />
      {data && (
        <DataTable
          rows={data}
          columns={[
            { key: "code", label: "Mã sinh viên" },
            { key: "full_name", label: "Họ và tên" },
            ...scoreColumns,
            {
              key: "status",
              label: "Đăng ký",
              render: (r) => <Badge value={r.status} />,
            },
          ]}
          exportName={"bang-diem-" + current.code}
          actions={(r) => (
            <>
              {small(
                r.total == null ? "Nhập điểm" : "Sửa điểm",
                () => setEditing(r),
                false,
                locked || r.status !== "ACTIVE",
              )}
              {admin &&
                small(
                  "Hủy đăng ký",
                  () => setConfirm({ type: "cancel", row: r }),
                  true,
                  r.status !== "ACTIVE" ||
                    r.total != null ||
                    current.status !== "OPEN" ||
                    locked,
                )}
            </>
          )}
        />
      )}{" "}
      {editing && (
        <Editor
          title={"Điểm · " + editing.full_name}
          initial={editing}
          fields={[
            ...["tx1", "tx2", "final_score"].map((name, i) => ({
              name,
              label: ["TX1", "TX2", "KTHP"][i],
              type: "number",
              step: "0.01",
              min: 0,
              max: 10,
              required: true,
            })),
            {
              name: "reason",
              label: "Lý do sửa điểm",
              required: editing.total != null,
              maxLength: 300,
            },
          ]}
          onClose={() => setEditing(null)}
          onSave={async (b) => {
            await api("/scores/" + editing.id, "PUT", b);
            refresh();
            notice("Đã lưu điểm.");
          }}
        />
      )}
      {adding && (
        <Editor
          title="Thêm sinh viên vào lớp"
          fields={[
            {
              name: "student_id",
              label: "Sinh viên",
              source: "students",
              required: true,
            },
          ]}
          lookups={lookups}
          onClose={() => setAdding(false)}
          onSave={async (b) => {
            await api("/sections/" + section.id + "/enroll", "POST", b);
            refresh();
            notice("Đã đăng ký sinh viên.");
          }}
        />
      )}
      {confirm && (
        <Confirm
          title={
            confirm.type === "cancel"
              ? "Hủy đăng ký của " + confirm.row.full_name + "?"
              : confirm.type === "lock"
                ? "Chốt và công bố điểm?"
                : "Mở lại điểm?"
          }
          description={
            confirm.type === "cancel"
              ? "Giữ lịch sử đăng ký đã hủy."
              : confirm.type === "lock"
                ? "Cần đóng đăng ký và nhập đủ điểm. Sau khi chốt, sinh viên sẽ xem được kết quả."
                : "Điểm tạm ngừng hiển thị trong bảng điểm sinh viên cho đến khi chốt lại. Thao tác và lý do sẽ được ghi nhật ký."
          }
          label={
            confirm.type === "cancel"
              ? "Hủy đăng ký"
              : confirm.type === "lock"
                ? "Chốt điểm"
                : "Mở lại điểm"
          }
          reasonRequired={confirm.type === "unlock"}
          danger={confirm.type === "cancel"}
          onClose={() => setConfirm(null)}
          onConfirm={async (reason) => {
            if (confirm.type === "cancel")
              await api("/enrollments/" + confirm.row.id, "DELETE");
            else
              await api("/sections/" + section.id + "/lock", "POST", {
                locked: confirm.type === "lock",
                reason,
              });
            refresh();
            notice("Đã cập nhật.");
          }}
        />
      )}
    </>
  );
}

function Transcript({ user, lookups, revision, refresh, notice }) {
  const [student, setStudent] = useState("");
  const admin = user.role === "ADMIN";
  if (admin && !student)
    return (
      <>
        <PageTitle
          title="Bảng điểm sinh viên"
          description="Chọn sinh viên để xem kết quả đã công bố."
        />
        <select
          className="wide-select"
          aria-label="Chọn sinh viên"
          value={student}
          onChange={(e) => setStudent(e.target.value)}
        >
          <option value="">Chọn sinh viên</option>
          {lookups.students?.map((s) => (
            <option key={s.id} value={s.id}>
              {s.code} · {s.full_name}
            </option>
          ))}
        </select>
      </>
    );
  return (
    <TranscriptBody
      key={student || "me"}
      student={admin ? student : null}
      onChange={() => setStudent("")}
      admin={admin}
      revision={revision}
      refresh={refresh}
      notice={notice}
    />
  );
}
function TranscriptBody({
  student,
  onChange,
  admin,
  revision,
  refresh,
  notice,
}) {
  const { data, error, loading } = useData(
    "/transcript" + (student ? "?studentId=" + student : ""),
    revision,
  );
  const [term, setTerm] = useState(""),
    [cancel, setCancel] = useState(null);
  const terms = [
    ...new Map(
      (data?.rows || []).map((r) => [r.semester_id, r.semester_name]),
    ).entries(),
  ];
  const summary = term
    ? data?.semesters.find((t) => String(t.semester_id) === term)
    : data;
  return (
    <>
      {admin && (
        <button className="back-button" onClick={onChange}>
          ← Chọn sinh viên khác
        </button>
      )}
      <PageTitle
        title="Kết quả học tập"
        description="Chỉ tính điểm đã chốt. GPA tích lũy chọn lần có tổng kết cao nhất của mỗi môn."
      />
      <State error={error} loading={loading} />
      {data && (
        <>
          <div className="stats">
            <Stat
              title={term ? "GPA học kỳ" : "GPA tích lũy"}
              value={summary?.gpa ?? "—"}
              subtitle="Thang điểm 4"
            />
            <Stat
              title="Tín chỉ đạt"
              value={summary?.earned_credits ?? 0}
              subtitle="Điểm chữ A, B, C hoặc D"
            />
            <Stat
              title="Tín chỉ đã có điểm"
              value={summary?.graded_credits ?? 0}
              subtitle="Bao gồm học phần chưa đạt"
            />
          </div>
          <DataTable
            rows={data.rows.filter(
              (r) => !term || String(r.semester_id) === term,
            )}
            columns={[
              { key: "subject_code", label: "Mã môn" },
              { key: "subject_name", label: "Môn học" },
              { key: "section_code", label: "Lớp học phần" },
              { key: "semester_name", label: "Học kỳ" },
              { key: "credits", label: "Tín chỉ" },
              ...scoreColumns,
              {
                key: "status",
                label: "Trạng thái",
                value: (r) =>
                  r.status === "CANCELLED"
                    ? "Đã hủy"
                    : r.total == null
                      ? "Chưa công bố"
                      : "Đã công bố",
              },
            ]}
            exportName="bang-diem"
            filters={
              <select
                aria-label="Lọc học kỳ"
                value={term}
                onChange={(e) => setTerm(e.target.value)}
              >
                <option value="">Tất cả học kỳ</option>
                {terms.map(([id, n]) => (
                  <option key={id} value={id}>
                    {n}
                  </option>
                ))}
              </select>
            }
            actions={(r) =>
              small(
                "Hủy đăng ký",
                () => setCancel(r),
                true,
                r.status !== "ACTIVE" ||
                  r.section_status !== "OPEN" ||
                  r.grade_locked,
              )
            }
          />
          <p className="footnote">
            Quy tắc minh họa: A ≥ 8.5; B ≥ 7; C ≥ 5.5; D ≥ 4; F &lt; 4. Điểm hệ
            4 tương ứng 4, 3, 2, 1, 0. Chưa áp dụng quy chế riêng của một
            trường.
          </p>
        </>
      )}
      {cancel && (
        <Confirm
          title={"Hủy đăng ký " + cancel.section_code + "?"}
          description="Chỉ hủy được khi lớp còn mở và chưa có điểm được nhập. Lịch sử đăng ký được giữ lại."
          label="Hủy đăng ký học phần"
          danger
          onClose={() => setCancel(null)}
          onConfirm={async () => {
            await api("/enrollments/" + cancel.id, "DELETE");
            refresh();
            notice("Đã hủy đăng ký.");
          }}
        />
      )}
    </>
  );
}

function AccountsPage({ lookups, revision, refresh, notice }) {
  const { data, error, loading } = useData("/admin/accounts", revision);
  const [editing, setEditing] = useState(null),
    [confirm, setConfirm] = useState(null),
    [role, setRole] = useState("STUDENT"),
    [creating, setCreating] = useState(false);
  const table = role === "STUDENT" ? "students" : "teachers";
  return (
    <>
      <PageTitle
        title="Quản lý tài khoản"
        description="Quyền truy cập được quản lý riêng với hồ sơ và dữ liệu học tập."
      >
        <button onClick={() => setCreating(true)}>+ Cấp tài khoản</button>
      </PageTitle>
      <State error={error} loading={loading} />
      {data && (
        <DataTable
          rows={data}
          exportName="tai-khoan"
          columns={[
            { key: "username", label: "Tên đăng nhập" },
            { key: "full_name", label: "Họ và tên" },
            { key: "role", label: "Vai trò", value: (r) => labels[r.role] },
            {
              key: "enabled",
              label: "Trạng thái",
              value: (r) => (r.enabled ? "Hoạt động" : "Đã khóa"),
              render: (r) => (
                <span
                  className={"badge " + (r.enabled ? "active" : "cancelled")}
                >
                  {r.enabled ? "Hoạt động" : "Đã khóa"}
                </span>
              ),
            },
            {
              key: "must_change",
              label: "Mật khẩu",
              value: (r) =>
                r.must_change ? "Cần đổi mật khẩu" : "Đã thiết lập",
            },
          ]}
          actions={(r) => (
            <>
              {small("Đặt lại mật khẩu", () => setEditing(r))}
              {r.role !== "ADMIN" && (
                <>
                  {small(r.enabled ? "Khóa" : "Mở khóa", () =>
                    setConfirm({ type: "toggle", row: r }),
                  )}
                  {small(
                    "Xóa tài khoản",
                    () => setConfirm({ type: "delete", row: r }),
                    true,
                  )}
                </>
              )}
            </>
          )}
        />
      )}{" "}
      {editing && !editing.provision && (
        <Editor
          title={"Đặt lại mật khẩu · " + editing.username}
          description="Phiên đăng nhập cũ sẽ mất quyền truy cập. Người dùng phải đổi mật khẩu sau khi đăng nhập lại."
          fields={passwordFields}
          onClose={() => setEditing(null)}
          onSave={async (b) => {
            validatePassword(b);
            await api("/admin/accounts/" + editing.id + "/password", "PUT", b);
            refresh();
            notice("Đã đặt lại mật khẩu.");
          }}
        />
      )}
      {confirm && (
        <Confirm
          title={
            confirm.type === "delete"
              ? "Xóa tài khoản " + confirm.row.username + "?"
              : (confirm.row.enabled ? "Khóa " : "Mở khóa ") +
                confirm.row.username +
                "?"
          }
          description={
            confirm.type === "delete"
              ? "Chỉ xóa tài khoản đăng nhập. Hồ sơ, đăng ký học phần và điểm được giữ nguyên."
              : confirm.row.enabled
                ? "Người dùng sẽ không đăng nhập được và phiên cũ mất quyền truy cập. Hồ sơ và điểm được giữ nguyên."
                : "Cho phép tài khoản đăng nhập trở lại."
          }
          danger={confirm.type === "delete"}
          label={
            confirm.type === "delete"
              ? "Xóa tài khoản"
              : confirm.row.enabled
                ? "Khóa tài khoản"
                : "Mở khóa"
          }
          onClose={() => setConfirm(null)}
          onConfirm={async () => {
            await api(
              "/admin/accounts/" +
                confirm.row.id +
                (confirm.type === "toggle" ? "/enabled" : ""),
              confirm.type === "delete" ? "DELETE" : "PUT",
              confirm.type === "toggle"
                ? { enabled: !confirm.row.enabled }
                : undefined,
            );
            refresh();
            notice("Đã cập nhật tài khoản.");
          }}
        />
      )}
      {creating && (
        <Modal
          title="Cấp tài khoản cho hồ sơ có sẵn"
          description="Chọn vai trò và hồ sơ chưa có tài khoản."
          onClose={() => setCreating(false)}
        >
          <label>
            Vai trò
            <select value={role} onChange={(e) => setRole(e.target.value)}>
              <option value="STUDENT">Sinh viên</option>
              <option value="TEACHER">Giảng viên</option>
            </select>
          </label>
          <button
            className="secondary"
            onClick={() => {
              setCreating(false);
              setEditing({ provision: true, role, table });
            }}
          >
            Tiếp tục →
          </button>
        </Modal>
      )}
      {editing?.provision && (
        <Editor
          key={editing.role}
          title="Cấp tài khoản"
          fields={[
            {
              name: "profile_id",
              label: "Hồ sơ chưa có tài khoản",
              source: "orphans",
              required: true,
            },
            ...passwordFields,
          ]}
          lookups={{
            orphans: (lookups[editing.table] || []).filter((r) => !r.user_id),
          }}
          onClose={() => setEditing(null)}
          onSave={async (b) => {
            validatePassword(b);
            await api("/admin/accounts", "POST", { ...b, role: editing.role });
            refresh();
            notice("Đã cấp tài khoản.");
          }}
        />
      )}
    </>
  );
}

function Stat({ title, value, subtitle, icon = "chart" }) {
  return (
    <div className="stat">
      <div className="stat-top">
        <span>{title}</span>
        <span className="stat-icon">
          <Icon name={icon} />
        </span>
      </div>
      <strong>{value}</strong>
      <small>{subtitle}</small>
    </div>
  );
}
function Dashboard({ user, revision, onNavigate }) {
  const { data, error, loading } = useData("/dashboard", revision);
  const [term, setTerm] = useState("");
  const terms = [
    ...new Map(
      (data?.sections || []).map((r) => [r.semester_id, r.semester_name]),
    ).entries(),
  ];
  const shown = (data?.sections || []).filter(
    (r) => !term || String(r.semester_id) === term,
  );
  const name = user.profile?.full_name || "Quản trị viên";
  return (
    <>
      <PageTitle
        title={"Xin chào, " + name}
        description="Theo dõi hoạt động đào tạo và tiếp tục công việc của bạn."
      />
      <div className="welcome-banner">
        <div>
          <span className="eyebrow">KHÔNG GIAN HỌC TẬP CỦA BẠN</span>
          <h2>Mỗi ngày, một bước tiến.</h2>
          <p>
            {user.role === "ADMIN"
              ? "Quản lý dữ liệu nhất quán, theo dõi tiến độ và hỗ trợ người học."
              : user.role === "TEACHER"
                ? "Theo dõi lớp phụ trách và cập nhật kết quả học tập."
                : "Tra cứu lớp học phần và theo dõi hành trình học tập của bạn."}
          </p>
          <button onClick={() => onNavigate("sections")}>
            {user.role === "STUDENT" ? "Đăng ký học phần" : "Xem lớp học phần"}{" "}
            →
          </button>
        </div>
        <div className="banner-symbol">
          <Icon name="book" size={88} />
        </div>
      </div>
      <State error={error} loading={loading} />
      {data && (
        <>
          {user.role === "ADMIN" ? (
            <div className="stats">
              <Stat
                title="Sinh viên"
                value={data.students}
                subtitle="Hồ sơ trong hệ thống"
                icon="users"
              />
              <Stat
                title="Giảng viên"
                value={data.teachers}
                subtitle="Đội ngũ giảng dạy"
                icon="users"
              />
              <Stat
                title="Môn học"
                value={data.subjects}
                subtitle="Danh mục đào tạo"
                icon="book"
              />
              <Stat
                title="Tài khoản"
                value={data.accounts}
                subtitle="Tất cả vai trò"
                icon="lock"
              />
            </div>
          ) : user.role === "STUDENT" ? (
            <div className="stats">
              <Stat
                title="GPA tích lũy"
                value={data.transcript.gpa ?? "—"}
                subtitle="Từ các điểm đã chốt"
              />
              <Stat
                title="Tín chỉ đạt"
                value={data.transcript.earned_credits}
                subtitle="Theo quy tắc của dự án"
              />
              <Stat
                title="Đăng ký còn hiệu lực"
                value={
                  data.transcript.rows.filter((r) => r.status === "ACTIVE")
                    .length
                }
                subtitle="Tất cả học kỳ"
              />
            </div>
          ) : (
            <div className="stats">
              <Stat
                title="Lớp phụ trách"
                value={shown.length}
                subtitle="Theo bộ lọc học kỳ"
                icon="book"
              />
              <Stat
                title="Lượt sinh viên"
                value={shown.reduce((a, r) => a + r.enrolled, 0)}
                subtitle="Theo lớp học phần"
                icon="users"
              />
              <Stat
                title="Lớp đã chốt điểm"
                value={shown.filter((r) => r.grade_locked).length}
                subtitle="Kết quả đã công bố"
                icon="check"
              />
            </div>
          )}
          <div className="section-heading">
            <h2>
              {user.role === "STUDENT"
                ? "Lớp học phần có thể tra cứu"
                : "Tình hình lớp học phần"}
            </h2>
            <select
              aria-label="Học kỳ tổng quan"
              value={term}
              onChange={(e) => setTerm(e.target.value)}
            >
              <option value="">Tất cả học kỳ</option>
              {terms.map(([id, n]) => (
                <option key={id} value={id}>
                  {n}
                </option>
              ))}
            </select>
          </div>
          <div className="course-grid">
            {shown.slice(0, 6).map((s) => (
              <div className="course-card" key={s.id}>
                <div>
                  <span className="course-code">{s.code}</span>
                  <Badge value={s.status} />
                </div>
                <h3>{s.subject_name}</h3>
                <p>{s.semester_name}</p>
                <div className="capacity">
                  <span
                    style={{
                      width:
                        Math.min(100, (s.enrolled / s.capacity) * 100) + "%",
                    }}
                  />
                </div>
                <footer>
                  <span>
                    {s.enrolled} / {s.capacity} sinh viên
                  </span>
                  <span>{s.credits} tín chỉ</span>
                </footer>
              </div>
            ))}
          </div>
          {!shown.length && (
            <p className="empty">Chưa có lớp học phần trong học kỳ này.</p>
          )}
          {data.recent && (
            <div className="activity">
              <h2>Hoạt động gần đây</h2>
              {data.recent.map((a) => (
                <div key={a.id}>
                  <span className="activity-dot" />
                  <div>
                    <strong>{a.action}</strong>
                    <p>{a.detail}</p>
                  </div>
                  <small>{a.actor}</small>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </>
  );
}

function Profile({ user, onChangePassword }) {
  const p = user.profile;
  return (
    <>
      <PageTitle
        title="Tài khoản của tôi"
        description="Thông tin tài khoản và hồ sơ cá nhân."
      />
      <div className="profile-card">
        <div className="avatar large">
          {(p?.full_name || user.username).charAt(0)}
        </div>
        <h2>{p?.full_name || user.username}</h2>
        <Badge value={user.role} />
        <dl>
          {[
            ["Tên đăng nhập", user.username],
            ["Mã hồ sơ", p?.code],
            ["Email", p?.email],
            ["Điện thoại", p?.phone],
            ["Ngày sinh", p?.birth_date],
            ["Trạng thái học tập", labels[p?.status]],
          ]
            .filter(([, v]) => v != null)
            .map(([k, v]) => (
              <div key={k}>
                <dt>{k}</dt>
                <dd>{v || "Chưa cập nhật"}</dd>
              </div>
            ))}
        </dl>
        <button onClick={onChangePassword}>Đổi mật khẩu</button>
        <p className="muted">
          Liên hệ quản trị viên nếu cần chỉnh sửa thông tin hồ sơ.
        </p>
      </div>
    </>
  );
}
function AuditPage({ revision }) {
  const { data, error, loading } = useData("/admin/audit", revision);
  return (
    <>
      <PageTitle
        title="Nhật ký thao tác"
        description="500 thao tác gần nhất. Bao gồm cấp/khóa/xóa tài khoản, đăng ký và thay đổi điểm."
      />
      <State error={error} loading={loading} />
      {data && (
        <DataTable
          rows={data}
          exportName="nhat-ky"
          columns={[
            {
              key: "created_at",
              label: "Thời gian",
              value: (r) => new Date(r.created_at).toLocaleString("vi-VN"),
            },
            { key: "actor", label: "Người thực hiện" },
            { key: "action", label: "Thao tác" },
            { key: "detail", label: "Chi tiết" },
          ]}
        />
      )}
    </>
  );
}

export default function App() {
  const [user, setUser] = useState(null),
    [checking, setChecking] = useState(true),
    [nav, setNav] = useState("dashboard"),
    [revision, setRevision] = useState(0),
    [lookups, setLookups] = useState({}),
    [lookupError, setLookupError] = useState(""),
    [message, setMessage] = useState(""),
    [roster, setRoster] = useState(null),
    [changing, setChanging] = useState(false),
    [menu, setMenu] = useState(false);
  const refresh = () => setRevision((n) => n + 1);
  useEffect(() => {
    api("/auth/me")
      .then(setUser)
      .catch(() => {})
      .finally(() => setChecking(false));
    const expired = () => {
      setUser(null);
      setRoster(null);
    };
    window.addEventListener("session-expired", expired);
    return () => window.removeEventListener("session-expired", expired);
  }, []);
  useEffect(() => {
    if (user?.role !== "ADMIN" || user.must_change) {
      setLookups({});
      return;
    }
    let active = true;
    setLookupError("");
    Promise.all(
      Object.keys(catalogs).map(async (k) => [
        k,
        await api("/admin/catalog/" + k),
      ]),
    )
      .then((rows) => {
        if (active) setLookups(Object.fromEntries(rows));
      })
      .catch((e) => {
        if (active) setLookupError(e.message);
      });
    return () => {
      active = false;
    };
  }, [user, revision]);
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => setMessage(""), 6500);
    return () => clearTimeout(timer);
  }, [message]);
  function navigate(n) {
    setNav(n);
    setRoster(null);
    setMenu(false);
    setMessage("");
  }
  if (checking) return <div className="boot">Đang kết nối UniDesk…</div>;
  if (!user)
    return (
      <Login
        onLogin={(u) => {
          setUser(u);
          setNav("dashboard");
          setRoster(null);
          setMessage("");
        }}
      />
    );
  const admin = user.role === "ADMIN",
    teacher = user.role === "TEACHER";
  const items = [
    ["dashboard", "Tổng quan", "grid"],
    ...(admin
      ? [
          ["students", "Quản lý sinh viên", "users"],
          ["teachers", "Quản lý giảng viên", "users"],
          ["catalog", "Danh mục đào tạo", "book"],
        ]
      : []),
    [
      "sections",
      teacher ? "Lớp phụ trách" : admin ? "Lớp học phần" : "Đăng ký học phần",
      "book",
    ],
    ...(!teacher
      ? [
          [
            "transcript",
            admin ? "Bảng điểm sinh viên" : "Kết quả học tập",
            "chart",
          ],
        ]
      : []),
    ...(admin
      ? [
          ["accounts", "Quản lý tài khoản", "lock"],
          ["audit", "Nhật ký thao tác", "clock"],
        ]
      : []),
    ["profile", "Tài khoản của tôi", "users"],
  ];
  const shared = { user, lookups, revision, refresh, notice: setMessage };
  const forced = user.must_change;
  return (
    <div className="app-shell">
      <aside className={menu ? "sidebar open" : "sidebar"}>
        <a
          className="brand"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            navigate("dashboard");
          }}
        >
          <img className="brand-mark" src="/haui-logo.png" alt="Logo HaUI" />
          <div>
            <strong>UniDesk</strong>
            <small>QUẢN LÝ SINH VIÊN</small>
          </div>
        </a>
        <div className="nav-caption">KHÔNG GIAN LÀM VIỆC</div>
        <nav>
          {items.map(([id, title, icon]) => (
            <button
              key={id}
              className={nav === id ? "nav-item selected" : "nav-item"}
              onClick={() => navigate(id)}
              disabled={forced}
            >
              <Icon name={icon} />
              <span>{title}</span>
              {nav === id && <i />}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <span className="status-dot" /> Phiên bản đồ án · 2.0
          <p>{labels[user.role]}</p>
        </div>
      </aside>
      <div className="main-shell">
        <header className="topbar">
          <div>
            <button
              className="mobile-toggle secondary"
              onClick={() => setMenu(!menu)}
              aria-label="Menu"
            >
              ☰
            </button>
            <span>
              Cổng thông tin <b>{labels[user.role]}</b>
            </span>
          </div>
          <div className="user-menu">
            <div className="avatar">
              {(user.profile?.full_name || user.username).charAt(0)}
            </div>
            <div>
              <strong>{user.profile?.full_name || user.username}</strong>
              <small>{labels[user.role]}</small>
            </div>
            <button
              className="icon-button"
              title="Đăng xuất"
              aria-label="Đăng xuất"
              onClick={async () => {
                try {
                  await logout();
                  setUser(null);
                } catch (e) {
                  setMessage(e.message);
                }
              }}
            >
              <Icon name="exit" />
            </button>
          </div>
        </header>
        <main>
          {message && (
            <div className="toast" role="status">
              {message}
              <button
                onClick={() => setMessage("")}
                aria-label="Đóng thông báo"
              >
                ×
              </button>
            </div>
          )}
          {lookupError && (
            <div className="alert error">
              {lookupError}{" "}
              <button className="secondary" onClick={refresh}>
                Tải lại
              </button>
            </div>
          )}
          {!forced &&
            (roster ? (
              <Roster
                {...shared}
                section={roster}
                onBack={() => setRoster(null)}
              />
            ) : nav === "dashboard" ? (
              <Dashboard {...shared} onNavigate={navigate} />
            ) : ["students", "teachers"].includes(nav) ? (
              <Catalog key={nav} kind={nav} {...shared} />
            ) : nav === "catalog" ? (
              <CatalogTabs {...shared} />
            ) : nav === "sections" ? (
              <Sections {...shared} onRoster={setRoster} />
            ) : nav === "transcript" ? (
              <Transcript {...shared} />
            ) : nav === "accounts" ? (
              <AccountsPage {...shared} />
            ) : nav === "audit" ? (
              <AuditPage {...shared} />
            ) : (
              <Profile user={user} onChangePassword={() => setChanging(true)} />
            ))}
          {forced && (
            <PageTitle
              title="Thiết lập mật khẩu của bạn"
              description="Tài khoản vừa được cấp hoặc đặt lại mật khẩu. Hãy đổi mật khẩu để tiếp tục."
            />
          )}
          <footer className="app-footer">
            UniDesk · Hệ thống quản lý sinh viên{" "}
            <span>Dữ liệu học tập, trong tầm tay.</span>
          </footer>
        </main>
      </div>
      {(changing || forced) && (
        <Editor
          title="Đổi mật khẩu"
          description="Sau khi đổi mật khẩu, hãy đăng nhập lại. Các phiên đăng nhập cũ sẽ mất quyền truy cập."
          fields={[
            {
              name: "oldPassword",
              label: "Mật khẩu hiện tại",
              type: "password",
              required: true,
              maxLength: 200,
            },
            ...passwordFields,
          ]}
          onClose={async () => {
            if (forced) {
              try {
                await logout();
                setUser(null);
              } catch (e) {
                setMessage(e.message);
              }
            } else setChanging(false);
          }}
          onSave={async (b) => {
            validatePassword(b);
            await api("/auth/password", "POST", b);
            setChanging(false);
            setUser(null);
            await token();
          }}
          saveLabel="Đổi mật khẩu và đăng nhập lại"
        />
      )}
    </div>
  );
}
function CatalogTabs(props) {
  const [kind, setKind] = useState("departments");
  return (
    <>
      <div className="tabs">
        {["departments", "majors", "classes", "subjects", "semesters"].map(
          (k) => (
            <button
              className={kind === k ? "active" : ""}
              key={k}
              onClick={() => setKind(k)}
            >
              {catalogs[k].title}
            </button>
          ),
        )}
      </div>
      <Catalog key={kind} kind={kind} {...props} />
    </>
  );
}
