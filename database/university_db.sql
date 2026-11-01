-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Oct 31, 2026 at 05:56 PM
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
    ('A001', 'U001');

-- --------------------------------------------------------

--
-- Stand-in structure for view `ams_ca_batch_summary`
-- (See below for the actual view)
--
CREATE TABLE `ams_ca_batch_summary` (
`batch_id` int(11)
,`course_code` varchar(20)
,`students` bigint(21)
,`average_CA` decimal(6,2)
,`CA_passed` decimal(22,0)
,`exam_eligible` decimal(22,0)
,`pending_results` decimal(22,0)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `ams_result_details`
-- (See below for the actual view)
--
CREATE TABLE `ams_result_details` (
`user_id` varchar(10)
,`reg_no` varchar(20)
,`batch_id` int(11)
,`is_repeat` tinyint(1)
,`course_code` varchar(20)
,`course_name` varchar(150)
,`credit` int(11)
,`quiz1_marks` decimal(5,2)
,`quiz2_marks` decimal(5,2)
,`quiz3_marks` decimal(5,2)
,`total_quiz_marks` decimal(5,2)
,`quiz_marks` decimal(5,2)
,`assignment1_marks` decimal(5,2)
,`assignment2_marks` decimal(5,2)
,`mid_exam_marks` decimal(5,2)
,`total_CA_marks` decimal(5,2)
,`CA_status` varchar(30)
,`eligibility` tinyint(1)
,`total_sessions` bigint(21)
,`present_sessions` decimal(22,0)
,`attendance_percent` decimal(28,2)
,`end_exam_marks` decimal(5,2)
,`total_marks` decimal(5,2)
,`grade` varchar(5)
,`grade_point` decimal(3,2)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `ams_semester_gpa`
-- (See below for the actual view)