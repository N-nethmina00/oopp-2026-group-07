-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Oct 05, 2026 at 11:38 AM
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
                         `admin_id` int(11) NOT NULL,
                         `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Table structure for table `batch`
--

CREATE TABLE `batch` (
                         `batch_id` int(11) NOT NULL,
                         `batch_name` varchar(50) NOT NULL,
                         `academic_year` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
                          `lecturer_id` int(11) NOT NULL,
                          `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_material`
--

CREATE TABLE `course_material` (
                                   `material_id` int(11) NOT NULL,
                                   `lecturer_id` int(11) NOT NULL,
                                   `course_id` int(11) NOT NULL,
                                   `material_title` varchar(150) NOT NULL,
                                   `material_type` varchar(50) NOT NULL,
                                   `file_name` varchar(255) NOT NULL,
                                   `file` varchar(255) NOT NULL,
                                   `uploaded_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `department`
--

CREATE TABLE `department` (
                              `dept_id` int(11) NOT NULL,
                              `dept_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Table structure for table `lecturer`
--

CREATE TABLE `lecturer` (
                            `lecturer_id` int(11) NOT NULL,
                            `user_id` int(11) NOT NULL,
                            `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Table structure for table `medical`
--

CREATE TABLE `medical` (
                           `medical_id` int(11) NOT NULL,
                           `reg_no` varchar(20) NOT NULL,
                           `course_id` int(11) NOT NULL,
                           `admin_id` int(11) DEFAULT NULL,
                           `medical_date` date NOT NULL,
                           `reason` text NOT NULL,
                           `approved_by` varchar(100) DEFAULT NULL,
                           `status` enum('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notice`
--

CREATE TABLE `notice` (
                          `notice_id` int(11) NOT NULL,
                          `admin_id` int(11) NOT NULL,
                          `title` varchar(150) NOT NULL,
                          `content` text NOT NULL,
                          `created_date` date NOT NULL,
                          `published_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Table structure for table `technical_officer`
--

CREATE TABLE `technical_officer` (
                                     `to_id` int(11) NOT NULL,
                                     `user_id` int(11) NOT NULL,
                                     `dept_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `timetable`
--

CREATE TABLE `timetable` (
                             `timetable_id` int(11) NOT NULL,
                             `admin_id` int(11) NOT NULL,
                             `course_id` int(11) NOT NULL,
                             `start_time` time NOT NULL,
                             `end_time` time NOT NULL,
                             `lecture_hall` varchar(50) NOT NULL,
                             `session_type` enum('Theory','Practical') NOT NULL,
                             `academic_year` varchar(20) NOT NULL,
                             `semester` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `undergraduate`
--

CREATE TABLE `undergraduate` (
                                 `reg_no` varchar(20) NOT NULL,
                                 `user_id` int(11) NOT NULL,
                                 `batch_id` int(11) NOT NULL,
                                 `dept_id` int(11) NOT NULL,
                                 `is_repeat` tinyint(1) NOT NULL DEFAULT 0,
                                 `is_batchmissed` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
                        `user_id` int(11) NOT NULL,
                        `user_name` varchar(50) NOT NULL,
                        `password` varchar(255) NOT NULL,
                        `first_name` varchar(50) NOT NULL,
                        `last_name` varchar(50) NOT NULL,
                        `email` varchar(100) NOT NULL,
                        `profile_image` varchar(255) DEFAULT NULL,
                        `role` enum('Undergraduate','Technical_Officer','Lecturer','Admin') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
    MODIFY `admin_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
    MODIFY `attendance_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `batch`
--
ALTER TABLE `batch`
    MODIFY `batch_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ca_evaluation`
--
ALTER TABLE `ca_evaluation`
    MODIFY `evaluation_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `course`
--
ALTER TABLE `course`
    MODIFY `course_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `course_material`
--
ALTER TABLE `course_material`
    MODIFY `material_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `department`
--
ALTER TABLE `department`
    MODIFY `dept_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `enrollment`
--
ALTER TABLE `enrollment`
    MODIFY `enrollment_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `lecturer`
--
ALTER TABLE `lecturer`
    MODIFY `lecturer_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mark`
--
ALTER TABLE `mark`
    MODIFY `mark_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `medical`
--
ALTER TABLE `medical`
    MODIFY `medical_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notice`
--
ALTER TABLE `notice`
    MODIFY `notice_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `semester_result`
--
ALTER TABLE `semester_result`
    MODIFY `result_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `sessions`
--
ALTER TABLE `sessions`
    MODIFY `session_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `technical_officer`
--
ALTER TABLE `technical_officer`
    MODIFY `to_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `timetable`
--
ALTER TABLE `timetable`
    MODIFY `timetable_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
    MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

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
