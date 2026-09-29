export const labels = {
  ADMIN: "Quản trị viên",
  TEACHER: "Giảng viên",
  STUDENT: "Sinh viên",
  STUDYING: "Đang học",
  PAUSED: "Bảo lưu",
  LEFT: "Thôi học",
  GRADUATED: "Tốt nghiệp",
  OPEN: "Mở đăng ký",
  CLOSED: "Đóng đăng ký",
  COMPLETED: "Đã kết thúc",
  ACTIVE: "Đang đăng ký",
  CANCELLED: "Đã hủy",
  MALE: "Nam",
  FEMALE: "Nữ",
  OTHER: "Khác",
};
const f = (name, label, extra = {}) => ({
  name,
  label,
  required: true,
  ...extra,
});
const code = f("code", "Mã", {
  maxLength: 30,
  pattern: "[A-Za-z0-9._-]{2,30}",
});
const name = f("name", "Tên", { maxLength: 150 });
const full = f("full_name", "Họ và tên", { maxLength: 150 });
const email = f("email", "Email", {
  type: "email",
  required: false,
  maxLength: 150,
});
const phone = f("phone", "Số điện thoại", { required: false, maxLength: 20 });
const select = (name, label, source) => f(name, label, { source });
const options = (name, label, values) =>
  f(name, label, {
    options: values.map((value) => ({ value, label: labels[value] })),
  });
export const catalogs = {
  students: {
    title: "Quản lý sinh viên",
    single: "sinh viên",
    description: "Hồ sơ, lớp hành chính và trạng thái học tập.",
    fields: [
      code,
      full,
      email,
      phone,
      f("birth_date", "Ngày sinh", { type: "date", required: false }),
      options("gender", "Giới tính", ["MALE", "FEMALE", "OTHER"]),
      select("class_id", "Lớp hành chính", "classes"),
      options("status", "Trạng thái học tập", [
        "STUDYING",
        "PAUSED",
        "LEFT",
        "GRADUATED",
      ]),
    ],
    columns: ["code", "full_name", "class_id", "email", "phone", "status"],
  },
  teachers: {
    title: "Quản lý giảng viên",
    single: "giảng viên",
    description: "Thông tin giảng viên và đơn vị công tác.",
    fields: [
      code,
      full,
      email,
      phone,
      select("department_id", "Khoa", "departments"),
    ],
    columns: ["code", "full_name", "department_id", "email", "phone"],
  },
  departments: {
    title: "Khoa",
    single: "khoa",
    description: "Các đơn vị đào tạo trong hệ thống.",
    fields: [code, name],
    columns: ["code", "name"],
  },
  majors: {
    title: "Ngành đào tạo",
    single: "ngành",
    description: "Danh sách ngành theo khoa.",
    fields: [code, name, select("department_id", "Khoa", "departments")],
    columns: ["code", "name", "department_id"],
  },
  classes: {
    title: "Lớp hành chính",
    single: "lớp hành chính",
    description: "Lớp sinh hoạt của sinh viên theo ngành.",
    fields: [code, name, select("major_id", "Ngành", "majors")],
    columns: ["code", "name", "major_id"],
  },
  subjects: {
    title: "Môn học",
    single: "môn học",
    description: "Danh mục môn học và số tín chỉ.",
    fields: [
      code,
      name,
      f("credits", "Số tín chỉ", {
        type: "number",
        min: 1,
        max: 30,
        default: 3,
      }),
    ],
    columns: ["code", "name", "credits"],
  },
  semesters: {
    title: "Học kỳ",
    single: "học kỳ",
    description: "Thời gian bắt đầu và kết thúc mỗi học kỳ.",
    fields: [
      code,
      name,
      f("starts_on", "Ngày bắt đầu", { type: "date" }),
      f("ends_on", "Ngày kết thúc", { type: "date" }),
    ],
    columns: ["code", "name", "starts_on", "ends_on"],
  },
};
export const sectionFields = [
  code,
  select("subject_id", "Môn học", "subjects"),
  select("semester_id", "Học kỳ", "semesters"),
  select("teacher_id", "Giảng viên", "teachers"),
  f("capacity", "Sĩ số tối đa", {
    type: "number",
    min: 1,
    max: 1000,
    default: 40,
  }),
  f("room", "Phòng học", { required: false, maxLength: 50 }),
  f("schedule_text", "Lịch học (mô tả)", { required: false, maxLength: 200 }),
  options("status", "Trạng thái", ["OPEN", "CLOSED", "COMPLETED"]),
  f("weight1", "Tỷ trọng TX1 (%)", {
    type: "number",
    min: 0,
    max: 100,
    default: 20,
  }),
  f("weight2", "Tỷ trọng TX2 (%)", {
    type: "number",
    min: 0,
    max: 100,
    default: 20,
  }),
  f("weight_final", "Tỷ trọng KTHP (%)", {
    type: "number",
    min: 0,
    max: 100,
    default: 60,
  }),
];
export const passwordFields = [
  f("newPassword", "Mật khẩu mới", {
    type: "password",
    minLength: 12,
    maxLength: 72,
  }),
  f("confirmPassword", "Nhập lại mật khẩu", {
    type: "password",
    minLength: 12,
    maxLength: 72,
  }),
];
export function validatePassword(b, key = "newPassword") {
  if (
    !b[key]?.trim() ||
    b[key].length < 12 ||
    new TextEncoder().encode(b[key]).length > 72
  )
    throw new Error("Mật khẩu cần ít nhất 12 ký tự, tối đa 72 byte UTF-8.");
  if (b[key] !== b.confirmPassword)
    throw new Error("Hai lần nhập mật khẩu không khớp.");
}
