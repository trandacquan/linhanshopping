# Category Feature — Giải thích chi tiết

> Ghi chú ngày 2026-07-11. File này giải thích toàn bộ cách chức năng quản lý **Category** (danh mục sản phẩm) được xây dựng trong module `backend` (Admin Control Panel), theo đúng pattern đã có sẵn của module `user/`. Đọc file này cùng với code đã có comment tiếng Việt trong từng file để dễ theo dõi.

## 1. Kiến trúc tổng quan: 4 tầng

Mọi chức năng CRUD trong project này (User, Category, sau này là Brand/Product...) đều đi theo đúng 4 tầng sau, từ dưới lên:

```
Database (MySQL, bảng "categories")
        ↑
CategoryRepository   (tầng truy vấn DB — Spring Data JPA)
        ↑
CategoryService      (tầng business logic — gọi Repository, xử lý nghiệp vụ)
        ↑
CategoryController   (tầng điều phối HTTP — nhận request, gọi Service, chọn template trả về)
        ↑
categories.html / category_form.html   (tầng giao diện — Thymeleaf template)
```

Nguyên tắc: **Controller không bao giờ gọi thẳng Repository**, luôn đi qua Service. Service không biết gì về HTTP (không có `HttpServletResponse`, `RedirectAttributes`...), chỉ nhận tham số thuần và trả về Entity/Page/List.

Các file liên quan (tất cả nằm trong `webparent/backend/src/main/java/com/linhanshopping/backend/category/`, trừ `Category.java` nằm ở module `common`):

| File | Vai trò |
|---|---|
| `common/.../entity/Category.java` | Entity JPA, map với bảng `categories` |
| `category/CategoryRepository.java` | Truy vấn DB (Spring Data JPA) |
| `category/CategoryService.java` | Business logic |
| `category/CategoryController.java` | Xử lý HTTP request/response |
| `category/CategoryNotFoundException.java` | Exception riêng khi không tìm thấy category |
| `category/export/CategoryCsvExporter.java` | Xuất danh sách ra file CSV |
| `category/export/CategoryExcelExporter.java` | Xuất ra file Excel (.xlsx) |
| `category/export/CategoryPdfExporter.java` | Xuất ra file PDF |
| `templates/categories/categories.html` | Trang danh sách (list, search, sort, phân trang) |
| `templates/categories/category_form.html` | Form tạo mới / chỉnh sửa |

## 2. Entity — `Category.java`

```java
@Entity
@Table(name = "categories")
public class Category extends IdBasedEntity {
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false, unique = true) private String alias;
    @Column(nullable = false) private String image;
    private boolean enabled;
    ...
    @Transient
    public String getImagePath() { ... }
}
```

