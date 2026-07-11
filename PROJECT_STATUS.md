# LinhanShopping — Tình trạng dự án

> Ghi chú tự động tạo ngày 2026-07-11 dựa trên khảo sát code thực tế (không phải suy đoán). Dùng file này làm điểm khởi đầu để tiếp tục phát triển.

## 1. Kiến trúc tổng quan

Multi-module Maven, Spring Boot 2.4.1, Java 11.

```
linhanshopping-project (root pom)
├── common          → entity dùng chung (JPA)
└── webparent        → pom cha chứa dependency chung (web, JPA, thymeleaf, security, mysql...)
    ├── backend       → trang quản trị (Admin Control Panel), port 8082, context /LinhanShoppingBackend
    └── frontend      → trang bán hàng công khai (site khách xem/mua), port 8080, context /LinhanShoppingFrontend
```

- DB: MySQL, database `linhanshopping`, port **3306**.
- Credential DB lấy qua biến môi trường `DB_USERNAME` / `DB_PASSWORD` (mặc định `root` / `123456789`), đã externalize — xem `application.properties` từng module.
- Build reactor: chạy `./mvnw install` ở root sau khi cài `common` (backend/frontend phụ thuộc jar `linhanshopping-common`).
- Chạy từng app: `cd webparent/backend && ../../mvnw spring-boot:run` (tương tự cho frontend). **Không** dùng `-am` với `spring-boot:run` vì Maven sẽ cố áp goal đó lên cả module `common` (không có main class) và gây lỗi build.

## 2. Đã hoàn thành

### `common` — entity dùng chung
- `IdBasedEntity` — lớp cha chứa `id`.
- `User`, `Role` — đầy đủ, phục vụ authentication/authorization.
- `Category` — entity đã có (field: name, alias, image, enabled) nhưng **chưa có tầng nào phía trên dùng nó** (xem mục 3).

### `backend` (Admin Control Panel) — module hoạt động nhiều nhất
- **Đăng nhập / bảo mật** (`security/WebSecurityConfig.java`, `ShoppingUserDetailsService`, `ShoppingUserDetails`)
  - Form login tại `/login`, xác thực bằng email + BCrypt password.
  - Remember-me 7 ngày.
  - Phân quyền theo `antMatchers` cho từng nhóm route: `/users/**` (Admin), `/categories/**` `/brands/**` (Admin, Editor), `/products/**` phân theo nhiều role (Admin/Editor/Salesperson/Shipper/Assistant tùy hành động).
  - ⚠️ Các rule cho `/categories/**`, `/brands/**`, `/products/**` **đã được viết sẵn** dù controller tương ứng chưa tồn tại — tức là bảo mật được thiết kế trước, code sau.
- **Quản lý Users** (`user/UserController.java`, `UserRestController.java`, `UserService.java`, `UserRepository.java`) — module duy nhất hoàn chỉnh:
  - Danh sách có phân trang, sort, tìm kiếm (`/users`, `/users/page/{n}`).
  - Thêm/sửa/xóa, bật/tắt tài khoản (`/users/new`, `/users/edit/{id}`, `/users/delete/{id}`, `/users/{id}/enabled/{status}`).
  - Export CSV / Excel / PDF (`user/export/UserCsvExporter.java`, `UserExcelExporter.java`, `UserPdfExporter.java`, lớp cha `AbstractExporter.java`).
  - Trang tài khoản cá nhân `/account`.
  - Đã kiểm thử thực tế qua trình duyệt — hoạt động đúng, dữ liệu thật từ MySQL hiển thị chính xác.
- Trang chủ admin + login UI (`MainController.java`, template `index.html`, `login.html`, `navigation.html`, `fragments.html`, `modal_fragments.html`) — có logo, giao diện hoàn chỉnh.
- Upload file (`FileUploadUtil.java`), cấu hình MVC (`config/MvcConfig.java`).

