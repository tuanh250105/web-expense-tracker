package com.expensemanager.service;

import com.expensemanager.dao.FAQRepository;
import com.expensemanager.model.FAQ;

import java.util.List;
import java.util.UUID;

public class FAQService {
    private FAQRepository repository;

    public FAQService() {
        this.repository = new FAQRepository();
    }

    public List<FAQ> getAllFAQs() {
        return repository.getAll();
    }

    public void addFAQ(FAQ faq) {
        repository.add(faq);
    }

    public void deleteFAQ(UUID id) {
        repository.delete(id);
    }

    // Phương thức init để thêm dữ liệu mẫu (gọi một lần, ví dụ trong listener hoặc servlet init)
    public void initSampleFAQs() {
        // Thêm FAQ với answer chứa HTML link
        addFAQ(new FAQ("Ứng dụng Expense Manager là gì và dành cho ai?",
                "Expense Manager là ứng dụng web hỗ trợ quản lý chi tiêu cá nhân hoặc nhóm hiệu quả. Nó ghi chép giao dịch, lập kế hoạch ngân sách, theo dõi nợ nần, lập lịch giao dịch tự động, và thêm yếu tố giải trí qua trò chơi thưởng. Phù hợp cho cá nhân (sinh viên, nhân viên), gia đình, hoặc nhóm bạn bè chia sẻ chi phí (như chuyến du lịch). Với giao diện thân thiện, bạn có thể truy cập từ máy tính hoặc điện thoại. Khám phá thêm tại <a href=\"/\">Trang Chủ</a>."));

        addFAQ(new FAQ("Làm thế nào để đăng nhập hoặc đăng ký tài khoản?",
                "Truy cập <a href=\"/\">Trang Chủ</a>, chọn \"Đăng Ký\" để tạo tài khoản mới với email, mật khẩu, và thông tin cơ bản (tên, loại tài khoản). Xác nhận qua email để đăng nhập. Nếu quên mật khẩu, dùng \"Quên Mật Khẩu\" để reset. Tài khoản cá nhân hóa đảm bảo dữ liệu an toàn trên đám mây với mã hóa."));

        addFAQ(new FAQ("Trang Tổng Quan (Overview) hiển thị những gì?",
                "Trang Overview là trung tâm, cho thấy số dư tài khoản, biểu đồ chi tiêu (pie chart theo danh mục), giao dịch gần nhất, và cảnh báo vượt ngân sách. Ví dụ: Nếu \"Ăn Uống\" chiếm 40% thu nhập, biểu đồ sẽ highlight. Cập nhật giao dịch thường xuyên để dữ liệu chính xác, hỗ trợ dự báo xu hướng. Xem ngay tại <a href=\"/overview\">Trang Tổng Quan</a>."));

        addFAQ(new FAQ("Cách ghi nhận giao dịch (Transactions) như thế nào?",
                "Vào <a href=\"/transactions\">Giao Dịch</a>, chọn \"Thêm Mới\", nhập loại (chi tiêu/thu nhập), số tiền, danh mục, ghi chú, và tài khoản. Lọc theo thời gian hoặc tìm kiếm từ khóa. Hữu ích để theo dõi chi tiết và phân tích xu hướng (như chi tiêu cuối tuần tăng)."));

        addFAQ(new FAQ("Giao dịch định kỳ (Scheduled Transactions) hoạt động ra sao?",
                "Tính năng <a href=\"/scheduled_transactions\">Giao Dịch Định Kỳ</a> lập lịch tự động (hóa đơn, lương). Chọn \"Tạo Mới\", nhập số tiền, danh mục, và lịch (ví dụ: ngày 1 hàng tháng). Ứng dụng tự tạo giao dịch khi đến hạn, cho phép skip hoặc run now. Tiết kiệm thời gian và tránh quên thanh toán."));

        // Thêm các FAQ còn lại tương tự, với link phù hợp như /accounts, /budgets, /debts, /charts, /calendar, /group_expenses, /import_export, /settings/mode, /top_categories, /manage_categories, /contact, /settings/email, /events, /gamification
        addFAQ(new FAQ("Quản lý tài khoản (Accounts) có những tính năng gì?",
                "Phần <a href=\"/accounts\">Tài Khoản</a> theo dõi nhiều tài khoản (ví tiền, thẻ tín dụng). Thêm tài khoản với tên và số dư ban đầu, ứng dụng cập nhật tự động. Liên kết ngân hàng (qua API an toàn) để lấy lịch sử. Xem tổng số dư và chuyển tiền giữa tài khoản."));

        addFAQ(new FAQ("Làm thế nào để thiết lập ngân sách (Budgets)?",
                "Vào <a href=\"/budgets\">Ngân Sách</a>, chọn danh mục và đặt giới hạn (ví dụ: 5 triệu VND/tháng). Theo dõi qua thanh tiến trình và nhận cảnh báo khi vượt 80%. Điều chỉnh linh hoạt. Hỗ trợ kiểm soát chi tiêu và tiết kiệm."));

        addFAQ(new FAQ("Quản lý nợ (Debts) giúp ích gì cho người dùng?",
                "<a href=\"/debts\">Quản Lý Nợ</a> theo dõi nợ thu/trả (vay bạn, trả góp). Thêm với số tiền, lãi suất, hạn chót, nhận nhắc nhở qua email. Tính toán tự động khi thanh toán, tránh quên nợ."));

        addFAQ(new FAQ("Tổng hợp biểu đồ (Charts) có những loại nào?",
                "<a href=\"/charts\">Phần Biểu Đồ</a> cung cấp pie chart (phân bổ danh mục), line chart (xu hướng chi tiêu), bar chart (so sánh tháng). Tùy chỉnh lọc theo tài khoản/loại. Hình ảnh hóa dữ liệu giúp nhận diện vấn đề chi tiêu."));

        addFAQ(new FAQ("Lịch (Calendar) dùng để làm gì?",
                "<a href=\"/calendar\">Lịch</a> hiển thị giao dịch định kỳ, hạn nợ, và sự kiện tài chính. Thêm sự kiện mới và nhận thông báo. Hỗ trợ lập kế hoạch dài hạn và dự báo chi tiêu."));

        addFAQ(new FAQ("Chi tiêu theo nhóm (Group Expenses) hoạt động như thế nào?",
                "Tạo <a href=\"/group_expenses\">Nhóm Chi Tiêu</a>, mời thành viên qua email. Thêm giao dịch chung, ứng dụng tính phần chia và nợ nhóm. Lý tưởng cho nhóm bạn bè, giảm tranh cãi tài chính."));

        addFAQ(new FAQ("Cách import/export file trong ứng dụng?",
                "Vào <a href=\"/import_export\">Import/Export</a>, nhập file CSV/Excel theo mẫu để thêm giao dịch. Export dữ liệu (giao dịch, báo cáo) ra file. Dễ dàng chuyển dữ liệu hoặc chia sẻ báo cáo."));

        addFAQ(new FAQ("Dark Mode/Light Mode là gì và cách chuyển?",
                "Chuyển đổi <a href=\"/settings/mode\">Chế Độ Giao Diện</a> giữa Dark Mode (nền tối) và Light Mode (nền sáng) qua nút góc trên, hoặc tự động theo thiết bị. Giảm mỏi mắt và cá nhân hóa trải nghiệm."));

        addFAQ(new FAQ("Thống kê Top Categories giúp ích gì?",
                "<a href=\"/top_categories\">Top Danh Mục</a> hiển thị 5 danh mục chi tiêu nhiều nhất (như \"Ăn Uống\" 30%). So sánh tháng/năm. Nhận diện \"lỗ hổng\" chi tiêu để điều chỉnh."));

        addFAQ(new FAQ("Quản lý danh mục (Manage Categories) như thế nào?",
                "<a href=\"/manage_categories\">Quản Lý Danh Mục</a> thêm/sửa/xóa danh mục (như \"Du Lịch\") với biểu tượng/màu sắc. Cá nhân hóa theo nhu cầu, cải thiện báo cáo."));

        addFAQ(new FAQ("Phần Help - Hỏi Đáp này dùng để gì?",
                "Đây là nơi tìm câu trả lời nhanh cho thắc mắc, như cách dùng <a href=\"/scheduled_transactions\">Giao Dịch Định Kỳ</a>. Nội dung cập nhật thường xuyên. Tự giải quyết vấn đề mà không cần hỗ trợ."));

        addFAQ(new FAQ("Cách gửi góp ý hoặc liên hệ hỗ trợ?",
                "Vào <a href=\"/contact\">Góp Ý - Liên Hệ</a>, điền form với ý kiến/lỗi. Phản hồi qua email trong 24-48 giờ. Giúp cải thiện ứng dụng dựa trên phản hồi."));

        addFAQ(new FAQ("Tự động gửi sao kê qua mail hoạt động ra sao?",
                "Thiết lập <a href=\"/settings/email\">Gửi Email</a> với tần suất (tuần/tháng). Nhận báo cáo PDF qua email, bao gồm giao dịch và biểu đồ. Lưu trữ và chia sẻ dễ dàng."));

        addFAQ(new FAQ("Quản lý sự kiện tài chính (Events) là gì?",
                "Thêm <a href=\"/events\">Sự Kiện</a> như \"Sinh Nhật\" với ngân sách và giao dịch liên quan. Nhận nhắc nhở, tích hợp vào lịch. Lập kế hoạch chi tiêu đặc biệt."));

        addFAQ(new FAQ("Trò chơi thưởng thêm (Gamification) để giải trí như thế nào?",
                "<a href=\"/gamification\">Trò Chơi Thưởng</a> có \"Thử Thách Tiết Kiệm\": Đạt mục tiêu ngân sách để nhận huy hiệu ảo/điểm thưởng (gợi ý ưu đãi). Chơi sau cập nhật giao dịch. Khuyến khích thói quen tốt và thư giãn."));
    }
}