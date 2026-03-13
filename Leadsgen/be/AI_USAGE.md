### AI_USAGE LOG
1. Ngày giờ: 13/03/2026 10:00
   Công cụ: GitHub Copilot (GPT-5.3-Codex)
   Prompt: "Xây dựng REST API quản lý sản phẩm và giỏ hàng bằng Java 17 + Spring Boot 3 + JPA + H2 + Lombok theo đề test internship."
   Sau đó tôi làm gì: Tôi tự viết Entity và DTO. Tôi dùng AI để hỗ trợ phần repository/service/controller, logic check stock, validation, exception handling và rà soát luồng API.

2. Ngày giờ: 13/03/2026 18:30
   Công cụ: GitHub Copilot (GPT-5.3-Codex)
   Prompt: "Deploy nhanh lên Railway hoặc Render"
   Sau đó tôi làm gì: AI hỗ trợ tạo file `render.yaml`, `railway.toml`, thêm `server.port=${PORT:8080}` và cập nhật hướng dẫn deploy trong `HELP.md`.

3. Ngày giờ: 13/03/2026 19:00
   Công cụ: GitHub Copilot (GPT-5.3-Codex)
   Prompt: "Tôi muốn test các API bằng Swagger cho trực quan với BE"
   Sau đó tôi làm gì: AI hỗ trợ thêm springdoc OpenAPI vào `pom.xml`, cấu hình `/swagger-ui.html`, `/v3/api-docs`, và cập nhật tài liệu test API.

4. Ngày giờ: 13/03/2026 19:10
   Công cụ: GitHub Copilot (GPT-5.3-Codex)
   Prompt: "Tôi bị lỗi Failed to load API definition, response status is 500 /v3/api-docs"
   Sau đó tôi làm gì: AI hỗ trợ tái hiện lỗi, đọc stacktrace, nâng phiên bản springdoc tương thích Spring Boot 3.5 và verify lại Swagger hoạt động bình thường.

5. Ngày giờ: 13/03/2026 19:20
   Công cụ: GitHub Copilot (GPT-5.3-Codex)
   Prompt: "Hãy sửa lại thông tin sản phẩm với API có sẵn https://dummyjson.com"
   Sau đó tôi làm gì: AI hỗ trợ cập nhật `DataInitializer` để lấy dữ liệu từ `https://dummyjson.com/products?limit=12`, map field phù hợp và loại bỏ dữ liệu sản phẩm hardcode/fallback theo yêu cầu.
