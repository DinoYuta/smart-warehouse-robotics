(function () {
  var storagePrefix = "warehouse.settings.";
  var root = document.documentElement;
  var defaultTheme = root.dataset.defaultTheme || "light";
  var defaults = {
    language: "en",
    theme: defaultTheme,
    density: "comfortable"
  };
  var allowedValues = {
    language: ["en", "vi"],
    theme: ["light", "dark"],
    density: ["comfortable", "compact"]
  };
  var applyingSettings = false;
  var titleSource = document.title || "";

  var keyedTranslations = {
    en: {
      accessDenied: "Access denied",
      activeRules: "Active Rules",
      activeWaiting: "Active / Waiting",
      allMissions: "All non-deleted missions",
      admin: "Admin",
      applySettings: "Apply Settings",
      assignedRobot: "Assigned Robot",
      cancellationNote: "Cancellation note",
      cancellationReason: "Cancellation reason",
      cancelledAt: "Cancelled at",
      cancelledBy: "Cancelled by",
      cancelledStopped: "Cancelled / Stopped",
      changePassword: "Change Password",
      codeStatus: "Code Status",
      clearNotifications: "Clear all",
      comfortable: "Comfortable",
      compact: "Compact",
      complete: "Complete",
      completed: "Completed",
      completedHistory: "Completed history",
      confirmPassword: "Confirm Password",
      createPickupCode: "Create Pickup Code",
      customerEmail: "Customer Email",
      customerName: "Customer Name",
      customerPhone: "Customer Phone",
      customerPickupCode: "Customer Pickup Code",
      customerPickupCodes: "Customer Pickup Codes",
      dashboard: "Dashboard",
      decision: "Decision",
      delete: "Delete",
      density: "UI Density",
      details: "Details",
      emailPreview: "Email Preview",
      english: "English",
      execution: "Execution",
      language: "Language",
      lifecycle: "Lifecycle",
      lightMode: "Light mode",
      liveMap: "Live Map",
      liveWarehouseMap: "Live Map",
      logout: "Logout",
      manager: "Manager",
      matchedRule: "Matched Rule",
      missionReview: "Mission review",
      missionStatus: "Mission Status",
      myMissions: "Staff Missions",
      newPickupRequest: "New Pickup Request",
      newPassword: "New Password",
      noNotifications: "No notifications",
      notifications: "Notifications",
      pickupCode: "Pickup Code",
      pickupRequest: "Pickup Request",
      policyAssignment: "Policy Assignment",
      process: "Process",
      processPickupCode: "Process Pickup Code",
      recentNotifications: "Notifications",
      robotManagement: "Robot Management",
      robotTaskBoard: "Robot Task Board",
      role: "Role",
      ruleManagement: "Rule Management",
      savedNote: "Saved for this browser/device.",
      selectedStrategy: "Selected Strategy",
      settings: "Settings",
      settingsSaved: "Settings saved.",
      settingsSubtitle: "User-facing preferences for this browser.",
      settingsTitle: "Settings",
      simulation: "Rule Execution Simulator",
      staff: "Staff",
      startExecution: "Start Execution",
      stop: "Stop",
      stopMission: "Stop Mission",
      strategy: "Strategy",
      systemFlow: "System Flow",
      technicalDetails: "Technical details",
      theme: "Theme",
      user: "User",
      userMenu: "User menu",
      userManagement: "User Management",
      unused: "Unused",
      used: "Used",
      usedAt: "Used At",
      usedBy: "Used By",
      vietnamese: "Vietnamese",
      returnedWaiting: "Returned to Base. Waiting for confirmation.",
      robotStillWorking: "Robot is still working.",
      waitingConfirmation: "Waiting Confirmation"
    },
    vi: {
      accessDenied: "Từ chối truy cập",
      activeRules: "Luật đang hoạt động",
      activeWaiting: "Đang hoạt động / Đang chờ",
      allMissions: "Tất cả nhiệm vụ chưa xóa",
      admin: "Quản trị viên",
      applySettings: "Áp dụng",
      assignedRobot: "Robot được phân công",
      cancellationNote: "Ghi chú hủy",
      cancellationReason: "Lý do hủy",
      cancelledAt: "Thời gian hủy",
      cancelledBy: "Người hủy",
      cancelledStopped: "Đã hủy / Đã dừng",
      changePassword: "Đổi mật khẩu",
      codeStatus: "Trạng thái mã",
      clearNotifications: "Xóa tất cả",
      comfortable: "Thoải mái",
      compact: "Gọn",
      complete: "Hoàn tất",
      completed: "Đã hoàn tất",
      completedHistory: "Lịch sử hoàn tất",
      confirmPassword: "Xác nhận mật khẩu",
      createPickupCode: "Tạo mã nhận hàng",
      customerEmail: "Email khách hàng",
      customerName: "Tên khách hàng",
      customerPhone: "Số điện thoại khách hàng",
      customerPickupCode: "Mã nhận hàng khách hàng",
      customerPickupCodes: "Mã nhận hàng khách hàng",
      dashboard: "Tổng quan",
      decision: "Quyết định",
      delete: "Xóa",
      density: "Mật độ giao diện",
      details: "Chi tiết",
      emailPreview: "Xem trước email",
      english: "Tiếng Anh",
      execution: "Thực thi",
      language: "Ngôn ngữ",
      lifecycle: "Vòng đời",
      lightMode: "Chế độ sáng",
      liveMap: "Bản đồ trực tiếp",
      liveWarehouseMap: "Bản đồ trực tiếp",
      logout: "Đăng xuất",
      manager: "Quản lý",
      matchedRule: "Luật phù hợp",
      missionReview: "Theo dõi nhiệm vụ",
      missionStatus: "Trạng thái nhiệm vụ",
      myMissions: "Nhiệm vụ của nhân viên",
      newPickupRequest: "Yêu cầu lấy hàng mới",
      newPassword: "Mật khẩu mới",
      noNotifications: "Không có thông báo",
      notifications: "Thông báo",
      pickupCode: "Mã nhận hàng",
      pickupRequest: "Yêu cầu lấy hàng",
      policyAssignment: "Phân công chính sách",
      process: "Xử lý",
      processPickupCode: "Xử lý mã nhận hàng",
      recentNotifications: "Thông báo",
      robotManagement: "Quản lý robot",
      robotTaskBoard: "Bảng nhiệm vụ robot",
      role: "Vai trò",
      ruleManagement: "Quản lý luật",
      savedNote: "Đã lưu cho trình duyệt/thiết bị này.",
      selectedStrategy: "Chiến lược đã chọn",
      settings: "Cài đặt",
      settingsSaved: "Đã lưu cài đặt.",
      settingsSubtitle: "Tùy chọn hiển thị cho trình duyệt này.",
      settingsTitle: "Cài đặt",
      simulation: "Mô phỏng thực thi luật",
      staff: "Nhân viên",
      startExecution: "Bắt đầu thực thi",
      stop: "Dừng",
      stopMission: "Dừng nhiệm vụ",
      strategy: "Chiến lược",
      systemFlow: "Luồng hệ thống",
      technicalDetails: "Chi tiết kỹ thuật",
      theme: "Giao diện",
      user: "Người dùng",
      userMenu: "Menu người dùng",
      userManagement: "Quản lý tài khoản",
      unused: "Chưa sử dụng",
      used: "Đã sử dụng",
      usedAt: "Thời gian sử dụng",
      usedBy: "Người sử dụng",
      vietnamese: "Tiếng Việt",
      returnedWaiting: "Robot đã về trạm. Chờ xác nhận hoàn tất.",
      robotStillWorking: "Robot đang thực hiện nhiệm vụ.",
      waitingConfirmation: "Chờ xác nhận"
    }
  };

  var strategyTranslations = {
    FastRouteStrategy: "Di chuyển nhanh",
    EnergySavingStrategy: "Tiết kiệm năng lượng",
    HeavyLoadStrategy: "Tải nặng",
    ObstacleAvoidanceStrategy: "Tránh vật cản",
    ChargingStrategy: "Sạc pin",
    SafeRouteStrategy: "Di chuyển an toàn",
    DefaultStrategy: "Mặc định",
    NormalStrategy: "Bình thường",
    NoStrategy: "Không có chiến lược",
    Fast: "Di chuyển nhanh",
    "Energy Saving": "Tiết kiệm năng lượng",
    "Heavy Load": "Tải nặng",
    "Obstacle Avoidance": "Tránh vật cản",
    Charging: "Đang sạc",
    "Safe Route": "Di chuyển an toàn",
    Normal: "Bình thường",
    Idle: "Rảnh"
  };

  var exactTranslations = {
    vi: {
      "Access Denied | Smart Warehouse": "Từ chối truy cập | Smart Warehouse",
      "Access denied": "Từ chối truy cập",
      "Action": "Thao tác",
      "Action Summary": "Tóm tắt thao tác",
      "Action summary": "Tóm tắt thao tác",
      "Actions": "Thao tác",
      "Active": "Đang hoạt động",
      "Active / Waiting": "Đang hoạt động / Đang chờ",
      "Active Admin Rules Available as Policies": "Luật Admin đang hoạt động có thể dùng làm chính sách",
      "Active Missions": "Nhiệm vụ đang hoạt động",
      "Active Rules": "Luật đang hoạt động",
      "Active Status": "Trạng thái hoạt động",
      "Active mission count": "Số nhiệm vụ đang hoạt động",
      "Active Strategies": "Chiến lược đang hoạt động",
      "Active rules are evaluated by priority. The selected rule supplies the strategy.": "Các luật đang hoạt động được đánh giá theo độ ưu tiên. Luật được chọn cung cấp chiến lược.",
      "Active workload includes assigned PENDING, ASSIGNED, and IN_PROGRESS missions. WAITING_CONFIRMATION missions are counted separately because the robot can accept new work after returning to Base. COMPLETED, CANCELLED, and deleted missions do not count as active workload.": "Khối lượng công việc đang hoạt động gồm các nhiệm vụ PENDING, ASSIGNED và IN_PROGRESS đã phân công. WAITING_CONFIRMATION được tính riêng vì robot có thể nhận việc mới sau khi về trạm. COMPLETED, CANCELLED và nhiệm vụ đã xóa không được tính là đang hoạt động.",
      "Active workload includes assigned PENDING, ASSIGNED, and IN_PROGRESS missions. COMPLETED, CANCELLED, and deleted missions do not count.": "Khối lượng công việc đang hoạt động gồm các nhiệm vụ PENDING, ASSIGNED và IN_PROGRESS đã phân công. COMPLETED, CANCELLED và nhiệm vụ đã xóa không được tính.",
      "All non-deleted missions": "Tất cả nhiệm vụ chưa xóa",
      "Admin": "Quản trị viên",
      "ADMIN": "Quản trị viên",
      "Admin Rules": "Luật của Admin",
      "Admin creates rules. Manager assigns active policies to Zone A, Zone B, and Zone C for daily mission processing.": "Admin tạo luật. Quản lý phân công chính sách đang hoạt động cho Zone A, Zone B và Zone C để xử lý nhiệm vụ hằng ngày.",
      "Apply": "Áp dụng",
      "Apply Settings": "Áp dụng",
      "Architecture": "Kiến trúc",
      "All Robots": "Tất cả robot",
      "Assign active rule": "Phân công luật đang hoạt động",
      "Assigned": "Đã phân công",
      "ASSIGNED": "Đã phân công",
      "Assigned Robot": "Robot được phân công",
      "Assigned rule is required.": "Cần chọn luật để phân công.",
      "Assignment Information": "Thông tin phân công",
      "Assignment Reason": "Lý do phân công",
      "Assignment reason": "Lý do phân công",
      "Assignment error.": "Lỗi phân công.",
      "Assign existing active Admin-created rules as operational policies for warehouse zones.": "Phân công các luật đang hoạt động do Admin tạo làm chính sách vận hành cho các khu vực kho.",
      "Available": "Khả dụng",
      "AVAILABLE": "Khả dụng",
      "Back to Missions": "Quay lại nhiệm vụ",
      "Base Station": "Trạm chính",
      "Battery": "Pin",
      "Battery Level (%)": "Mức pin (%)",
      "Battery level": "Mức pin",
      "Battery status message.": "Thông báo trạng thái pin.",
      "Battery unavailable": "Không có dữ liệu pin",
      "Cancel": "Hủy",
      "Cancelled": "Đã hủy",
      "CANCELLED": "Đã hủy",
      "Cancelled / Stopped": "Đã hủy / Đã dừng",
      "Cancelled at": "Thời gian hủy",
      "Cancelled by": "Người hủy",
      "Cancelled missions cannot be completed.": "Nhiệm vụ đã hủy không thể hoàn tất.",
      "Cancelled / Stopped Missions": "Nhiệm vụ đã hủy / đã dừng",
      "Cancellation note": "Ghi chú hủy",
      "Cancellation reason": "Lý do hủy",
      "Cargo": "Hàng hóa",
      "Cargo / Location": "Hàng hóa / Vị trí",
      "Cargo Information": "Thông tin hàng hóa",
      "Cargo Location": "Vị trí hàng",
      "Cargo Type": "Loại hàng",
      "Cargo location is required.": "Cần chọn vị trí hàng.",
      "Cargo location must match the assigned zone.": "Vị trí hàng phải khớp với khu vực đã phân công.",
      "Cargo type must be Small Cargo, Medium Cargo, or Large Cargo.": "Loại hàng phải là Hàng nhỏ, Hàng vừa hoặc Hàng lớn.",
      "Cargo type is required.": "Vui lòng chọn loại hàng.",
      "Cargo location": "Vị trí hàng",
      "Cargo type": "Loại hàng",
      "Cargo type to warehouse zone": "Loại hàng theo khu vực kho",
      "Change policy": "Đổi chính sách",
      "Change Password": "Đổi mật khẩu",
      "Charging": "Đang sạc",
      "CHARGING": "Đang sạc",
      "Charging Strategy": "Chiến lược sạc pin",
      "Charging Station": "Trạm sạc",
      "Charging at Charging Station.": "Robot đang sạc tại trạm.",
      "Charging at station.": "Robot đang sạc tại trạm.",
      "Charging Required": "Cần sạc",
      "Charging required after current mission.": "Robot cần sạc sau nhiệm vụ này.",
      "Charging required after this task.": "Robot cần sạc sau nhiệm vụ này.",
      "Clear": "Xóa",
      "Clear all": "Xóa tất cả",
      "Clear path": "Đường trống",
      "Code Status": "Trạng thái mã",
      "Collapsed": "Đã thu gọn",
      "Comfortable": "Thoải mái",
      "Compact": "Gọn",
      "Complete": "Hoàn tất",
      "Completed": "Đã hoàn tất",
      "COMPLETED": "Đã hoàn tất",
      "Completed history": "Lịch sử hoàn tất",
      "Condition": "Điều kiện",
      "Condition Evaluation Details": "Chi tiết đánh giá điều kiện",
      "Condition Expression": "Biểu thức điều kiện",
      "Condition expression is required.": "Cần nhập biểu thức điều kiện.",
      "Confirm Password": "Xác nhận mật khẩu",
      "Create": "Tạo mới",
      "Create Pickup Code": "Tạo mã nhận hàng",
      "Create Pickup Request": "Tạo yêu cầu lấy hàng",
      "Created At": "Thời gian tạo",
      "Create a pickup request, choose the cargo location, and send it to the mission queue.": "Tạo yêu cầu lấy hàng, chọn vị trí hàng và gửi vào hàng đợi nhiệm vụ.",
      "Create customer pickup codes and preview the demo email sent to the customer.": "Tạo mã nhận hàng cho khách hàng và xem trước email demo gửi cho khách.",
      "Create and maintain rules that connect warehouse conditions to robot strategies.": "Tạo và duy trì các luật liên kết điều kiện kho với chiến lược robot.",
      "Created": "Đã tạo",
      "Current Active Strategy": "Chiến lược đang hoạt động",
      "Current Mission": "Nhiệm vụ hiện tại",
      "Current Policy": "Chính sách hiện tại",
      "Current Position": "Vị trí hiện tại",
      "Current Status": "Trạng thái hiện tại",
      "Current Strategy": "Chiến lược hiện tại",
      "Current Target": "Mục tiêu hiện tại",
      "Critical Battery": "Pin nguy cấp",
      "Critical Battery Rule": "Luật pin nguy cấp",
      "Critical Battery With Obstacle": "Pin nguy cấp kèm vật cản",
      "Critical Battery With Obstacle Rule": "Luật pin nguy cấp kèm vật cản",
      "Current page": "Trang hiện tại",
      "Customer / Request Identifier": "Khách hàng / Mã yêu cầu",
      "Customer A": "Khách hàng A",
      "Customer B": "Khách hàng B",
      "Customer changed request": "Khách đổi yêu cầu",
      "Customer Email": "Email khách hàng",
      "Customer Name": "Tên khách hàng",
      "Customer Name / Code": "Tên / mã khách hàng",
      "Customer Phone": "Số điện thoại khách hàng",
      "Customer Pickup Code": "Mã nhận hàng khách hàng",
      "Customer Pickup Codes": "Mã nhận hàng khách hàng",
      "Customer name is required.": "Vui lòng nhập tên khách hàng.",
      "Customer name / code": "Tên / mã khách hàng",
      "Customer not provided": "Chưa có thông tin khách hàng",
      "Dashboard": "Tổng quan",
      "Dark mode": "Chế độ tối",
      "Decision": "Quyết định",
      "Decision Control System": "Hệ thống điều phối quyết định",
      "Decision Engine": "Bộ máy quyết định",
      "Decision Summary": "Tóm tắt quyết định",
      "Decision output not stored yet.": "Chưa lưu kết quả quyết định.",
      "Decision ready": "Quyết định đã sẵn sàng",
      "Decision summary": "Tóm tắt quyết định",
      "Demo users": "Tài khoản demo",
      "Demo-only in-memory users. Do not use these passwords in production.": "Tài khoản demo chỉ lưu trong bộ nhớ. Không dùng các mật khẩu này trong môi trường production.",
      "Default Strategy": "Chiến lược mặc định",
      "Define New Rule": "Định nghĩa luật mới",
      "Delete": "Xóa",
      "Detected": "Phát hiện",
      "Details": "Chi tiết",
      "Disabled": "Đã tắt",
      "Dispatched by StrategyContext from the matched rule.": "Được Bộ điều phối chiến lược (StrategyContext) điều phối từ luật phù hợp.",
      "Distance": "Khoảng cách",
      "Distance (m)": "Khoảng cách (m)",
      "Duplicate request": "Trùng yêu cầu",
      "Edit": "Sửa",
      "Edit Rule": "Sửa luật",
      "Email Preview": "Xem trước email",
      "Enable or disable": "Bật hoặc tắt",
      "Enabled": "Đã bật",
      "Energy recovery": "Phục hồi năng lượng",
      "Energy saving mode active.": "Chế độ tiết kiệm năng lượng đang hoạt động.",
      "Energy Saving": "Tiết kiệm năng lượng",
      "Energy Saving Strategy": "Chiến lược tiết kiệm năng lượng",
      "Engine": "Bộ máy",
      "English": "Tiếng Anh",
      "Enter a request code, customer name, or both. A request code is generated if only a customer name is provided.": "Nhập mã yêu cầu, tên khách hàng hoặc cả hai. Hệ thống sẽ tạo mã yêu cầu nếu chỉ nhập tên khách hàng.",
      "Enter robot conditions and run the engine to show the decision trace.": "Nhập trạng thái robot và chạy bộ máy để xem vết quyết định.",
      "Evaluation Result": "Kết quả đánh giá",
      "Evaluation Trace": "Vết đánh giá",
      "Estimated cargo load": "Mức tải ước tính",
      "Executing": "Đang thực thi",
      "Execution": "Thực thi",
      "Execution Flow": "Luồng thực thi",
      "Execution History": "Lịch sử thực thi",
      "Execution Simulator": "Mô phỏng thực thi",
      "Execution Step": "Bước thực thi",
      "Execution Started": "Đã bắt đầu thực thi",
      "Existing active rules": "Các luật đang hoạt động",
      "Example:": "Ví dụ:",
      "FALSE": "SAI",
      "Fast Mode": "Chế độ di chuyển nhanh",
      "Fast Route Strategy": "Chiến lược di chuyển nhanh",
      "Filter": "Bộ lọc",
      "Final Action Message": "Thông báo thao tác cuối",
      "Final Decision Summary": "Tóm tắt quyết định cuối",
      "Final Robot Action": "Thao tác cuối của robot",
      "First workflow screen": "Màn hình đầu của quy trình",
      "Fleet Snapshot": "Tổng quan đội robot",
      "Flow Overview": "Tổng quan luồng",
      "Following Backend State": "Đang theo trạng thái backend",
      "Fullscreen live warehouse map demo": "Bản demo bản đồ kho trực tiếp toàn màn hình",
      "Graduation demo users": "Tài khoản demo tốt nghiệp",
      "Group": "Nhóm",
      "Heavy load mode active.": "Chế độ tải nặng đang hoạt động.",
      "Heavy Load Rule": "Luật tải nặng",
      "Heavy Load Strategy": "Chiến lược tải nặng",
      "High": "Cao",
      "High Priority": "Ưu tiên cao",
      "High Priority Active": "Đang hoạt động ưu tiên cao",
      "High-priority active": "Đang hoạt động ưu tiên cao",
      "History": "Lịch sử",
      "IDLE": "Rảnh",
      "IN_PROGRESS": "Đang thực hiện",
      "Input": "Đầu vào",
      "Input State": "Trạng thái đầu vào",
      "Input Summary": "Tóm tắt đầu vào",
      "Interpreter": "Interpreter",
      "Invalid customer email.": "Email khách hàng không hợp lệ.",
      "Invalid customer phone number.": "Số điện thoại khách hàng không hợp lệ.",
      "Invalid username or password.": "Tên đăng nhập hoặc mật khẩu không đúng.",
      "Language": "Ngôn ngữ",
      "Large Cargo": "Hàng lớn",
      "LARGE CARGO": "Hàng lớn",
      "Lifecycle": "Vòng đời",
      "Light mode": "Chế độ sáng",
      "Live Map": "Bản đồ trực tiếp",
      "Live Map is following backend execution state.": "Bản đồ trực tiếp đang theo trạng thái thực thi từ backend.",
      "Live Warehouse Map": "Bản đồ trực tiếp",
      "Live Warehouse Map visual board": "Bảng trực quan bản đồ trực tiếp",
      "Location": "Vị trí",
      "Location Code": "Mã vị trí",
      "Login": "Đăng nhập",
      "Login | Smart Warehouse": "Đăng nhập | Smart Warehouse",
      "Login form": "Biểu mẫu đăng nhập",
      "Logout": "Đăng xuất",
      "LOADED": "Đang chở hàng",
      "Long Distance Safe Route Rule": "Luật tuyến an toàn cho quãng đường dài",
      "Low": "Thấp",
      "Low Battery": "Pin thấp",
      "Low Battery Rule": "Luật pin thấp",
      "Manage": "Quản lý",
      "MANAGE": "Quản lý",
      "Manager": "Quản lý",
      "Manager Policy": "Chính sách quản lý",
      "Manager Policy Assignment": "Phân công chính sách của quản lý",
      "Manager Robot Task Board": "Bảng nhiệm vụ robot của quản lý",
      "Manager Rule / Policy Assignment": "Phân công luật / chính sách của quản lý",
      "Manager Workflow": "Quy trình quản lý",
      "Map Legend": "Chú giải bản đồ",
      "Map controls and legend": "Điều khiển và chú giải bản đồ",
      "Map display mode": "Chế độ hiển thị bản đồ",
      "Matched": "Phù hợp",
      "MATCHED": "PHÙ HỢP",
      "Matched Rule": "Luật phù hợp",
      "Matched rules dispatch strategies, then action history is saved.": "Luật phù hợp sẽ điều phối chiến lược, sau đó lịch sử thao tác được lưu lại.",
      "Medium": "Trung bình",
      "Medium Cargo": "Hàng vừa",
      "MEDIUM CARGO": "Hàng vừa",
      "Mission": "Nhiệm vụ",
      "Mission Detail": "Chi tiết nhiệm vụ",
      "Mission Flow": "Luồng nhiệm vụ",
      "Mission Processing": "Xử lý nhiệm vụ",
      "Mission review": "Theo dõi nhiệm vụ",
      "Mission Status": "Trạng thái nhiệm vụ",
      "Mission assigned. Waiting for Staff to start execution.": "Nhiệm vụ đã được phân công. Đang chờ nhân viên bắt đầu thực thi.",
      "Mission can only be completed after the robot returns to Base Station.": "Chỉ có thể hoàn tất nhiệm vụ sau khi robot đã về trạm.",
      "Mission is already completed.": "Nhiệm vụ đã được hoàn tất.",
      "Mission completed.": "Nhiệm vụ đã hoàn tất.",
      "Mission could not be processed.": "Không thể xử lý nhiệm vụ.",
      "Mission detail": "Chi tiết nhiệm vụ",
      "Mission has not been assigned.": "Nhiệm vụ chưa được phân công.",
      "Mission is required to calculate a route.": "Cần có nhiệm vụ để tính tuyến đường.",
      "Mission locationCode is required to calculate a route.": "Cần mã vị trí nhiệm vụ để tính tuyến đường.",
      "Mission locationCode must be A1-A9, B1-B9, or C1-C9.": "Mã vị trí nhiệm vụ phải thuộc A1-A9, B1-B9 hoặc C1-C9.",
      "Mission not found.": "Không tìm thấy nhiệm vụ.",
      "Mission processed.": "Nhiệm vụ đã được xử lý.",
      "Mission must have an assigned robot before execution can start.": "Nhiệm vụ phải có robot được phân công trước khi bắt đầu thực thi.",
      "Mission stopped.": "Nhiệm vụ đã dừng.",
      "Mission zone must be Zone A, Zone B, or Zone C.": "Khu vực nhiệm vụ phải là Zone A, Zone B hoặc Zone C.",
      "Mission zone must match the target locationCode.": "Khu vực nhiệm vụ phải khớp với mã vị trí đích.",
      "Movement Mode": "Chế độ di chuyển",
      "Monitor fleet health, active missions, rules, and recent engine decisions.": "Theo dõi sức khỏe đội robot, nhiệm vụ đang hoạt động, luật và các quyết định gần đây của bộ máy.",
      "Monitor robot availability, battery, charging state, and current strategy.": "Theo dõi khả năng sẵn sàng, pin, trạng thái sạc và chiến lược hiện tại của robot.",
      "Monitor robot workload, priority pressure, and charging availability.": "Theo dõi khối lượng công việc, áp lực ưu tiên và khả năng sạc của robot.",
      "Moving": "Đang di chuyển",
      "MOVING": "Đang di chuyển",
      "MOVING_TO_TARGET": "Đang di chuyển đến vị trí hàng",
      "Moving to target location.": "Đang di chuyển đến vị trí hàng.",
      "My Missions": "Nhiệm vụ của tôi",
      "N/A": "Không có",
      "NO MATCH": "KHÔNG PHÙ HỢP",
      "NOT_STARTED": "Chưa bắt đầu",
      "New Pickup Request": "Yêu cầu lấy hàng mới",
      "New Password": "Mật khẩu mới",
      "Next Action": "Thao tác tiếp theo",
      "Next Zone": "Zone tiếp theo",
      "No": "Không",
      "No Strategy": "Không có chiến lược",
      "No active strategy": "Không có chiến lược đang hoạt động",
      "No active mission.": "Không có nhiệm vụ đang hoạt động.",
      "No active missions assigned to this robot.": "Robot này chưa được phân công nhiệm vụ đang hoạt động.",
      "No active pickup mission assigned.": "Không có nhiệm vụ lấy hàng đang hoạt động.",
      "No cancellation note provided.": "Không có ghi chú hủy.",
      "No cancelled or stopped missions are recorded.": "Chưa ghi nhận nhiệm vụ đã hủy hoặc đã dừng.",
      "No active rules are available.": "Không có luật đang hoạt động.",
      "No active rules are available. Enable at least one rule in Admin Rule Management before assigning zone policies.": "Không có luật đang hoạt động. Hãy bật ít nhất một luật trong Quản lý luật của Admin trước khi phân công chính sách khu vực.",
      "No active rules were evaluated.": "Không có luật đang hoạt động nào được đánh giá.",
      "No active strategies are available.": "Không có chiến lược đang hoạt động.",
      "No assigned tasks": "Không có nhiệm vụ được phân công",
      "No backend position recorded": "Chưa có vị trí từ backend",
      "No condition details are available because no active rules were evaluated.": "Không có chi tiết điều kiện vì chưa đánh giá luật đang hoạt động nào.",
      "No decision summary stored.": "Chưa lưu tóm tắt quyết định.",
      "No notifications": "Không có thông báo",
      "No notifications yet.": "Không có thông báo",
      "No notes provided.": "Chưa có ghi chú.",
      "No rules have been created yet. Use the form to add the first runtime rule.": "Chưa có luật nào được tạo. Hãy dùng biểu mẫu để thêm luật runtime đầu tiên.",
      "No robot assigned yet.": "Chưa phân công robot.",
      "No robots found. Seed data may not be loaded yet.": "Không tìm thấy robot. Dữ liệu mẫu có thể chưa được tải.",
      "No robots found. Seed robot data before reviewing the task board.": "Không tìm thấy robot. Hãy nạp dữ liệu robot mẫu trước khi xem bảng nhiệm vụ.",
      "No robots have been registered yet.": "Chưa đăng ký robot nào.",
      "No selected robot.": "Chưa chọn robot.",
      "No simulation execution history has been saved yet.": "Chưa lưu lịch sử mô phỏng thực thi.",
      "No simulation executions have been saved yet.": "Chưa lưu lượt mô phỏng nào.",
      "No missions have been saved yet. Create a pickup request to add the first pending mission.": "Chưa có nhiệm vụ nào được lưu. Tạo yêu cầu lấy hàng để thêm nhiệm vụ đang chờ đầu tiên.",
      "No missions are currently in this group.": "Hiện không có nhiệm vụ nào trong nhóm này.",
      "No unassigned PENDING missions are waiting for robot assignment.": "Không có nhiệm vụ PENDING chưa phân công đang chờ robot.",
      "Normal": "Bình thường",
      "Not assigned": "Chưa phân công",
      "Not evaluated yet.": "Chưa đánh giá.",
      "Not processed": "Chưa xử lý",
      "Not provided": "Chưa cung cấp",
      "Not recorded": "Chưa ghi nhận",
      "Not reached": "Chưa đến",
      "Not returned": "Chưa về trạm",
      "Not selected yet.": "Chưa chọn.",
      "Not started": "Chưa bắt đầu",
      "Notes": "Ghi chú",
      "Notes / request detail": "Ghi chú / chi tiết yêu cầu",
      "None": "Không có",
      "Notification": "Thông báo",
      "Notifications": "Thông báo",
      "Obstacle": "Vật cản",
      "Obstacle Detected": "Phát hiện vật cản",
      "Obstacle Avoidance Strategy": "Chiến lược tránh vật cản",
      "Obstacle Detection Rule": "Luật phát hiện vật cản",
      "Other": "Khác",
      "Open My Missions to process this request and assign a robot.": "Mở Nhiệm vụ của tôi để xử lý yêu cầu này và phân công robot.",
      "Open Simulator": "Mở mô phỏng",
      "Only active Admin-created rules are available for assignment.": "Chỉ các luật do Admin tạo và đang hoạt động mới có thể phân công.",
      "Only ASSIGNED missions can start execution.": "Chỉ nhiệm vụ đã phân công mới có thể bắt đầu thực thi.",
      "Only CANCELLED missions can be deleted.": "Chỉ nhiệm vụ đã hủy mới có thể xóa.",
      "Only IN_PROGRESS missions can be marked completed after returning to Base Station.": "Chỉ nhiệm vụ đang thực hiện mới có thể hoàn tất sau khi robot đã về Trạm chính.",
      "Only missions waiting for confirmation can be marked completed.": "Chỉ nhiệm vụ đang chờ xác nhận mới có thể đánh dấu hoàn tất.",
      "Only missions with NOT_STARTED execution can start execution.": "Chỉ nhiệm vụ chưa bắt đầu thực thi mới có thể bắt đầu.",
      "Only PENDING missions can be processed.": "Chỉ nhiệm vụ đang chờ mới có thể xử lý.",
      "Only PENDING, ASSIGNED, or IN_PROGRESS missions can be stopped.": "Chỉ nhiệm vụ đang chờ, đã phân công hoặc đang thực hiện mới có thể dừng.",
      "Only PENDING, ASSIGNED, IN_PROGRESS, or WAITING_CONFIRMATION missions can be stopped.": "Chỉ nhiệm vụ đang chờ, đã phân công, đang thực hiện hoặc chờ xác nhận mới có thể dừng.",
      "Operators:": "Toán tử:",
      "Optional handling note or customer detail": "Ghi chú xử lý hoặc chi tiết khách hàng nếu có",
      "Package not found": "Không tìm thấy hàng",
      "Overview": "Tổng quan",
      "PENDING": "Đang chờ",
      "PENDING Mission": "Nhiệm vụ đang chờ",
      "PICKING_UP": "Đang lấy hàng",
      "Password": "Mật khẩu",
      "Password confirmation does not match.": "Xác nhận mật khẩu không khớp.",
      "Password is required.": "Vui lòng nhập mật khẩu.",
      "Password must be at least 8 characters.": "Mật khẩu phải có ít nhất 8 ký tự.",
      "Password must be 100 characters or fewer.": "Mật khẩu không được vượt quá 100 ký tự.",
      "Password updated successfully.": "Cập nhật mật khẩu thành công.",
      "Pending": "Đang chờ",
      "Pickup": "Lấy hàng",
      "Pickup Code": "Mã nhận hàng",
      "Pickup code created. Email preview is ready.": "Đã tạo mã nhận hàng. Email xem trước đã sẵn sàng.",
      "Pickup code is required.": "Vui lòng nhập mã nhận hàng.",
      "Pickup code not found.": "Không tìm thấy mã nhận hàng.",
      "Picking up cargo.": "Đang lấy hàng.",
      "Pickup Reached": "Đã đến điểm lấy hàng",
      "Pickup Request": "Yêu cầu lấy hàng",
      "Pickup Request Form": "Biểu mẫu yêu cầu lấy hàng",
      "Pickup location": "Vị trí lấy hàng",
      "Pickup location is required.": "Vui lòng chọn vị trí lấy hàng.",
      "Pickup request saved.": "Đã lưu yêu cầu lấy hàng.",
      "Please fix the request details.": "Vui lòng kiểm tra lại chi tiết yêu cầu.",
      "Please select a cancellation reason.": "Vui lòng chọn lý do hủy nhiệm vụ.",
      "Policy Assignment": "Phân công chính sách",
      "Policy Flow": "Luồng chính sách",
      "Power Level": "Mức pin",
      "Primary": "Chính",
      "Primary Strategy": "Chiến lược chính",
      "Priority": "Ưu tiên",
      "Priority: High": "Độ ưu tiên: Cao",
      "Priority Label": "Nhãn ưu tiên",
      "Priority must be 1 = High, 2 = Medium, or 3 = Low.": "Ưu tiên phải là 1 = Cao, 2 = Trung bình hoặc 3 = Thấp.",
      "Priority must be 1 or greater.": "Ưu tiên phải từ 1 trở lên.",
      "Process": "Xử lý",
      "Process Mission / Assign Robot": "Xử lý nhiệm vụ / Phân công robot",
      "Process Pickup Code": "Xử lý mã nhận hàng",
      "Process pickup requests, start assigned work, and close missions after return.": "Xử lý yêu cầu lấy hàng, bắt đầu công việc đã phân công và đóng nhiệm vụ sau khi robot quay về.",
      "Processed": "Đã xử lý",
      "Profile": "Hồ sơ",
      "Preview only. Backend state is unchanged.": "Chỉ xem trước. Trạng thái backend không thay đổi.",
      "Preview only: returning to Base.": "Chỉ xem trước: đang quay về Trạm chính.",
      "Read-only mission status, assignment, and rule decision result.": "Xem trạng thái nhiệm vụ, phân công và kết quả quyết định luật ở chế độ chỉ đọc.",
      "Reads Conditions": "Đọc điều kiện",
      "Ready to process": "Sẵn sàng xử lý",
      "Recent Activity": "Hoạt động gần đây",
      "Recent Execution History": "Lịch sử thực thi gần đây",
      "Recent History": "Lịch sử gần đây",
      "Refresh": "Làm mới",
      "Remaining tasks were reassigned.": "Các nhiệm vụ còn lại đã được phân công lại.",
      "Request": "Yêu cầu",
      "Request Code": "Mã yêu cầu",
      "Request Information": "Thông tin yêu cầu",
      "Request code": "Mã yêu cầu",
      "Request code or customer name is required.": "Cần nhập mã yêu cầu hoặc tên khách hàng.",
      "Request flow": "Luồng yêu cầu",
      "Reset": "Đặt lại",
      "Result": "Kết quả",
      "Returned": "Đã về trạm",
      "RETURNED_TO_BASE": "Đã về trạm",
      "Returned / Waiting Confirmation": "Đã về trạm / Chờ xác nhận",
      "Returned To Base": "Đã về trạm",
      "Returned to Base. Waiting for confirmation.": "Robot đã về trạm. Chờ xác nhận hoàn tất.",
      "Returning": "Đang quay về",
      "RETURNING_TO_BASE": "Đang quay về",
      "Returning to Base Station.": "Đang quay về Trạm chính.",
      "Robot": "Robot",
      "Robot Action": "Thao tác robot",
      "Robot Assignment": "Phân công robot",
      "Robot Battery": "Pin robot",
      "Robot Condition Input": "Đầu vào điều kiện robot",
      "Robot Fleet": "Đội robot",
      "Robot Input": "Đầu vào robot",
      "Robot Load": "Tải robot",
      "Robot Load (%)": "Tải robot (%)",
      "robotLoad means estimated cargo load percentage.": "robotLoad là mức tải ước tính của hàng hóa.",
      "robotLoad means estimated cargo load percentage. Small Cargo = 30%, Medium Cargo = 60%, Large Cargo = 90%.": "robotLoad là mức tải ước tính của hàng hóa. Hàng nhỏ = 30%, Hàng vừa = 60%, Hàng lớn = 90%.",
      "Robot Management": "Quản lý robot",
      "Robot Name": "Tên robot",
      "Robot action.": "Thao tác robot.",
      "Robot action summary": "Tóm tắt thao tác robot",
      "Robot battery is low.": "Pin robot đang thấp.",
      "Robot currently viewing Zone A": "Robot đang xem Zone A",
      "Robot currently viewing Zone B": "Robot đang xem Zone B",
      "Robot currently viewing Zone C": "Robot đang xem Zone C",
      "Robot home dock": "Bến đỗ chính của robot",
      "Robot is charging at station.": "Robot đang sạc tại trạm.",
      "Robot issue": "Robot gặp sự cố",
      "Robot is still working.": "Robot đang thực hiện nhiệm vụ.",
      "Robot is waiting for path to clear.": "Robot đang chờ đường di chuyển thông thoáng.",
      "Robot returned to Base. Waiting for confirmation.": "Robot đã về trạm. Chờ xác nhận hoàn tất.",
      "Robot status": "Trạng thái robot",
      "Robot workload": "Khối lượng công việc robot",
      "Robots": "Robot",
      "Robots on Board": "Robot trên bảng",
      "Robot Task Board": "Bảng nhiệm vụ robot",
      "Role": "Vai trò",
      "Role-based demo login": "Đăng nhập demo theo vai trò",
      "Route Network": "Mạng tuyến đường",
      "Route Planned": "Đã lập tuyến",
      "Route is required to calculate execution progress.": "Cần có tuyến đường để tính tiến độ thực thi.",
      "Route must include a PICKUP step.": "Tuyến đường phải có bước PICKUP.",
      "Route robots to charging.": "Đưa robot đến trạm sạc.",
      "Route the robot to a charging station when the battery is low.": "Đưa robot đến trạm sạc khi pin thấp.",
      "Route preview could not start because the selected robot marker is not visible.": "Không thể bắt đầu xem trước tuyến đường vì không thấy điểm đánh dấu robot đã chọn.",
      "Prefer safer routes when risk or priority requires caution.": "Ưu tiên tuyến an toàn hơn khi có rủi ro hoặc mức ưu tiên cần thận trọng.",
      "Prefer the shortest travel time for urgent tasks.": "Ưu tiên thời gian di chuyển ngắn nhất cho nhiệm vụ khẩn cấp.",
      "Recalculate movement when an obstacle is detected.": "Tính lại hướng di chuyển khi phát hiện vật cản.",
      "Reduce speed and choose safer paths when the robot carries a heavy load.": "Giảm tốc độ và chọn tuyến an toàn hơn khi robot chở hàng nặng.",
      "Reduce speed and conserve energy during normal operations.": "Giảm tốc độ và tiết kiệm năng lượng trong vận hành bình thường.",
      "Rule": "Luật",
      "Rule -> Strategy -> Action -> History": "Luật -> Chiến lược -> Thao tác -> Lịch sử",
      "Rule / Policy Assignment": "Phân công luật / chính sách",
      "Rule Execution Simulator": "Mô phỏng thực thi luật",
      "Rule List": "Danh sách luật",
      "Rule Management": "Quản lý luật",
      "Rule Match": "Luật phù hợp",
      "Rule Match Status": "Trạng thái khớp luật",
      "Rule Name": "Tên luật",
      "Rule name is required.": "Cần nhập tên luật.",
      "Rule and Strategy Result": "Kết quả luật và chiến lược",
      "Rule error.": "Lỗi luật.",
      "Rule saved.": "Đã lưu luật.",
      "Rules and Strategies": "Luật và chiến lược",
      "Run Engine": "Chạy bộ máy",
      "Run Simulator": "Chạy mô phỏng",
      "Save": "Lưu",
      "Safe Route Strategy": "Chiến lược di chuyển an toàn",
      "Save Pending Mission": "Lưu nhiệm vụ đang chờ",
      "Save Rule": "Lưu luật",
      "Save Zone Policy": "Lưu chính sách khu vực",
      "Saved Mission Summary": "Tóm tắt nhiệm vụ đã lưu",
      "Search": "Tìm kiếm",
      "Selected": "Đã chọn",
      "Selected Robot": "Robot đã chọn",
      "Selected Robot View": "Chế độ robot đã chọn",
      "Selected Strategy": "Chiến lược đã chọn",
      "Selected group": "Nhóm đã chọn",
      "Selected rule does not exist.": "Luật đã chọn không tồn tại.",
      "Selected rule must be active.": "Luật đã chọn phải đang hoạt động.",
      "Selected strategy is not active.": "Chiến lược đã chọn không hoạt động.",
      "Select Strategy...": "Chọn chiến lược...",
      "Select a cargo type first": "Chọn loại hàng trước",
      "Select a cargo type to display available storage locations.": "Chọn loại hàng để hiển thị vị trí lưu trữ còn trống.",
      "Select a robot to view status, battery, strategy, and mission flow.": "Chọn robot để xem trạng thái, pin, chiến lược và luồng nhiệm vụ.",
      "Select a robot with an active pickup mission before starting visual preview.": "Chọn robot có nhiệm vụ lấy hàng đang hoạt động trước khi xem trước tuyến đường.",
      "Select an active rule...": "Chọn luật đang hoạt động...",
      "Select cargo type...": "Chọn loại hàng...",
      "Select location": "Chọn vị trí",
      "Select one storage cell from the zone generated by the cargo type.": "Chọn một ô lưu trữ trong khu vực được tạo theo loại hàng.",
      "Selects Behavior": "Chọn hành vi",
      "Settings": "Cài đặt",
      "Settings applied.": "Đã áp dụng cài đặt.",
      "Settings are saved for this browser session/device.": "Cài đặt được lưu cho phiên/thiết bị trình duyệt này.",
      "Settings saved.": "Đã lưu cài đặt.",
      "Show All Robots": "Hiển thị tất cả robot",
      "Showing all robots across all zones": "Đang hiển thị tất cả robot trên mọi khu vực",
      "Sign in": "Đăng nhập",
      "Simulation": "Mô phỏng",
      "Simulation Parameters": "Tham số mô phỏng",
      "Simulation Robot": "Robot mô phỏng",
      "Simulate This Robot": "Mô phỏng robot này",
      "Simulate robot conditions to test rule evaluation and strategy execution.": "Mô phỏng trạng thái robot để kiểm thử đánh giá luật và thực thi chiến lược.",
      "SKIPPED": "BỎ QUA",
      "Small Cargo": "Hàng nhỏ",
      "SMALL CARGO": "Hàng nhỏ",
      "Staff": "Nhân viên",
      "STAFF": "Nhân viên",
      "Staff Home": "Trang nhân viên",
      "Staff Missions": "Nhiệm vụ của nhân viên",
      "Staff Note": "Ghi chú nhân viên",
      "Staff Pickup Request": "Yêu cầu lấy hàng của nhân viên",
      "Staff View": "Góc nhìn nhân viên",
      "Staff Workflow": "Quy trình nhân viên",
      "Start Execution": "Bắt đầu thực thi",
      "Status": "Trạng thái",
      "Step 3: Rule Matching": "Bước 3: Khớp luật",
      "Step 4: Strategy Pattern": "Bước 4: Strategy Pattern",
      "Step 5: Robot Action": "Bước 5: Thao tác robot",
      "Step 6: Execution History": "Bước 6: Lịch sử thực thi",
      "STATUS": "TRẠNG THÁI",
      "Stop": "Dừng",
      "Stopped": "Đã dừng",
      "STOPPED": "Đã dừng",
      "Stop Mission": "Dừng nhiệm vụ",
      "Strategy": "Chiến lược",
      "Strategy / Action": "Chiến lược / Thao tác",
      "Strategy Dispatch": "Điều phối chiến lược",
      "Strategy is required.": "Cần chọn chiến lược.",
      "StrategyContext dispatches the selected behavior at runtime.": "Bộ điều phối chiến lược (StrategyContext) điều phối hành vi đã chọn khi chạy.",
      "Submit": "Gửi",
      "Submit the form to save a PENDING mission. Use My Missions to process and assign it.": "Gửi biểu mẫu để lưu nhiệm vụ Đang chờ. Dùng Nhiệm vụ của tôi để xử lý và phân công.",
      "Supported syntax": "Cú pháp hỗ trợ",
      "Supported zone assignments": "Phân công khu vực hỗ trợ",
      "System Flow": "Luồng hệ thống",
      "System Flow Visualization": "Trực quan hóa luồng hệ thống",
      "Task Board": "Bảng nhiệm vụ",
      "Task Priority": "Ưu tiên nhiệm vụ",
      "Target Strategy": "Chiến lược mục tiêu",
      "Technical Catalog": "Danh mục kỹ thuật",
      "Technical details": "Chi tiết kỹ thuật",
      "Theme": "Giao diện",
      "The Logic": "Logic",
      "The selected strategy returns a human-readable action, such as routing the robot to a charging station or recalculating a safer path.": "Chiến lược đã chọn trả về thao tác dễ đọc, ví dụ đưa robot đến trạm sạc hoặc tính lại tuyến đường an toàn hơn.",
      "This pickup code has already been used.": "Mã nhận hàng này đã được sử dụng.",
      "This form captures cargo, priority, and location, then saves it as a PENDING mission. Cargo type automatically determines the warehouse zone.": "Biểu mẫu này ghi nhận hàng hóa, ưu tiên và vị trí, sau đó lưu thành nhiệm vụ Đang chờ. Loại hàng tự động xác định khu vực kho.",
      "This read-only board groups database-backed missions by assigned robot.": "Bảng chỉ đọc này nhóm nhiệm vụ trong cơ sở dữ liệu theo robot được phân công.",
      "Time": "Thời gian",
      "To Base Station": "Về Trạm chính",
      "Total Missions": "Tổng số nhiệm vụ",
      "TRUE": "ĐÚNG",
      "Track robot position, battery, strategy, and mission flow from the backend state.": "Theo dõi vị trí robot, pin, chiến lược và luồng nhiệm vụ từ trạng thái backend.",
      "UI Density": "Mật độ giao diện",
      "Unavailable": "Không khả dụng",
      "UNAVAILABLE": "Không khả dụng",
      "Unassigned Pending": "Đang chờ chưa phân công",
      "Unassigned Pending Missions": "Nhiệm vụ đang chờ chưa phân công",
      "Unknown": "Không rõ",
      "Unused": "Chưa sử dụng",
      "USER": "Người dùng",
      "Updated successfully.": "Đã cập nhật thành công.",
      "Urgent Task Fast Route Rule": "Luật nhiệm vụ khẩn cấp di chuyển nhanh",
      "Use Details for rule trace and decision output.": "Dùng Chi tiết để xem vết luật và kết quả quyết định.",
      "Use My Missions": "Dùng Nhiệm vụ của tôi",
      "Uses assigned policy": "Dùng chính sách đã phân công",
      "User": "Người dùng",
      "User-facing preferences for this browser.": "Tùy chọn hiển thị cho trình duyệt này.",
      "User menu": "Menu người dùng",
      "User Management": "Quản lý tài khoản",
      "Username": "Tên đăng nhập",
      "Updated At": "Thời gian cập nhật",
      "Used": "Đã sử dụng",
      "Used At": "Thời gian sử dụng",
      "Used By": "Người sử dụng",
      "Validation error": "Lỗi xác thực",
      "Vietnamese": "Tiếng Việt",
      "View Admin Rules": "Xem luật Admin",
      "View Missions": "Xem nhiệm vụ",
      "View My Missions": "Xem nhiệm vụ của tôi",
      "Visual Preview Running": "Đang xem trước tuyến đường",
      "Visual Route Preview": "Xem trước tuyến đường",
      "Visual route controls": "Điều khiển xem trước tuyến đường",
      "Waiting": "Đang chờ",
      "WAITING": "Đang chờ",
      "Waiting Confirmation": "Chờ xác nhận",
      "WAITING_CONFIRMATION": "Chờ xác nhận",
      "Waiting for confirmation": "Chờ xác nhận",
      "Waiting for mission processing.": "Đang chờ xử lý nhiệm vụ.",
      "Waiting for path to clear.": "Robot đang chờ đường di chuyển thông thoáng.",
      "Waiting for rule evaluation.": "Đang chờ đánh giá luật.",
      "Warehouse Decision Engine Dashboard": "Tổng quan bộ máy quyết định kho",
      "Warehouse Engine": "Warehouse Engine",
      "Warehouse board with active zone, routes, robots, and stations": "Bảng kho với khu vực đang hoạt động, tuyến đường, robot và trạm",
      "Warehouse Overview": "Tổng quan kho",
      "Warehouse Zone": "Khu vực kho",
      "Warehouse Zones": "Khu vực kho",
      "Warehouse zone": "Khu vực kho",
      "Warehouse zone must be Zone A, Zone B, or Zone C.": "Khu vực kho phải là Zone A, Zone B hoặc Zone C.",
      "Wrong cargo type": "Sai loại hàng",
      "Wrong location": "Sai vị trí",
      "Workload": "Khối lượng công việc",
      "Workload definition": "Định nghĩa khối lượng công việc",
      "Workflow position": "Vị trí trong quy trình",
      "Yes": "Có",
      "You have been logged out.": "Bạn đã đăng xuất.",
      "Your current role cannot open this page.": "Vai trò hiện tại không thể mở trang này.",
      "Zone": "Khu vực",
      "Zone A": "Zone A",
      "Zone B": "Zone B",
      "Zone C": "Zone C",
      "Zone C station area": "Khu trạm của Zone C",
      "Zone must be Zone A, Zone B, or Zone C.": "Khu vực phải là Zone A, Zone B hoặc Zone C.",
      "Zone policy": "Chính sách khu vực",
      "Zone policy assignment saved.": "Đã lưu phân công chính sách khu vực.",
      "Zone policies are database-backed assignments; rule evaluation and strategy classes stay unchanged.": "Chính sách khu vực là phân công lưu trong cơ sở dữ liệu; đánh giá luật và các lớp chiến lược không thay đổi.",
      "Zone-based policies": "Chính sách theo khu vực",
      "Zones": "Khu vực",
      "available": "khả dụng",
      "charging": "đang sạc",
      "current strategy": "chiến lược hiện tại",
      "unavailable": "không khả dụng"
    }
  };

  Object.keys(strategyTranslations).forEach(function (key) {
    exactTranslations.vi[key] = strategyTranslations[key];
  });

  function storageKey(name) {
    return storagePrefix + name;
  }

  function normalizeValue(name, value) {
    var allowed = allowedValues[name] || [];

    if (allowed.indexOf(value) >= 0) {
      return value;
    }

    return defaults[name];
  }

  function readSetting(name) {
    var value = null;

    try {
      value = window.localStorage.getItem(storageKey(name));
    } catch (ex) {
      value = null;
    }

    return normalizeValue(name, value || defaults[name]);
  }

  function writeSetting(name, value) {
    var normalizedValue = normalizeValue(name, value);

    try {
      window.localStorage.setItem(storageKey(name), normalizedValue);
    } catch (ex) {
      // Settings still apply for the current page even if storage is unavailable.
    }

    return normalizedValue;
  }

  function currentSettings() {
    return {
      language: readSetting("language"),
      theme: readSetting("theme"),
      density: readSetting("density")
    };
  }

  function applyClass(prefix, value, values) {
    values.forEach(function (candidate) {
      root.classList.remove(prefix + candidate);
    });
    root.classList.add(prefix + value);
  }

  function normalizeDisplaySource(value) {
    return value == null ? "" : String(value).trim().replace(/\s+/g, " ");
  }

  function preserveWhitespace(source, translated) {
    var prefix = String(source).match(/^\s*/)[0];
    var suffix = String(source).match(/\s*$/)[0];

    return prefix + translated + suffix;
  }

  function translateCargoTerm(value, language) {
    return translateText(value, language);
  }

  function translateStrategyName(value, language) {
    var source = normalizeDisplaySource(value);

    if (language !== "vi") {
      return source;
    }

    return strategyTranslations[source] || source;
  }

  function translatePriorityLabel(value, language) {
    var source = normalizeDisplaySource(value);
    var priorityMatch = source.match(/^(\d+)\s*=\s*(High|Medium|Low)$/);

    if (language !== "vi" || !priorityMatch) {
      return source;
    }

    return priorityMatch[1] + " = " + translateText(priorityMatch[2], language);
  }

  function translateStatusLabel(value, language) {
    return translateText(value, language);
  }

  function translatePattern(source, language) {
    var match;

    if (language !== "vi") {
      return source;
    }

    match = source.match(/^(\d+)\s*=\s*(High|Medium|Low)$/);
    if (match) {
      return match[1] + " = " + translateText(match[2], language);
    }

    match = source.match(/^(\d+)\s+Rules Stored$/);
    if (match) {
      return match[1] + " luật đã lưu";
    }

    match = source.match(/^(\d+)\s+Active Rules$/);
    if (match) {
      return match[1] + " luật đang hoạt động";
    }

    match = source.match(/^(\d+)\s+Active Missions$/);
    if (match) {
      return match[1] + " nhiệm vụ đang hoạt động";
    }

    match = source.match(/^(\d+)\s+High Priority$/);
    if (match) {
      return match[1] + " ưu tiên cao";
    }

    match = source.match(/^(\d+)\s+Pending$/);
    if (match) {
      return match[1] + " đang chờ";
    }

    match = source.match(/^(\d+)\s+pending confirmation$/);
    if (match) {
      return match[1] + " chờ xác nhận";
    }

    match = source.match(/^(\d+)\s+Cancelled$/);
    if (match) {
      return match[1] + " đã hủy";
    }

    match = source.match(/^(\d+)\s+mission\(s\), newest first$/);
    if (match) {
      return match[1] + " nhiệm vụ, mới nhất trước";
    }

    match = source.match(/^(\d+)\s+PKG$/);
    if (match) {
      return match[1] + " ô hàng";
    }

    match = source.match(/^Step\s+(\d+)$/);
    if (match) {
      return "Bước " + match[1];
    }

    match = source.match(/^Step\s+(\d+)-(\d+)$/);
    if (match) {
      return "Bước " + match[1] + "-" + match[2];
    }

    match = source.match(/^Primary:\s*(.+)$/);
    if (match) {
      return "Chính: " + translateText(match[1], language);
    }

    match = source.match(/^Primary\s+(.+)$/);
    if (match) {
      return "Chính " + translateText(match[1], language);
    }

    match = source.match(/^Primary Strategy:\s*(.+)$/);
    if (match) {
      return "Chiến lược chính: " + translateText(match[1], language);
    }

    match = source.match(/^Active Strategy:\s*(.+)$/);
    if (match) {
      return "Chiến lược đang hoạt động: " + translateText(match[1], language);
    }

    match = source.match(/^Move to\s+(.+)$/);
    if (match) {
      return "Di chuyển đến " + translateText(match[1], language);
    }

    match = source.match(/^Focused on\s+(.+)\s+in\s+(.+)$/);
    if (match) {
      return "Đang tập trung vào " + match[1] + " tại " + translateText(match[2], language);
    }

    match = source.match(/^Animating\s+(.+)\s+from Base Station through warehouse route dots$/);
    if (match) {
      return "Đang mô phỏng " + match[1] + " từ Trạm chính qua các điểm tuyến trong kho";
    }

    match = source.match(/^Preview only: moving to\s+(.+)\.$/);
    if (match) {
      return "Chỉ xem trước: đang di chuyển đến " + match[1] + ".";
    }

    match = source.match(/^Preview only: pickup at\s+(.+)\.$/);
    if (match) {
      return "Chỉ xem trước: lấy hàng tại " + match[1] + ".";
    }

    match = source.match(/^(.+)\s+is routed to the nearest charging station\.$/);
    if (match) {
      return match[1] + " được điều hướng đến trạm sạc gần nhất.";
    }

    match = source.match(/^(.+)\s+selects the fastest available route\.$/);
    if (match) {
      return match[1] + " chọn tuyến đường nhanh nhất hiện có.";
    }

    match = source.match(/^(.+)\s+reduces speed to preserve battery\.$/);
    if (match) {
      return match[1] + " giảm tốc để tiết kiệm pin.";
    }

    match = source.match(/^(.+)\s+reduces speed and requests a heavy-load route\.$/);
    if (match) {
      return match[1] + " giảm tốc và yêu cầu tuyến tải nặng.";
    }

    match = source.match(/^(.+)\s+slows down and recalculates around the obstacle\.$/);
    if (match) {
      return match[1] + " giảm tốc và tính lại đường vòng qua vật cản.";
    }

    match = source.match(/^(.+)\s+uses a safe route with lower collision risk\.$/);
    if (match) {
      return match[1] + " sử dụng tuyến an toàn với rủi ro va chạm thấp hơn.";
    }

    match = source.match(/^Remaining tasks were reassigned and robot sent to Charging Station\. Reassigned queued missions:\s*(\d+)\. Unassigned queued missions:\s*(\d+)\.$/);
    if (match) {
      return "Các nhiệm vụ còn lại đã được phân công lại và robot được đưa đến Trạm sạc. Nhiệm vụ đã phân công lại: " + match[1] + ". Nhiệm vụ còn chờ chưa phân công: " + match[2] + ".";
    }

    match = source.match(/^Critical battery robot sent to Charging Station\. Reassigned queued missions:\s*(\d+)\. Unassigned queued missions:\s*(\d+)\.$/);
    if (match) {
      return "Robot pin nguy cấp đã được đưa đến Trạm sạc. Nhiệm vụ đã phân công lại: " + match[1] + ". Nhiệm vụ còn chờ chưa phân công: " + match[2] + ".";
    }

    match = source.match(/^Mission\s+(.+)\s+processed through RuleEvaluator and StrategyContext\.$/);
    if (match) {
      return "Nhiệm vụ " + match[1] + " đã được xử lý qua Bộ đánh giá luật (RuleEvaluator) và Bộ điều phối chiến lược (StrategyContext).";
    }

    match = source.match(/^Mission\s+(.+)\s+execution started from Base Station\.$/);
    if (match) {
      return "Nhiệm vụ " + match[1] + " đã bắt đầu thực thi từ Trạm chính.";
    }

    match = source.match(/^Mission\s+(.+)\s+marked COMPLETED\.(?:\s+(.+))?$/);
    if (match) {
      return "Nhiệm vụ " + match[1] + " đã được đánh dấu hoàn tất." + (match[2] ? " " + translateText(match[2], language) : "");
    }

    match = source.match(/^Mission\s+(.+)\s+stopped as CANCELLED\.(?:\s+(.+))?$/);
    if (match) {
      return "Nhiệm vụ " + match[1] + " đã dừng và chuyển sang đã hủy." + (match[2] ? " " + translateText(match[2], language) : "");
    }

    match = source.match(/^Cancelled mission\s+(.+)\s+deleted from the main mission lists\.$/);
    if (match) {
      return "Nhiệm vụ đã hủy " + match[1] + " đã được xóa khỏi danh sách nhiệm vụ chính.";
    }

    match = source.match(/^(Small Cargo|Medium Cargo|Large Cargo)\s+must be assigned to\s+(Zone [ABC])\.$/);
    if (match) {
      return translateText(match[1], language) + " phải được phân vào " + match[2] + ".";
    }

    match = source.match(/^(Zone [ABC])\s+must use\s+(Small Cargo|Medium Cargo|Large Cargo)\.$/);
    if (match) {
      return match[1] + " phải dùng " + translateText(match[2], language) + ".";
    }

    match = source.match(/^Mission not found:\s*(.+)$/);
    if (match) {
      return "Không tìm thấy nhiệm vụ: " + match[1];
    }

    match = source.match(/^Rule not found:\s*(.+)$/);
    if (match) {
      return "Không tìm thấy luật: " + match[1];
    }

    match = source.match(/^Unsupported condition:\s*(.+)$/);
    if (match) {
      return "Điều kiện không được hỗ trợ: " + match[1];
    }

    match = source.match(/^(.+)\s+requires a whole-number threshold\.$/);
    if (match) {
      return match[1] + " yêu cầu ngưỡng là số nguyên.";
    }

    match = source.match(/^(\d+)% battery$/);
    if (match) {
      return "Pin " + match[1] + "%";
    }

    match = source.match(/^(\d+)% battery \(charging \+(\d+)%\)$/);
    if (match) {
      return "Pin " + match[1] + "% (đang sạc +" + match[2] + "%)";
    }

    match = source.match(/^(\d+)% battery \(charging\)$/);
    if (match) {
      return "Pin " + match[1] + "% (đang sạc)";
    }

    match = source.match(/^(\d+)% battery \((\d+)% route drain\)$/);
    if (match) {
      return "Pin " + match[1] + "% (hao " + match[2] + "% theo tuyến)";
    }

    match = source.match(/^(Small Cargo|Medium Cargo|Large Cargo)\s*->\s*(Zone [ABC])\s*\/\s*(\d+)% load$/);
    if (match) {
      return translateText(match[1], language) + " -> " + match[2] + " / mức tải " + match[3] + "%";
    }

    match = source.match(/^([ABC]\d-[ABC]\d)\s*\/\s*(\d+)% load$/);
    if (match) {
      return match[1] + " / mức tải " + match[2] + "%";
    }

    match = source.match(/^(Small Cargo|Medium Cargo|Large Cargo)\s*->\s*(Zone [ABC])$/);
    if (match) {
      return translateText(match[1], language) + " -> " + match[2];
    }

    match = source.match(/^(Zone [ABC])\s*->\s*(Small Cargo|Medium Cargo|Large Cargo)$/);
    if (match) {
      return match[1] + " -> " + translateText(match[2], language);
    }

    match = source.match(/^(.+)\s*->\s*(FastRouteStrategy|EnergySavingStrategy|HeavyLoadStrategy|ObstacleAvoidanceStrategy|ChargingStrategy|SafeRouteStrategy|DefaultStrategy|NormalStrategy)$/);
    if (match) {
      return match[1] + " -> " + translateText(match[2], language);
    }

    return source;
  }

  function translateText(value, language) {
    var source = value == null ? "" : String(value);
    var normalizedSource = normalizeDisplaySource(source);
    var dictionary = exactTranslations[language] || {};
    var translated;

    if (!normalizedSource) {
      return source;
    }

    if (language !== "vi") {
      return source;
    }

    translated = dictionary[normalizedSource] || translatePattern(normalizedSource, language);
    return translated === normalizedSource ? source : preserveWhitespace(source, translated);
  }

  function translateKeyedElements(language, rootElement) {
    var scope = rootElement || document;
    var dictionary = keyedTranslations[language] || keyedTranslations.en;

    scope.querySelectorAll("[data-i18n]").forEach(function (element) {
      var key = element.dataset.i18n;
      if (dictionary[key]) {
        element.textContent = dictionary[key];
      }
    });

    scope.querySelectorAll("[data-i18n-aria-label]").forEach(function (element) {
      var key = element.dataset.i18nAriaLabel;
      if (dictionary[key]) {
        element.setAttribute("aria-label", dictionary[key]);
      }
    });

    scope.querySelectorAll("[data-i18n-title]").forEach(function (element) {
      var key = element.dataset.i18nTitle;
      if (dictionary[key]) {
        element.setAttribute("title", dictionary[key]);
      }
    });

    scope.querySelectorAll("[data-i18n-text]").forEach(function (element) {
      var sourceText = element.dataset.i18nText || element.textContent;

      if (!element.dataset.i18nText) {
        element.dataset.i18nText = sourceText.trim();
      }

      element.textContent = translateText(sourceText, language);
    });
  }

  function shouldSkipTextNode(node) {
    var parent = node.parentElement;

    if (!parent) {
      return true;
    }

    if (/^(SCRIPT|STYLE|NOSCRIPT|TEMPLATE)$/i.test(parent.tagName)) {
      return true;
    }

    if (parent.closest("[data-no-i18n], .material-symbols-outlined")) {
      return true;
    }

    return !normalizeDisplaySource(node.nodeValue);
  }

  function translateTextNodes(language, rootElement) {
    var scope = rootElement || document.body || document;
    var walker = document.createTreeWalker(scope, NodeFilter.SHOW_TEXT, {
      acceptNode: function (node) {
        return shouldSkipTextNode(node) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
      }
    });
    var nodes = [];
    var node;

    while ((node = walker.nextNode())) {
      nodes.push(node);
    }

    nodes.forEach(function (textNode) {
      if (!textNode.__warehouseI18nSource) {
        textNode.__warehouseI18nSource = textNode.nodeValue;
      }

      textNode.nodeValue = translateText(textNode.__warehouseI18nSource, language);
    });
  }

  function datasetKeyForAttribute(attributeName) {
    var camelName = attributeName.replace(/-([a-z])/g, function (_, letter) {
      return letter.toUpperCase();
    });

    return "i18nAuto" + camelName.charAt(0).toUpperCase() + camelName.slice(1);
  }

  function translateAttribute(element, attributeName, language) {
    var sourceKey = datasetKeyForAttribute(attributeName);
    var currentValue = element.getAttribute(attributeName);

    if (!currentValue) {
      return;
    }

    if (!element.dataset[sourceKey]) {
      element.dataset[sourceKey] = currentValue;
    }

    element.setAttribute(attributeName, translateText(element.dataset[sourceKey], language));
  }

  function translateAttributes(language, rootElement) {
    var scope = rootElement || document;

    ["placeholder", "title", "aria-label"].forEach(function (attributeName) {
      scope.querySelectorAll("[" + attributeName + "]").forEach(function (element) {
        if (element.hasAttribute("data-i18n-" + attributeName)) {
          return;
        }

        translateAttribute(element, attributeName, language);
      });
    });
  }

  function translatePage(language, rootElement) {
    var activeLanguage = language || readSetting("language");

    translateKeyedElements(activeLanguage, rootElement);
    translateAttributes(activeLanguage, rootElement);
    translateTextNodes(activeLanguage, rootElement);

    if (!rootElement) {
      document.title = translateText(titleSource, activeLanguage);
    }
  }

  function applySettings(settings, options) {
    var activeSettings = settings || currentSettings();
    var applyOptions = options || {};

    root.lang = activeSettings.language;
    applyClass("theme-", activeSettings.theme, allowedValues.theme);
    applyClass("density-", activeSettings.density, allowedValues.density);

    if (document.body) {
      document.body.classList.toggle("theme-light", activeSettings.theme === "light");
      document.body.classList.toggle("theme-dark", activeSettings.theme === "dark");
      document.body.classList.toggle("density-comfortable", activeSettings.density === "comfortable");
      document.body.classList.toggle("density-compact", activeSettings.density === "compact");

      applyingSettings = true;
      translatePage(activeSettings.language);
      applyingSettings = false;

      if (!applyOptions.skipNotifications &&
          !applyingSettings &&
          window.WarehouseNotifications &&
          typeof window.WarehouseNotifications.render === "function") {
        window.WarehouseNotifications.render(null, { skipSettingsApply: true });
      }

      document.dispatchEvent(new CustomEvent("warehouse:settings-applied", {
        detail: activeSettings
      }));
    }
  }

  function updateSettingsForm(settings) {
    document.querySelectorAll("[data-setting-field]").forEach(function (field) {
      var settingName = field.dataset.settingField;
      if (settings[settingName] !== undefined) {
        field.value = settings[settingName];
      }
    });
  }

  function showSavedMessage() {
    var messages = document.querySelectorAll("[data-settings-saved]");

    if (!messages.length) {
      return;
    }

    messages.forEach(function (message) {
      message.hidden = false;
    });

    window.setTimeout(function () {
      messages.forEach(function (message) {
        message.hidden = true;
      });
    }, 2200);
  }

  function saveFromForm(form) {
    var settings = currentSettings();

    form.querySelectorAll("[data-setting-field]").forEach(function (field) {
      settings[field.dataset.settingField] = writeSetting(field.dataset.settingField, field.value);
    });

    applySettings(settings);
    updateSettingsForm(settings);
    showSavedMessage();
  }

  function bindSettingsForm() {
    var forms = document.querySelectorAll("[data-settings-form]");
    var settings = currentSettings();

    updateSettingsForm(settings);

    if (!forms.length) {
      return;
    }

    forms.forEach(function (form) {
      form.addEventListener("change", function () {
        saveFromForm(form);
      });

      form.addEventListener("submit", function (event) {
        event.preventDefault();
        saveFromForm(form);
      });
    });
  }

  function closeAppDropdowns(activeWidget) {
    document.querySelectorAll("[data-settings-widget], [data-user-widget]").forEach(function (widget) {
      var panel = widget.querySelector("[data-app-dropdown-panel]");

      if (panel && widget !== activeWidget) {
        panel.hidden = true;
      }
    });
  }

  function bindTopbarDropdowns() {
    document.querySelectorAll("[data-settings-widget], [data-user-widget]").forEach(function (widget) {
      var toggle = widget.querySelector("[data-app-dropdown-toggle]");
      var panel = widget.querySelector("[data-app-dropdown-panel]");

      if (!toggle || !panel) {
        return;
      }

      toggle.addEventListener("click", function (event) {
        event.stopPropagation();
        closeAppDropdowns(widget);
        panel.hidden = !panel.hidden;
      });

      panel.addEventListener("click", function (event) {
        event.stopPropagation();
      });
    });

    document.addEventListener("click", function (event) {
      if (event.target.closest("[data-settings-widget], [data-user-widget]")) {
        return;
      }
      closeAppDropdowns(null);
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") {
        closeAppDropdowns(null);
      }
    });
  }

  applySettings(null, { skipNotifications: true });

  document.addEventListener("DOMContentLoaded", function () {
    applySettings(null, { skipNotifications: true });
    bindSettingsForm();
    bindTopbarDropdowns();
  });

  window.WarehouseSettings = {
    get: readSetting,
    set: function (name, value) {
      var normalizedValue = writeSetting(name, value);
      applySettings();
      return normalizedValue;
    },
    apply: applySettings,
    current: currentSettings,
    translateText: function (value) {
      return translateText(value, readSetting("language"));
    },
    translateFragment: function (element) {
      if (element) {
        translatePage(readSetting("language"), element);
      }
    },
    translateStatus: function (value) {
      return translateStatusLabel(value, readSetting("language"));
    },
    translatePriority: function (value) {
      return translatePriorityLabel(value, readSetting("language"));
    },
    translateStrategy: function (value) {
      return translateStrategyName(value, readSetting("language"));
    }
  };
})();