- `IdBasedEntity` (lớp cha dùng chung cho mọi entity) cung cấp field `id` (Integer, auto-increment).
- `name`, `alias` là `unique = true` — DB sẽ từ chối 2 category trùng tên hoặc trùng alias.
- `image` là `nullable = false` — **luôn phải có giá trị**, không được để `null` khi save (xem mục 5, bug #2 liên quan gián tiếp tới điều này).
- `getImagePath()` (`@Transient` — không lưu xuống DB, chỉ tính toán khi cần) trả về đường dẫn ảnh để hiển thị trên giao diện.

## 3. Repository — `CategoryRepository.java`

```java
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>{
    public Long countById(Integer id);

    @Query("SELECT c FROM Category c WHERE c.name LIKE %?1%")
    public Page<Category> findAll(String keyword, Pageable pageable);

    @Query("UPDATE Category c SET c.enabled = ?2 WHERE c.id = ?1")
    @Modifying
    public void updateEnabledStatus(Integer id, boolean enabled);
}
```

- Kế thừa `PagingAndSortingRepository<Category, Integer>` → tự động có sẵn `save`, `findById`, `deleteById`, `findAll(Pageable)`, `findAll(Sort)`... không cần viết gì thêm.
- `countById`: **derived query method** — Spring Data tự sinh SQL dựa vào tên hàm (`countBy` + field `Id`). Dùng để kiểm tra tồn tại trước khi update/xóa.
- `findAll(String keyword, Pageable pageable)`: query JPQL tùy chỉnh, tìm category có `name` chứa `keyword` (không phân biệt hoa/thường tùy collation MySQL). Đây là **overload** của `findAll()` có sẵn, không phải override.
- `updateEnabledStatus`: query `UPDATE` trực tiếp (không load Entity lên rồi save lại) — bắt buộc phải có `@Modifying` khi query không phải `SELECT`.

## 4. Service — `CategoryService.java`

| Hàm | Việc làm |
|---|---|
| `listAll()` | Lấy tất cả category, sort theo `name` — dùng cho export |
| `listByPage(pageNum, sortField, sortDir, keyword)` | Lấy category theo trang, có sort + tìm kiếm |
| `save(category)` | Tạo mới nếu `id == null`, cập nhật nếu `id != null` (Spring Data tự phân biệt) |
| `get(id)` | Lấy 1 category, ném `CategoryNotFoundException` nếu không có |
| `delete(id)` | Kiểm tra tồn tại (`countById`) trước khi xóa, tránh lỗi khó hiểu |
| `updateCategoryEnabledStatus(id, enabled)` | Bật/tắt category |

Lưu ý: `pageNum` trên URL bắt đầu từ 1 (thân thiện người dùng: `/categories/page/1`), nhưng `PageRequest.of(...)` của Spring Data bắt đầu từ 0 → phải trừ đi 1: `PageRequest.of(pageNum - 1, ...)`.

## 5. Controller — `CategoryController.java`

Bảng route đầy đủ:

| Route | Method | Việc làm |
|---|---|---|
| `/categories` | GET | Redirect sang `/categories/page/1` |
| `/categories/page/{pageNum}` | GET | Render danh sách (`categories/categories.html`) |
| `/categories/new` | GET | Mở form tạo mới |
| `/categories/save` | POST | Lưu category (tạo mới hoặc cập nhật) + upload ảnh |
| `/categories/edit/{id}` | GET | Mở form sửa |
| `/categories/delete/{id}` | GET | Xóa category + xóa thư mục ảnh |
| `/categories/{id}/enabled/{status}` | GET | Bật/tắt category |
| `/categories/export/csv` \| `/excel` \| `/pdf` | GET | Xuất file |

### Luồng tạo mới / cập nhật category (`saveCategory`)

```
User bấm Save trên form
  → POST /categories/save (multipart/form-data, có th:object="${category}")
  → nếu người dùng CÓ chọn ảnh mới:
        1. lấy tên file (StringUtils.cleanPath tránh path traversal)
        2. gán vào category.image, save xuống DB TRƯỚC (để chắc chắn có category.getId()
           — quan trọng với trường hợp TẠO MỚI, vì id chỉ có sau khi INSERT)
        3. dọn thư mục ảnh cũ (FileUploadUtil.cleanDir) rồi lưu file ảnh mới
           (FileUploadUtil.saveFile) vào "../category-images/{id}/"
  → nếu KHÔNG chọn ảnh mới:
        giữ nguyên tên ảnh cũ (lấy từ hidden input th:field="*{image}" trong form)
        rồi save xuống DB
  → redirect về trang 1, kèm keyword = tên category vừa lưu (để thấy ngay kết quả)
```

### Luồng xóa (`deleteCategory`)

```
GET /categories/delete/{id}
  → categoryService.delete(id) — ném CategoryNotFoundException nếu id không tồn tại
  → nếu xóa DB thành công: xóa luôn thư mục "../category-images/{id}/" (dọn rác)
  → bắt CategoryNotFoundException nếu có -->không để lộ lỗi 500, chỉ redirect kèm flash message
```

## 6. 2 bug thật đã phát hiện khi chạy thử (rất đáng nhớ)

Cả 2 bug này **không hiện ra khi compile**, chỉ phát hiện được khi chạy app thật và thao tác qua HTTP (browser hoặc curl mô phỏng browser). Đây là lý do vì sao bước "chạy thử" luôn cần thiết dù code đã compile OK.

### Bug #1 — `Category.getImagePath()` thiếu dấu `/`

```java
// SAI (bug cũ):
return "/category-images" + this.id + "/" + this.image;
// -->với id=1, image="logo.png" -->ra "/category-images1/logo.png" (thiếu / giữa "category-images" và "1")

// ĐÚNG (đã sửa):
return "/category-images/" + this.id + "/" + this.image;
// -->ra đúng "/category-images/1/logo.png"
```

### Bug #2 — sai thư mục lưu ảnh so với nơi được expose ra URL

`MvcConfig.java` (file cấu hình có sẵn từ trước, không phải do làm Category tạo ra) khai báo:

```java
exposeDirectory("../category-images", registry); // ../ = đi ra NGOÀI thư mục backend, ngang hàng với webparent/
```

Nghĩa là ảnh phải nằm ở `webparent/category-images/`, **KHÔNG PHẢI** `webparent/backend/category-images/`. Lần đầu viết `CategoryController`, `uploadDir` được viết là:

```java
// SAI (bug cũ): lưu ảnh vào bên TRONG module backend
String uploadDir = "category-images/" + savedCategory.getId();

// ĐÚNG (đã sửa): lưu ảnh ra NGOÀI, khớp với MvcConfig
String uploadDir = "../category-images/" + savedCategory.getId();
```

Hậu quả khi còn bug: ảnh **lưu được** (không lỗi khi upload) nhưng khi hiển thị thì **404** vì Spring tìm ảnh ở thư mục khác với nơi thực sự lưu. Đây là kiểu bug "âm thầm" rất dễ bỏ sót nếu không thực sự bấm thử trên giao diện.

**Bài học chung:** khi thêm 1 entity mới có upload ảnh (Brand, Product sau này), luôn kiểm tra 2 chỗ phải khớp nhau:
1. `MvcConfig.addResourceHandlers()` — nơi ảnh được "public" ra URL.
2. `Controller` — nơi ảnh thực sự được lưu (`FileUploadUtil.saveFile`).

## 7. Cách chạy thử lại (nếu muốn tự kiểm tra)

```bash
# 1. Cài common vào local repo (backend phụ thuộc jar này)
cd common && ./mvnw -o install -DskipTests

# 2. Chạy backend (KHÔNG dùng -am, sẽ lỗi vì common không có main class)
cd webparent/backend && ./mvnw -o spring-boot:run
```

- Cần MySQL chạy sẵn ở `localhost:3306`, database `linhanshopping` (xem `application.properties`).
- Truy cập: `http://localhost:8082/LinhanShoppingBackend/login`
- Đăng nhập bằng tài khoản có role `Admin` hoặc `Editor` (rule phân quyền trong `WebSecurityConfig.java`: `/categories/**` yêu cầu 1 trong 2 role này).

## 8. Dữ liệu mẫu hiện có

Đã insert 8 category mẫu cho ví dụ website bán cà phê (bảng `categories`, id 11–18): Cà Phê Rang Xay, Cà Phê Hòa Tan, Cà Phê Hạt Nguyên, Cà Phê Pha Phin, Cà Phê Sữa, Dụng Cụ Pha Chế, Phụ Kiện Cà Phê, Cà Phê Capsule. Tất cả `enabled = true`, ảnh đang là placeholder `default.png` (chưa có file ảnh thật — upload qua form edit khi cần ảnh thật).

## 9. Áp dụng pattern này cho Brand / Product (bước tiếp theo)

Khi làm `Brand` hoặc `Product`, lặp lại đúng các bước đã làm với Category:

1. Tạo entity trong `common` (nếu chưa có), chú ý field nào cần `nullable=false`/`unique=true`.
2. `XxxRepository` — kế thừa `PagingAndSortingRepository`, thêm `countById` + query tìm kiếm theo keyword + (nếu cần) query update trạng thái enabled.
3. `XxxService` — copy cấu trúc `CategoryService`, đổi tên entity.
4. `XxxController` — copy cấu trúc `CategoryController`; **nhớ kiểm tra kỹ `uploadDir` phải khớp với `MvcConfig`** (bug #2 ở trên).
5. Template `xxx.html` / `xxx_form.html` — copy cấu trúc `categories.html` / `category_form.html`, đổi field names.
6. **Luôn chạy thử thật qua HTTP** (không chỉ compile) trước khi coi là xong — như đã thấy, 2 bug ở trên chỉ lộ ra khi chạy thật.
