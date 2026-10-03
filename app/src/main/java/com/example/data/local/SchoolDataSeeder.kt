package com.example.data.local

import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SchoolDataSeeder {

    suspend fun seedIfEmpty(dao: SchoolDao) = withContext(Dispatchers.IO) {
        if (dao.getStudentCount() > 0) return@withContext

        // 1. Seed Students
        val sampleStudents = listOf(
            Student(
                id = 1,
                admissionNo = "ADM-2024-001",
                name = "Aria Montgomery",
                grade = "Grade 10",
                section = "A",
                rollNo = "101",
                guardianName = "Byron Montgomery",
                phone = "+1 (555) 234-8901",
                email = "aria.m@oakridge.edu",
                monthlyFee = 450.0,
                admissionDate = "2024-08-10",
                status = "Active"
            ),
            Student(
                id = 2,
                admissionNo = "ADM-2024-002",
                name = "Liam Alexander Vance",
                grade = "Grade 10",
                section = "A",
                rollNo = "102",
                guardianName = "Eleanor Vance",
                phone = "+1 (555) 345-6789",
                email = "liam.v@oakridge.edu",
                monthlyFee = 450.0,
                admissionDate = "2024-08-12",
                status = "Active"
            ),
            Student(
                id = 3,
                admissionNo = "ADM-2024-003",
                name = "Sophia Chen",
                grade = "Grade 10",
                section = "B",
                rollNo = "103",
                guardianName = "David Chen",
                phone = "+1 (555) 456-7890",
                email = "sophia.c@oakridge.edu",
                monthlyFee = 480.0,
                admissionDate = "2024-08-15",
                status = "Active"
            ),
            Student(
                id = 4,
                admissionNo = "ADM-2024-004",
                name = "Ethan Michael Ross",
                grade = "Grade 9",
                section = "A",
                rollNo = "901",
                guardianName = "Sarah Ross",
                phone = "+1 (555) 567-8901",
                email = "ethan.r@oakridge.edu",
                monthlyFee = 420.0,
                admissionDate = "2025-01-09",
                status = "Active"
            ),
            Student(
                id = 5,
                admissionNo = "ADM-2024-005",
                name = "Zara Fatima Al-Hassan",
                grade = "Grade 9",
                section = "A",
                rollNo = "902",
                guardianName = "Tariq Al-Hassan",
                phone = "+1 (555) 678-9012",
                email = "zara.a@oakridge.edu",
                monthlyFee = 420.0,
                admissionDate = "2025-01-11",
                status = "Active"
            ),
            Student(
                id = 6,
                admissionNo = "ADM-2024-006",
                name = "Marcus Aurelius Thorne",
                grade = "Grade 8",
                section = "A",
                rollNo = "801",
                guardianName = "Cassandra Thorne",
                phone = "+1 (555) 789-0123",
                email = "marcus.t@oakridge.edu",
                monthlyFee = 390.0,
                admissionDate = "2025-08-01",
                status = "Active"
            )
        )
        dao.insertStudents(sampleStudents)

        // 2. Seed Fee Records & Invoices
        val sampleFees = listOf(
            FeeRecord(
                id = 1,
                studentId = 1,
                invoiceNo = "INV-2026-0901",
                monthYear = "September 2026",
                dueDate = "2026-09-15",
                tuitionFee = 450.0,
                libraryFee = 35.0,
                transportFee = 80.0,
                labOrExamFee = 50.0,
                otherFee = 15.0,
                totalAmount = 630.0,
                paidAmount = 630.0,
                paymentStatus = "Paid",
                paymentDate = "2026-09-10",
                paymentMethod = "Bank Transfer",
                receiptNo = "REC-2026-0041",
                remarks = "Paid in full via online wire transfer"
            ),
            FeeRecord(
                id = 2,
                studentId = 1,
                invoiceNo = "INV-2026-0801",
                monthYear = "August 2026",
                dueDate = "2026-08-15",
                tuitionFee = 450.0,
                libraryFee = 35.0,
                transportFee = 80.0,
                labOrExamFee = 0.0,
                otherFee = 0.0,
                totalAmount = 565.0,
                paidAmount = 565.0,
                paymentStatus = "Paid",
                paymentDate = "2026-08-12",
                paymentMethod = "Cash",
                receiptNo = "REC-2026-0012",
                remarks = "Cash received by Bursar"
            ),
            FeeRecord(
                id = 3,
                studentId = 2,
                invoiceNo = "INV-2026-0902",
                monthYear = "September 2026",
                dueDate = "2026-09-15",
                tuitionFee = 450.0,
                libraryFee = 35.0,
                transportFee = 0.0,
                labOrExamFee = 50.0,
                otherFee = 0.0,
                totalAmount = 535.0,
                paidAmount = 300.0,
                paymentStatus = "Partial",
                paymentDate = "2026-09-14",
                paymentMethod = "Online/UPI",
                receiptNo = "REC-2026-0048",
                remarks = "Installment 1 paid; balance of $235 due end of month"
            ),
            FeeRecord(
                id = 4,
                studentId = 3,
                invoiceNo = "INV-2026-0903",
                monthYear = "September 2026",
                dueDate = "2026-09-15",
                tuitionFee = 480.0,
                libraryFee = 40.0,
                transportFee = 90.0,
                labOrExamFee = 60.0,
                otherFee = 20.0,
                totalAmount = 690.0,
                paidAmount = 0.0,
                paymentStatus = "Pending",
                paymentDate = null,
                paymentMethod = null,
                receiptNo = null,
                remarks = "Reminder notification sent to guardian"
            ),
            FeeRecord(
                id = 5,
                studentId = 4,
                invoiceNo = "INV-2026-0904",
                monthYear = "September 2026",
                dueDate = "2026-09-15",
                tuitionFee = 420.0,
                libraryFee = 30.0,
                transportFee = 75.0,
                labOrExamFee = 40.0,
                otherFee = 0.0,
                totalAmount = 565.0,
                paidAmount = 565.0,
                paymentStatus = "Paid",
                paymentDate = "2026-09-08",
                paymentMethod = "Cheque",
                receiptNo = "REC-2026-0037",
                remarks = "Cheque #8849 cleared"
            ),
            FeeRecord(
                id = 6,
                studentId = 5,
                invoiceNo = "INV-2026-0905",
                monthYear = "September 2026",
                dueDate = "2026-09-15",
                tuitionFee = 420.0,
                libraryFee = 30.0,
                transportFee = 0.0,
                labOrExamFee = 40.0,
                otherFee = 10.0,
                totalAmount = 500.0,
                paidAmount = 0.0,
                paymentStatus = "Pending",
                paymentDate = null,
                paymentMethod = null,
                receiptNo = null,
                remarks = "Pending guardian confirmation"
            )
        )
        dao.insertFeeRecords(sampleFees)

        // 3. Seed Exams & Subjects
        val sampleExams = listOf(
            Exam(
                id = 1,
                name = "Annual Examination 2026",
                academicYear = "2025-2026",
                term = "Term 2 Final",
                startDate = "2026-06-10",
                status = "Published"
            ),
            Exam(
                id = 2,
                name = "Mid-Term Examination 2026",
                academicYear = "2025-2026",
                term = "Term 1 Mid-Term",
                startDate = "2026-02-15",
                status = "Published"
            )
        )
        dao.insertExams(sampleExams)

        val sampleSubjects = listOf(
            Subject(id = 1, name = "Advanced Mathematics", code = "MTH-10", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0),
            Subject(id = 2, name = "Physics & Lab Practice", code = "PHY-10", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0),
            Subject(id = 3, name = "Chemistry & Organic Analysis", code = "CHM-10", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0),
            Subject(id = 4, name = "English Literature & Composition", code = "ENG-10", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0),
            Subject(id = 5, name = "Computer Science & Python", code = "CSC-10", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0)
        )
        dao.insertSubjects(sampleSubjects)

        // 4. Seed Exam Results for Aria Montgomery (student 1) and Liam Vance (student 2)
        val sampleResults = listOf(
            // Student 1 (Aria) - High Achiever
            ExamResult(id = 1, examId = 1, studentId = 1, subjectId = 1, marksObtained = 94.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Outstanding analytical ability"),
            ExamResult(id = 2, examId = 1, studentId = 1, subjectId = 2, marksObtained = 91.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Excellent lab precision"),
            ExamResult(id = 3, examId = 1, studentId = 1, subjectId = 3, marksObtained = 88.0, maxMarks = 100.0, gradeLetter = "A", gradePoint = 3.8, remarks = "Very thorough comprehension"),
            ExamResult(id = 4, examId = 1, studentId = 1, subjectId = 4, marksObtained = 96.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Exceptional essay expression"),
            ExamResult(id = 5, examId = 1, studentId = 1, subjectId = 5, marksObtained = 98.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Superb coding logic"),

            // Student 2 (Liam)
            ExamResult(id = 6, examId = 1, studentId = 2, subjectId = 1, marksObtained = 82.0, maxMarks = 100.0, gradeLetter = "A", gradePoint = 3.6, remarks = "Strong mathematical problem solving"),
            ExamResult(id = 7, examId = 1, studentId = 2, subjectId = 2, marksObtained = 79.0, maxMarks = 100.0, gradeLetter = "B+", gradePoint = 3.3, remarks = "Good physics conceptual work"),
            ExamResult(id = 8, examId = 1, studentId = 2, subjectId = 3, marksObtained = 74.0, maxMarks = 100.0, gradeLetter = "B", gradePoint = 3.0, remarks = "Solid performance"),
            ExamResult(id = 9, examId = 1, studentId = 2, subjectId = 4, marksObtained = 85.0, maxMarks = 100.0, gradeLetter = "A", gradePoint = 3.7, remarks = "Commendable vocabulary"),
            ExamResult(id = 10, examId = 1, studentId = 2, subjectId = 5, marksObtained = 90.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Excellent algorithmic projects")
        )
        dao.insertExamResults(sampleResults)

        // 5. Seed Staff Members
        val sampleStaff = listOf(
            Staff(
                id = 1,
                employeeId = "EMP-101",
                name = "Dr. Robert Sterling",
                role = "Principal",
                department = "Administration",
                email = "r.sterling@oakridge.edu",
                phone = "+1 (555) 100-2001",
                address = "12 Faculty Row, West Wing",
                joiningDate = "2018-06-01",
                subjectsTaught = "Educational Leadership, Ethics",
                assignedClasses = "All Sections",
                baseSalary = 6200.0,
                allowances = 850.0,
                deductions = 420.0,
                status = "Active"
            ),
            Staff(
                id = 2,
                employeeId = "EMP-102",
                name = "Prof. Margaret Holloway",
                role = "Department Head",
                department = "Mathematics",
                email = "m.holloway@oakridge.edu",
                phone = "+1 (555) 100-2002",
                address = "45 Elmwood Drive, Suite 3",
                joiningDate = "2019-08-15",
                subjectsTaught = "Advanced Mathematics, Calculus",
                assignedClasses = "Grade 10-A, Grade 10-B",
                baseSalary = 4800.0,
                allowances = 500.0,
                deductions = 290.0,
                status = "Active"
            ),
            Staff(
                id = 3,
                employeeId = "EMP-103",
                name = "Jonathan Hayes",
                role = "Teacher",
                department = "Science",
                email = "j.hayes@oakridge.edu",
                phone = "+1 (555) 100-2003",
                address = "88 Pinecrest Lane",
                joiningDate = "2021-01-10",
                subjectsTaught = "Physics & Lab Practice",
                assignedClasses = "Grade 9-A, Grade 10-A",
                baseSalary = 3900.0,
                allowances = 400.0,
                deductions = 210.0,
                status = "Active"
            ),
            Staff(
                id = 4,
                employeeId = "EMP-104",
                name = "Elena Rostova",
                role = "Accountant",
                department = "Accounts & Finance",
                email = "e.rostova@oakridge.edu",
                phone = "+1 (555) 100-2004",
                address = "214 Birchwood Terrace",
                joiningDate = "2020-04-01",
                subjectsTaught = "N/A (Finance Staff)",
                assignedClasses = "School Bursar Office",
                baseSalary = 4100.0,
                allowances = 450.0,
                deductions = 250.0,
                status = "Active"
            ),
            Staff(
                id = 5,
                employeeId = "EMP-105",
                name = "Julian Thorne",
                role = "Teacher",
                department = "Computer Science",
                email = "j.thorne@oakridge.edu",
                phone = "+1 (555) 100-2005",
                address = "77 Innovation Way",
                joiningDate = "2022-09-01",
                subjectsTaught = "Computer Science & Python",
                assignedClasses = "Grade 10-A, Grade 9-A",
                baseSalary = 4000.0,
                allowances = 350.0,
                deductions = 200.0,
                status = "On Leave"
            )
        )
        dao.insertStaffList(sampleStaff)

        // 6. Seed Leave Requests
        val sampleLeaves = listOf(
            LeaveRequest(
                id = 1,
                staffId = 5,
                staffName = "Julian Thorne",
                leaveType = "Casual Leave",
                startDate = "2026-10-02",
                endDate = "2026-10-05",
                daysCount = 3,
                reason = "Attending National STEM Educators Colloquium",
                status = "Approved",
                appliedDate = "2026-09-25",
                adminNotes = "Approved by Principal Sterling. Substitute arranged."
            ),
            LeaveRequest(
                id = 2,
                staffId = 3,
                staffName = "Jonathan Hayes",
                leaveType = "Sick Leave",
                startDate = "2026-10-06",
                endDate = "2026-10-07",
                daysCount = 2,
                reason = "Medical dental surgery recovery",
                status = "Pending",
                appliedDate = "2026-09-29",
                adminNotes = null
            ),
            LeaveRequest(
                id = 3,
                staffId = 2,
                staffName = "Prof. Margaret Holloway",
                leaveType = "Annual Leave",
                startDate = "2026-11-10",
                endDate = "2026-11-14",
                daysCount = 5,
                reason = "Family sabbatical travel",
                status = "Pending",
                appliedDate = "2026-09-30",
                adminNotes = null
            )
        )
        dao.insertLeaveRequests(sampleLeaves)

        // 7. Seed Student Daily Attendance Check-ins
        if (dao.getAttendanceCount() == 0) {
            val dates = listOf("2026-09-25", "2026-09-28", "2026-09-29", "2026-09-30", "2026-10-01")
            val attendances = mutableListOf<com.example.data.model.StudentAttendance>()

            // Students 1 to 6
            for (stId in 1L..6L) {
                for (date in dates) {
                    val status = when {
                        stId == 3L && date == "2026-09-29" -> "Absent"
                        stId == 2L && date == "2026-09-30" -> "Late"
                        stId == 5L && date == "2026-09-28" -> "Excused"
                        else -> "Present"
                    }
                    val time = if (status == "Late") "08:24 AM" else if (status == "Present") "07:55 AM" else null
                    val remarks = when (status) {
                        "Absent" -> "Unexcused medical absence"
                        "Late" -> "School bus delay"
                        "Excused" -> "Inter-school debate tournament"
                        else -> null
                    }
                    attendances.add(
                        com.example.data.model.StudentAttendance(
                            studentId = stId,
                            date = date,
                            status = status,
                            checkInTime = time,
                            remarks = remarks
                        )
                    )
                }
            }
            dao.insertStudentAttendanceList(attendances)
        }

        // 8. Seed Default School Profile
        dao.insertOrUpdateSchoolProfile(
            com.example.data.model.SchoolProfile(
                id = 1L,
                schoolName = "Oakridge International Academy",
                schoolMotto = "Excellence in Academic Leadership & Innovation",
                campusAddress = "Main Academic Campus, Kathmandu, Nepal",
                contactPhone = "+977-1-4567890",
                contactEmail = "info@school.edu.np",
                academicSession = "Session 2025-2026",
                principalName = "Dr. Arthur Pendelton",
                currencySymbol = "Rs.",
                affiliationCode = "NEB-REG-48201",
                websiteUrl = "www.oakridge.edu.np"
            )
        )

        // 9. Seed Default Multi-Tenant User Credentials (RBAC)
        if (dao.getUserAccountCount() == 0) {
            val sampleUsers = listOf(
                com.example.data.model.UserAccount(
                    id = 1L,
                    fullName = "Dr. Arthur Pendelton (Admin)",
                    username = "admin",
                    passwordHash = "admin123",
                    role = "ADMIN",
                    schoolTenantId = "school-1",
                    status = "Active",
                    createdAt = "2026-08-01"
                ),
                com.example.data.model.UserAccount(
                    id = 2L,
                    fullName = "Bishal Thapa (Accountant)",
                    username = "accountant",
                    passwordHash = "pay123",
                    role = "ACCOUNTANT",
                    schoolTenantId = "school-1",
                    status = "Active",
                    createdAt = "2026-08-10"
                ),
                com.example.data.model.UserAccount(
                    id = 3L,
                    fullName = "Dr. Sarah Jenkins (Faculty)",
                    username = "teacher",
                    passwordHash = "teach123",
                    role = "TEACHER",
                    schoolTenantId = "school-1",
                    status = "Active",
                    createdAt = "2026-08-15"
                ),
                com.example.data.model.UserAccount(
                    id = 4L,
                    fullName = "David Chen (Parent)",
                    username = "parent",
                    passwordHash = "read123",
                    role = "PARENT",
                    schoolTenantId = "school-1",
                    status = "Active",
                    createdAt = "2026-09-01"
                )
            )
            dao.insertUserAccounts(sampleUsers)
        }
    }
}
