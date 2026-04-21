## โครงสร้างโปรเจกต์

```
project/
├── backend/           Spring Boot 3.5.0 + Java 21 + Maven
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/passenger/
│       │   ├── PassengerApplication.java
│       │   ├── config/
│       │   │   ├── CorsConfig.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── controller/PassengerController.java
│       │   ├── model/
│       │   │   ├── Passenger.java
│       │   │   └── UploadResponse.java
│       │   ├── service/
│       │   │   ├── PassengerService.java
│       │   │   └── PassengerExportService.java
│       │   └── util/StrictOoxmlConverter.java
│       ├── main/resources/application.properties
│       └── test/java/com/example/passenger/
│           ├── service/PassengerServiceTest.java
│           └── controller/PassengerControllerTest.java
└── frontend/          Angular 19 (standalone components)
├── package.json
├── angular.json
├── tsconfig*.json
└── src/
├── index.html, main.ts, styles.css
├── assets/icons/calendar.svg
└── app/
├── app.component.ts
├── app.routes.ts
├── models/passenger.model.ts
├── services/
│   ├── passenger.service.ts
│   └── result.store.ts
├── validators/passenger.validators.ts
└── components/
├── upload/          Initial หน้าจอ (Flight no + file picker)
├── result/          หน้า Result (เรียกใช้ form + table)
├── passenger-form/  Form สำหรับแก้ไข
└── passenger-table/ ตารางแสดงผล + Download link

## Prerequisites

- JDK 21 หรือใหม่กว่า
- Maven (หรือใช้ Embedded Maven ใน Eclipse)
- Node.js 20+ และ npm
- Angular CLI 19: `npm install -g @angular/cli@19`

## วิธี Run

### Backend (port 8080)

```bash
cd backend
mvn clean install           # build + run tests
mvn spring-boot:run         # start server
```

Endpoints:
- `POST /api/passengers/upload` — form-data: `flightNo`, `file` (.xlsx)
- `POST /api/passengers/export?flightNo=XXX` — JSON body (passenger list) → ตอบกลับไฟล์ .xlsx

### Frontend (port 4200)

```bash
cd frontend
npm install
npm start                   # เปิด http://localhost:4200
```

### Run JUnit tests แยก

```bash
cd backend
mvn test
```

ครอบคลุม:
- `PassengerServiceTest` — validate First/Last name, Gender, Date of birth, Nationality
- `PassengerControllerTest` — ปฏิเสธ flight no format ผิด และไฟล์ที่ไม่ใช่ .xlsx

## Validation Rules

| Field          | Rule                                                          |
| -------------- | ------------------------------------------------------------- |
| Flight no      | `[A-Z0-9]{2}[0-9]{1,4}` เช่น TG126, 7C127                      |
| First / Last   | ตัวอักษร A-Z หรือ a-z, ยาวไม่เกิน 20                               |
| Gender         | Male / Female / Unknown                                       |
| Date of birth  | วันที่จริงในปฏิทิน และ ≤ วันที่ปัจจุบัน                                   |
| Nationality    | A-Z 3 ตัวพอดี (เช่น USA, FRA, DEU)                               |
| File           | .xlsx เท่านั้น (ตรวจ magic bytes), ขนาดไม่เกิน 1 MB                |

## Behavior

### Initial page (Upload)
- กรอก Flight no + เลือกไฟล์ Excel → กด Save
- **Auto-fill**: ถ้าเลือกไฟล์ชื่อ `Passenger_{FlightNo}.xlsx` และช่อง Flight no ยังว่าง จะ fill ให้อัตโนมัติ
- ถ้าไฟล์ > 1 MB → แสดง "File size must be less than 1 MB" ใต้ช่อง Excel file
- Validate required + flight no format → ถ้าผ่านส่งไป API
- ถ้า backend reject (ไฟล์ไม่ใช่ .xlsx, row ผิด) → แสดง error + กรอบแดง
- ถ้าถูกต้องทั้งหมด → navigate ไปหน้า Result

### Result page
- แสดงตารางข้อมูลจากไฟล์ + form สำหรับแก้ไข
- กด ✏️ → โหลดข้อมูล row เข้า form (date picker + calendar icon)
- Form: validate แบบเดียวกับ API → Save อัปเดต row ในตาราง / Clear เคลียร์ form
- **File >> Passenger_XXX.xlsx** (ใต้ตาราง): download ไฟล์**ต้นฉบับ**ที่ upload มา
- **Save Excel** (ปุ่มล่างซ้าย): backend generate ไฟล์ใหม่ด้วย POI จากข้อมูลปัจจุบัน (สะท้อนการแก้ไข)
- **Cancel**: กลับไปหน้า Initial# somapa-it-exam
