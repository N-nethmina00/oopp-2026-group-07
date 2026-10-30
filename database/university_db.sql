-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Oct 30, 2026 at 09:06 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `university_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `admin_id` varchar(10) NOT NULL,
  `user_id` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`admin_id`, `user_id`) VALUES
('A001', 'A001');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` int(11) NOT NULL,
  `enrollment_id` int(11) NOT NULL,
  `session_id` int(11) NOT NULL,
  `status` enum('Present','Absent','Late','Excused') NOT NULL DEFAULT 'Absent'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `enrollment_id`, `session_id`, `status`) VALUES
(1, 1, 1, 'Present'),
(2, 3, 1, 'Late'),
(3, 5, 1, 'Absent'),
(4, 2, 2, 'Present'),
(5, 4, 3, 'Excused');

-- --------------------------------------------------------

--
-- Table structure for table `batch`
--

CREATE TABLE `batch` (
  `batch_id` int(11) NOT NULL,
  `batch_name` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `batch`
--

INSERT INTO `batch` (`batch_id`, `batch_name`, `academic_year`) VALUES
(1, 'ICT Batch 2023', '2023/2024'),
(2, 'ICT Batch 2024', '2024/2025'),
(3, 'CS Batch 2024', '2024/2025');

-- --------------------------------------------------------

--
-- Table structure for table `ca_evaluation`
--

CREATE TABLE `ca_evaluation` (
  `evaluation_id` int(11) NOT NULL,
  `enrollment_id` int(11) NOT NULL,
  `total_quiz_marks` decimal(5,2) DEFAULT NULL,
  `CA_status` varchar(30) DEFAULT NULL,
  `eligibility` tinyint(1) DEFAULT 1,
  `quiz1_marks` decimal(5,2) DEFAULT NULL,
  `quiz2_marks` decimal(5,2) DEFAULT NULL,
  `quiz_marks` decimal(5,2) DEFAULT NULL,
  `assignment1_marks` decimal(5,2) DEFAULT NULL,
  `assignment2_marks` decimal(5,2) DEFAULT NULL,
  `mid_exam_marks` decimal(5,2) DEFAULT NULL,
  `total_CA_marks` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ca_evaluation`
--

INSERT INTO `ca_evaluation` (`evaluation_id`, `enrollment_id`, `total_quiz_marks`, `CA_status`, `eligibility`, `quiz1_marks`, `quiz2_marks`, `quiz_marks`, `assignment1_marks`, `assignment2_marks`, `mid_exam_marks`, `total_CA_marks`) VALUES
(1, 1, 17.00, 'Completed', 1, 8.00, 9.00, 17.00, 18.00, 17.00, 22.00, 74.00),
(2, 3, 15.00, 'Completed', 1, 7.00, 8.00, 15.00, 16.00, 18.00, 20.00, 69.00),
(3, 5, 12.00, 'In Progress', 1, 6.00, 6.00, 12.00, 15.00, 14.00, 18.00, 59.00);

-- --------------------------------------------------------

--
-- Table structure for table `course`
--

CREATE TABLE `course` (
  `course_id` int(11) NOT NULL,
  `course_code` varchar(20) NOT NULL,
  `course_name` varchar(150) NOT NULL,
  `credit` int(11) NOT NULL,
  `type` enum('Theory','Practical') NOT NULL,
  `lecturer_id` varchar(10) NOT NULL,
  `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `course`
--

INSERT INTO `course` (`course_id`, `course_code`, `course_name`, `credit`, `type`, `lecturer_id`, `dept_id`) VALUES
(1, 'ICT2132', 'Object Oriented Programming', 3, 'Theory', 'L001', 1),
(2, 'ICT2133', 'Object Oriented Programming Practical', 1, 'Practical', 'L001', 1),
(3, 'ICT2142', 'Database Management Systems', 3, 'Theory', 'L001', 1),
(4, 'CSC2101', 'Data Structures and Algorithms', 3, 'Theory', 'L002', 2),
(5, 'CSC2102', 'Software Engineering', 3, 'Theory', 'L002', 2);

-- --------------------------------------------------------

--
-- Table structure for table `course_material`
--

CREATE TABLE `course_material` (
  `material_id` int(11) NOT NULL,
  `lecturer_id` varchar(10) NOT NULL,
  `course_id` int(11) NOT NULL,
  `material_title` varchar(150) NOT NULL,
  `material_type` varchar(50) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file` varchar(255) NOT NULL,
  `uploaded_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `course_material`
--

INSERT INTO `course_material` (`material_id`, `lecturer_id`, `course_id`, `material_title`, `material_type`, `file_name`, `file`, `uploaded_date`) VALUES
(1, 'L001', 1, 'OOP Introduction Slides', 'PDF', 'oop_intro.pdf', 'materials/oop_intro.pdf', '2026-10-01'),
(2, 'L001', 3, 'Database Basics', 'PDF', 'database_basics.pdf', 'materials/database_basics.pdf', '2026-10-02'),
(3, 'L002', 4, 'Data Structures Notes', 'PDF', 'data_structures.pdf', 'materials/data_structures.pdf', '2026-10-03');

-- --------------------------------------------------------

--
-- Table structure for table `department`
--

CREATE TABLE `department` (
  `dept_id` int(11) NOT NULL,
  `dept_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `department`
--

INSERT INTO `department` (`dept_id`, `dept_name`) VALUES
(2, 'Computer Science'),
(1, 'Information Technology');

-- --------------------------------------------------------

--
-- Table structure for table `enrollment`
--

CREATE TABLE `enrollment` (
  `enrollment_id` int(11) NOT NULL,
  `reg_no` varchar(20) NOT NULL,
  `course_id` int(11) NOT NULL,
  `enroll_date` date NOT NULL,
  `status` enum('Enrolled','Completed','Dropped') NOT NULL DEFAULT 'Enrolled'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `enrollment`
--

INSERT INTO `enrollment` (`enrollment_id`, `reg_no`, `course_id`, `enroll_date`, `status`) VALUES
(1, 'TG2000', 1, '2026-09-01', 'Enrolled'),
(2, 'TG2000', 2, '2026-09-01', 'Enrolled'),
(3, 'TG2001', 1, '2026-09-01', 'Enrolled'),
(4, 'TG2001', 3, '2026-09-01', 'Enrolled'),
(5, 'TG2002', 1, '2026-09-02', 'Enrolled'),
(6, 'TG2003', 4, '2026-09-02', 'Enrolled'),
(7, 'TG2004', 5, '2026-09-02', 'Enrolled');

-- --------------------------------------------------------

--
-- Table structure for table `lecturer`
--

CREATE TABLE `lecturer` (
  `lecturer_id` varchar(10) NOT NULL,
  `user_id` varchar(10) NOT NULL,
  `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lecturer`
--

INSERT INTO `lecturer` (`lecturer_id`, `user_id`, `dept_id`) VALUES
('L001', 'L001', 1),
('L002', 'L002', 2);

-- --------------------------------------------------------

--
-- Table structure for table `mark`
--

CREATE TABLE `mark` (
  `mark_id` int(11) NOT NULL,
  `enrollment_id` int(11) NOT NULL,
  `end_exam_marks` decimal(5,2) DEFAULT NULL,
  `total_CA_marks` decimal(5,2) DEFAULT NULL,
  `total_marks` decimal(5,2) DEFAULT NULL,
  `grade` varchar(5) DEFAULT NULL,
  `grade_point` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mark`
--

INSERT INTO `mark` (`mark_id`, `enrollment_id`, `end_exam_marks`, `total_CA_marks`, `total_marks`, `grade`, `grade_point`) VALUES
(1, 1, 18.00, 74.00, 92.00, 'A+', 4.00),
(2, 3, 16.00, 69.00, 85.00, 'A', 4.00),
(3, 5, 14.00, 59.00, 73.00, 'B+', 3.30);

-- --------------------------------------------------------

--
-- Table structure for table `medical`
--

CREATE TABLE `medical` (
  `medical_id` int(11) NOT NULL,
  `reg_no` varchar(20) NOT NULL,
  `course_id` int(11) NOT NULL,
  `admin_id` varchar(10) DEFAULT NULL,
  `medical_date` date NOT NULL,
  `reason` text NOT NULL,
  `approved_by` varchar(100) DEFAULT NULL,
  `status` enum('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `medical`
--

INSERT INTO `medical` (`medical_id`, `reg_no`, `course_id`, `admin_id`, `medical_date`, `reason`, `approved_by`, `status`) VALUES
(1, 'TG2000', 1, 'A001', '2026-10-10', 'Fever and medical rest', NULL, 'Pending'),
(2, 'TG2001', 3, 'A001', '2026-10-08', 'Hospital appointment', 'Nimal Perera', 'Approved'),
(3, 'TG2002', 1, 'A001', '2026-10-12', 'Viral infection', NULL, 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `notice`
--

CREATE TABLE `notice` (
  `notice_id` int(11) NOT NULL,
  `admin_id` varchar(10) NOT NULL,
  `title` varchar(150) NOT NULL,
  `content` text NOT NULL,
  `created_date` date NOT NULL,
  `published_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notice`
--

INSERT INTO `notice` (`notice_id`, `admin_id`, `title`, `content`, `created_date`, `published_date`) VALUES
(1, 'A001', 'Semester Examination Timetable', 'The semester examination timetable will be published soon. Please check the student portal regularly.', '2026-10-01', '2026-10-02'),
(2, 'A001', 'Library Opening Hours', 'The library will be open from 8.00 AM to 6.00 PM on weekdays.', '2026-10-05', '2026-10-05'),
(3, 'A001', 'Project Submission Reminder', 'Students should submit their mini project documents before the announced deadline.', '2026-10-15', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `semester_result`
--

CREATE TABLE `semester_result` (
  `result_id` int(11) NOT NULL,
  `reg_no` varchar(20) NOT NULL,
  `semester` varchar(20) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `GPA` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `semester_result`
--

INSERT INTO `semester_result` (`result_id`, `reg_no`, `semester`, `academic_year`, `GPA`) VALUES
(1, 'TG2000', 'Semester 1', '2025/2026', 3.65),
(2, 'TG2001', 'Semester 1', '2025/2026', 3.42),
(3, 'TG2002', 'Semester 1', '2025/2026', 3.18);

-- --------------------------------------------------------

--
-- Table structure for table `sessions`
--

CREATE TABLE `sessions` (
  `session_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `session_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `session_type` enum('Theory','Practical') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sessions`
--

INSERT INTO `sessions` (`session_id`, `course_id`, `session_date`, `start_time`, `end_time`, `session_type`) VALUES
(1, 1, '2026-10-20', '08:30:00', '10:30:00', 'Theory'),
(2, 2, '2026-10-21', '10:30:00', '12:30:00', 'Practical'),
(3, 3, '2026-10-22', '13:00:00', '15:00:00', 'Theory'),
(4, 4, '2026-10-23', '08:30:00', '10:30:00', 'Theory'),
(5, 5, '2026-10-24', '10:30:00', '12:30:00', 'Theory');

-- --------------------------------------------------------

--
-- Table structure for table `technical_officer`
--

CREATE TABLE `technical_officer` (
  `to_id` varchar(10) NOT NULL,
  `user_id` varchar(10) NOT NULL,
  `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `technical_officer`
--

INSERT INTO `technical_officer` (`to_id`, `user_id`, `dept_id`) VALUES
('T001', 'T001', 1);

-- --------------------------------------------------------

--
-- Table structure for table `timetable`
--

CREATE TABLE `timetable` (
  `timetable_id` int(11) NOT NULL,
  `admin_id` varchar(10) NOT NULL,
  `course_id` int(11) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `lecture_hall` varchar(50) NOT NULL,
  `session_type` enum('Theory','Practical') NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `semester` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `timetable`
--

INSERT INTO `timetable` (`timetable_id`, `admin_id`, `course_id`, `start_time`, `end_time`, `lecture_hall`, `session_type`, `academic_year`, `semester`) VALUES
(1, 'A001', 1, '08:30:00', '10:30:00', 'LT-01', 'Theory', '2026/2027', 'Semester 1'),
(2, 'A001', 2, '10:30:00', '12:30:00', 'Lab-02', 'Practical', '2026/2027', 'Semester 1'),
(3, 'A001', 3, '13:00:00', '15:00:00', 'LT-03', 'Theory', '2026/2027', 'Semester 1'),
(4, 'A001', 4, '08:30:00', '10:30:00', 'LT-02', 'Theory', '2026/2027', 'Semester 1'),
(5, 'A001', 5, '10:30:00', '12:30:00', 'LT-04', 'Theory', '2026/2027', 'Semester 1');

-- --------------------------------------------------------

--
-- Table structure for table `undergraduate`
--

CREATE TABLE `undergraduate` (
  `reg_no` varchar(20) NOT NULL,
  `user_id` varchar(10) NOT NULL,
  `batch_id` int(11) NOT NULL,
  `dept_id` int(11) NOT NULL,
  `is_repeat` tinyint(1) NOT NULL DEFAULT 0,
  `is_batchmissed` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `undergraduate`
--

INSERT INTO `undergraduate` (`reg_no`, `user_id`, `batch_id`, `dept_id`, `is_repeat`, `is_batchmissed`) VALUES
('TG2000', 'U001', 1, 1, 0, 0),
('TG2001', 'U002', 1, 1, 0, 0),
('TG2002', 'U003', 2, 1, 0, 0),
('TG2003', 'U004', 2, 2, 0, 0),
('TG2004', 'U005', 3, 2, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `user_id` varchar(10) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `role` enum('Undergraduate','Technical_Officer','Lecturer','Admin') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `user_name`, `password`, `first_name`, `last_name`, `email`, `profile_image`, `role`) VALUES
('A001', 'admin01', 'Admin@123', 'Nimal', 'Perera', 'nimal.admin@example.com', NULL, 'Admin'),
('L001', 'lecturer01', 'Lecturer@123', 'Kamal', 'Silva', 'kamal.silva@example.com', NULL, 'Lecturer'),
('L002', 'lecturer02', 'Lecturer@123', 'Sanduni', 'Fernando', 'sanduni.fernando@example.com', NULL, 'Lecturer'),
('T001', 'tech01', 'Tech@123', 'Amara', 'Jayasinghe', 'amara.tech@example.com', NULL, 'Technical_Officer'),
('U001', 'student01', 'Student@123', 'Kasun', 'Wickramasinghe', 'kasun.student@example.com', NULL, 'Undergraduate'),
('U002', 'student02', 'Student@123', 'Tharushi', 'Bandara', 'tharushi.student@example.com', NULL, 'Undergraduate'),
('U003', 'student03', 'Student@123', 'Dilan', 'Peris', 'dilan.student@example.com', NULL, 'Undergraduate'),
('U004', 'student04', 'Student@123', 'Mihiri', 'Senanayake', 'mihiri.student@example.com', NULL, 'Undergraduate'),
('U005', 'student05', 'Student@123', 'Ravindu', 'Dissanayake', 'ravindu.student@example.com', NULL, 'Undergraduate');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`admin_id`),
  ADD UNIQUE KEY `uq_admin_user` (`user_id`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD UNIQUE KEY `uq_attendance` (`enrollment_id`,`session_id`),
  ADD KEY `idx_attendance_enrollment` (`enrollment_id`),
  ADD KEY `idx_attendance_session` (`session_id`);

--
-- Indexes for table `batch`
--
ALTER TABLE `batch`
  ADD PRIMARY KEY (`batch_id`);

--
-- Indexes for table `ca_evaluation`
--
ALTER TABLE `ca_evaluation`
  ADD PRIMARY KEY (`evaluation_id`),
  ADD UNIQUE KEY `uq_ca_enrollment` (`enrollment_id`);

--
-- Indexes for table `course`
--
ALTER TABLE `course`
  ADD PRIMARY KEY (`course_id`),
  ADD UNIQUE KEY `uq_course_code` (`course_code`),
  ADD KEY `idx_course_lecturer` (`lecturer_id`),
  ADD KEY `idx_course_department` (`dept_id`);

--
-- Indexes for table `course_material`
--
ALTER TABLE `course_material`
  ADD PRIMARY KEY (`material_id`),
  ADD KEY `idx_material_lecturer` (`lecturer_id`),
  ADD KEY `idx_material_course` (`course_id`);

--
-- Indexes for table `department`
--
ALTER TABLE `department`
  ADD PRIMARY KEY (`dept_id`),
  ADD UNIQUE KEY `uq_department_name` (`dept_name`);

--
-- Indexes for table `enrollment`
--
ALTER TABLE `enrollment`
  ADD PRIMARY KEY (`enrollment_id`),
  ADD UNIQUE KEY `uq_enrollment` (`reg_no`,`course_id`),
  ADD KEY `idx_enrollment_reg_no` (`reg_no`),
  ADD KEY `idx_enrollment_course` (`course_id`);

--
-- Indexes for table `lecturer`
--
ALTER TABLE `lecturer`
  ADD PRIMARY KEY (`lecturer_id`),
  ADD UNIQUE KEY `uq_lecturer_user` (`user_id`),
  ADD KEY `idx_lecturer_department` (`dept_id`);

--
-- Indexes for table `mark`
--
ALTER TABLE `mark`
  ADD PRIMARY KEY (`mark_id`),
  ADD UNIQUE KEY `uq_mark_enrollment` (`enrollment_id`);

--
-- Indexes for table `medical`
--
ALTER TABLE `medical`
  ADD PRIMARY KEY (`medical_id`),
  ADD KEY `idx_medical_reg_no` (`reg_no`),
  ADD KEY `idx_medical_course` (`course_id`),
  ADD KEY `idx_medical_admin` (`admin_id`);

--
-- Indexes for table `notice`
--
ALTER TABLE `notice`
  ADD PRIMARY KEY (`notice_id`),
  ADD KEY `idx_notice_admin` (`admin_id`);

--
-- Indexes for table `semester_result`
--
ALTER TABLE `semester_result`
  ADD PRIMARY KEY (`result_id`),
  ADD UNIQUE KEY `uq_semester_result` (`reg_no`,`semester`,`academic_year`),
  ADD KEY `idx_result_reg_no` (`reg_no`);

--
-- Indexes for table `sessions`
--
ALTER TABLE `sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD KEY `idx_sessions_course` (`course_id`);

--
-- Indexes for table `technical_officer`
--
ALTER TABLE `technical_officer`
  ADD PRIMARY KEY (`to_id`),
  ADD UNIQUE KEY `uq_to_user` (`user_id`),
  ADD KEY `idx_to_department` (`dept_id`);

--
-- Indexes for table `timetable`
--
ALTER TABLE `timetable`
  ADD PRIMARY KEY (`timetable_id`),
  ADD KEY `idx_timetable_admin` (`admin_id`),
  ADD KEY `idx_timetable_course` (`course_id`);

--
-- Indexes for table `undergraduate`
--
ALTER TABLE `undergraduate`
  ADD PRIMARY KEY (`reg_no`),
  ADD UNIQUE KEY `uq_undergraduate_user` (`user_id`),
  ADD KEY `idx_undergraduate_batch` (`batch_id`),
  ADD KEY `idx_undergraduate_department` (`dept_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uq_user_username` (`user_name`),
  ADD UNIQUE KEY `uq_user_email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendance_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `batch`
--
ALTER TABLE `batch`
  MODIFY `batch_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `ca_evaluation`
--
ALTER TABLE `ca_evaluation`
  MODIFY `evaluation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `course`
--
ALTER TABLE `course`
  MODIFY `course_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `course_material`
--
ALTER TABLE `course_material`
  MODIFY `material_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `department`
--
ALTER TABLE `department`
  MODIFY `dept_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `enrollment`
--
ALTER TABLE `enrollment`
  MODIFY `enrollment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `mark`
--
ALTER TABLE `mark`
  MODIFY `mark_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `medical`
--
ALTER TABLE `medical`
  MODIFY `medical_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `notice`
--
ALTER TABLE `notice`
  MODIFY `notice_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `semester_result`
--
ALTER TABLE `semester_result`
  MODIFY `result_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `sessions`
--
ALTER TABLE `sessions`
  MODIFY `session_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `timetable`
--
ALTER TABLE `timetable`
  MODIFY `timetable_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `fk_attendance_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_attendance_session` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `ca_evaluation`
--
ALTER TABLE `ca_evaluation`
  ADD CONSTRAINT `fk_ca_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course`
--
ALTER TABLE `course`
  ADD CONSTRAINT `fk_course_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_course_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturer` (`lecturer_id`) ON UPDATE CASCADE;

--
-- Constraints for table `course_material`
--
ALTER TABLE `course_material`
  ADD CONSTRAINT `fk_material_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_material_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturer` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `enrollment`
--
ALTER TABLE `enrollment`
  ADD CONSTRAINT `fk_enrollment_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_enrollment_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `lecturer`
--
ALTER TABLE `lecturer`
  ADD CONSTRAINT `fk_lecturer_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_lecturer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `mark`
--
ALTER TABLE `mark`
  ADD CONSTRAINT `fk_mark_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `medical`
--
ALTER TABLE `medical`
  ADD CONSTRAINT `fk_medical_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_medical_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_medical_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `notice`
--
ALTER TABLE `notice`
  ADD CONSTRAINT `fk_notice_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `semester_result`
--
ALTER TABLE `semester_result`
  ADD CONSTRAINT `fk_result_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `sessions`
--
ALTER TABLE `sessions`
  ADD CONSTRAINT `fk_sessions_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `technical_officer`
--
ALTER TABLE `technical_officer`
  ADD CONSTRAINT `fk_to_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_to_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `timetable`
--
ALTER TABLE `timetable`
  ADD CONSTRAINT `fk_timetable_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_timetable_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `undergraduate`
--
ALTER TABLE `undergraduate`
  ADD CONSTRAINT `fk_undergraduate_batch` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_undergraduate_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_undergraduate_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