### Build / tooling (commit gần đây)
- Sửa lỗi module path mismatch giữa `pom.xml` khai báo và tên thư mục thực tế (`16b6e37`).
- Externalize DB credentials qua env var, dọn `.gitignore` cho tool/IDE junk (`8edba73`).

## 3. Chưa hoàn thành / đang dang dở

### Backend — các mục menu chưa có logic
| Mục menu | Entity | Repository/Service | Controller | Template | Trạng thái khi bấm |
|---|---|---|---|---|---|
| Categories | ✅ có (`Category.java`) | ❌ chưa | ❌ chưa | ✅ có (`templates/categories/categories.html`) — mồ côi, không controller nào render nó | **404** |
| Brand | ❌ chưa có entity | ❌ | ❌ | ❌ | **404** |
| Product | ❌ chưa có entity | ❌ | ❌ | ❌ | **404** (dù rule phân quyền đã viết sẵn trong `WebSecurityConfig`) |
| Customers | ❌ | ❌ | ❌ | ❌ | Chưa có link trong `navigation.html` — chỉ là chữ tĩnh |
| Oders (Orders) | ❌ | ❌ | ❌ | ❌ | Chưa có link trong `navigation.html` — chỉ là chữ tĩnh |

→ Việc tiếp theo hợp lý nhất: làm **Category** trước (đã có sẵn entity + template, chỉ thiếu Repository/Service/Controller — làm theo đúng pattern của `user/` package), sau đó **Brand**, **Product**, rồi **Customers/Orders** (phải thiết kế từ đầu, kể cả entity).

### Frontend — gần như trống hoàn toàn
- Chỉ có duy nhất `FrontendApplication.java` (main class). Không có Controller, không có template, không có gì khác ngoài `application.properties`.
- Vì `webparent/pom.xml` (pom cha dùng chung) khai báo `spring-boot-starter-security`, module frontend tự động bị Spring Security auto-config khóa **toàn bộ route** và hiện trang login mặc định (không phải giao diện tùy chỉnh) khi chạy.
- Việc cần làm: viết Controller + template trang chủ, trang danh mục/sản phẩm, giỏ hàng... rồi viết `SecurityConfig` riêng cho frontend (permitAll cho trang public, chỉ khóa phần cần đăng nhập như checkout/tài khoản) — hiện chưa có config nào cả nên nó rơi vào trạng thái "khóa hết theo mặc định".
- `application.properties` của frontend có cấu hình mail SMTP (`spring.mail.*`) nhưng vẫn để giá trị placeholder `your-email@gmail.com` / `your-app-password` — cần điền thật khi cần dùng tính năng gửi mail.
- Đã sửa 1 lỗi nhỏ: `spring.datasource.url` của frontend trỏ nhầm cổng 3310 (không có gì chạy ở đó), đã sửa về 3306 cho khớp MySQL đang chạy trên máy này.

## 4. Gợi ý lộ trình tiếp theo

1. **Category (backend)** — viết `CategoryRepository`, `CategoryService`, `CategoryController` theo đúng pattern của `user/` package; nối với template `categories.html` đã có sẵn.
2. **Brand (backend)** — tạo entity `Brand` trong `common`, rồi Repository/Service/Controller/template tương tự.
3. **Product (backend)** — entity phức tạp hơn (liên kết Category, Brand), rule phân quyền đã có sẵn trong `WebSecurityConfig`, chỉ cần code phần còn lại.
4. **Customers / Orders (backend)** — thiết kế từ đầu: entity, quan hệ với User, Controller, template, và thêm link vào `navigation.html`.
5. **Frontend** — bắt đầu với trang chủ public (Controller + Thymeleaf template hiển thị danh mục/sản phẩm), sau đó viết `SecurityConfig` riêng để mở các route public, tránh bị khóa toàn bộ như hiện tại.
6. Điền thông tin mail SMTP thật trong `frontend/application.properties` khi cần dùng chức năng gửi email (quên mật khẩu, xác nhận đơn hàng...).
