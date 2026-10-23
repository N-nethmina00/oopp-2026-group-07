-- MySQL dump 10.13  Distrib 9.7.0, for Win64 (x86_64)
--
-- Host: localhost    Database: university_db
-- ------------------------------------------------------
-- Server version	9.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `university_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `university_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `university_db`;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
                         `admin_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                         `user_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                         PRIMARY KEY (`admin_id`),
                         UNIQUE KEY `uq_admin_user` (`user_id`),
                         CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES ('A001','A001');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
                              `attendance_id` int NOT NULL AUTO_INCREMENT,
                              `enrollment_id` int NOT NULL,
                              `session_id` int NOT NULL,
                              `status` enum('Present','Absent','Late','Excused') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Absent',
                              PRIMARY KEY (`attendance_id`),
                              UNIQUE KEY `uq_attendance` (`enrollment_id`,`session_id`),
                              KEY `idx_attendance_enrollment` (`enrollment_id`),
                              KEY `idx_attendance_session` (`session_id`),
                              CONSTRAINT `fk_attendance_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                              CONSTRAINT `fk_attendance_session` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
INSERT INTO `attendance` VALUES (1,1,1,'Present'),(2,3,1,'Late'),(3,5,1,'Absent'),(4,2,2,'Present'),(5,4,3,'Excused');
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batch`
--

DROP TABLE IF EXISTS `batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch` (
                         `batch_id` int NOT NULL AUTO_INCREMENT,
                         `batch_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                         `academic_year` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                         PRIMARY KEY (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch`
--

LOCK TABLES `batch` WRITE;
/*!40000 ALTER TABLE `batch` DISABLE KEYS */;
INSERT INTO `batch` VALUES (1,'ICT Batch 2023','2023/2024'),(2,'ICT Batch 2024','2024/2025'),(3,'CS Batch 2024','2024/2025');
/*!40000 ALTER TABLE `batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ca_evaluation`
--

DROP TABLE IF EXISTS `ca_evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ca_evaluation` (
                                 `evaluation_id` int NOT NULL AUTO_INCREMENT,
                                 `enrollment_id` int NOT NULL,
                                 `total_quiz_marks` decimal(5,2) DEFAULT NULL,
                                 `CA_status` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                 `eligibility` tinyint(1) DEFAULT '1',
                                 `quiz1_marks` decimal(5,2) DEFAULT NULL,
                                 `quiz2_marks` decimal(5,2) DEFAULT NULL,
                                 `quiz_marks` decimal(5,2) DEFAULT NULL,
                                 `assignment1_marks` decimal(5,2) DEFAULT NULL,
                                 `assignment2_marks` decimal(5,2) DEFAULT NULL,
                                 `mid_exam_marks` decimal(5,2) DEFAULT NULL,
                                 `total_CA_marks` decimal(5,2) DEFAULT NULL,
                                 PRIMARY KEY (`evaluation_id`),
                                 UNIQUE KEY `uq_ca_enrollment` (`enrollment_id`),
                                 CONSTRAINT `fk_ca_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ca_evaluation`
--

LOCK TABLES `ca_evaluation` WRITE;
/*!40000 ALTER TABLE `ca_evaluation` DISABLE KEYS */;
INSERT INTO `ca_evaluation` VALUES (1,1,17.00,'Completed',1,8.00,9.00,17.00,18.00,17.00,22.00,74.00),(2,3,15.00,'Completed',1,7.00,8.00,15.00,16.00,18.00,20.00,69.00),(3,5,12.00,'In Progress',1,6.00,6.00,12.00,15.00,14.00,18.00,59.00);
/*!40000 ALTER TABLE `ca_evaluation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
                          `course_id` int NOT NULL AUTO_INCREMENT,
                          `course_code` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                          `course_name` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
                          `credit` int NOT NULL,
                          `type` enum('Theory','Practical') COLLATE utf8mb4_general_ci NOT NULL,
                          `lecturer_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                          `dept_id` int NOT NULL,
                          PRIMARY KEY (`course_id`),
                          UNIQUE KEY `uq_course_code` (`course_code`),
                          KEY `idx_course_lecturer` (`lecturer_id`),
                          KEY `idx_course_department` (`dept_id`),
                          CONSTRAINT `fk_course_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
                          CONSTRAINT `fk_course_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturer` (`lecturer_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'TCS2112','Business Economics',3,'Theory','L001',1),(2,'ICT2113','Data Structures and Algorithms',3,'Theory','L001',1),(3,'ICT2142','E-Business Systems',3,'Theory','L001',1),(4,'ENG2112','English III (ICT)',2,'Theory','L001',1),(5,'ICT2162','Management Information Systems',3,'Theory','L001',1),(6,'ICT2152','Object Oriented Analysis and Design',3,'Theory','L001',1),(7,'ICT2122','Object Oriented Programming',3,'Theory','L001',1),(8,'ICT2132','Object Oriented Programming Practicum',1,'Practical','L001',1),(9,'TCS2121','Soft Skills',2,'Practical','L001',1);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_material`
--

DROP TABLE IF EXISTS `course_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_material` (
                                   `material_id` int NOT NULL AUTO_INCREMENT,
                                   `lecturer_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                                   `course_id` int NOT NULL,
                                   `material_title` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
                                   `material_type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                                   `file_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                   `file` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                   `uploaded_date` date NOT NULL,
                                   PRIMARY KEY (`material_id`),
                                   KEY `idx_material_lecturer` (`lecturer_id`),
                                   KEY `idx_material_course` (`course_id`),
                                   CONSTRAINT `fk_material_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                   CONSTRAINT `fk_material_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturer` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_material`
--

LOCK TABLES `course_material` WRITE;
/*!40000 ALTER TABLE `course_material` DISABLE KEYS */;
INSERT INTO `course_material` VALUES (1,'L001',1,'OOP Introduction Slides','PDF','oop_intro.pdf','materials/oop_intro.pdf','2026-10-01'),(2,'L001',3,'Database Basics','PDF','database_basics.pdf','materials/database_basics.pdf','2026-10-02'),(3,'L002',2,'Data Structures Notes','PDF','data_structures.pdf','materials/data_structures.pdf','2026-10-03');
/*!40000 ALTER TABLE `course_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
                              `dept_id` int NOT NULL AUTO_INCREMENT,
                              `dept_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
                              PRIMARY KEY (`dept_id`),
                              UNIQUE KEY `uq_department_name` (`dept_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (2,'Computer Science'),(1,'Information Technology');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment` (
                              `enrollment_id` int NOT NULL AUTO_INCREMENT,
                              `reg_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                              `course_id` int NOT NULL,
                              `enroll_date` date NOT NULL,
                              `status` enum('Enrolled','Completed','Dropped') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Enrolled',
                              PRIMARY KEY (`enrollment_id`),
                              UNIQUE KEY `uq_enrollment` (`reg_no`,`course_id`),
                              KEY `idx_enrollment_reg_no` (`reg_no`),
                              KEY `idx_enrollment_course` (`course_id`),
                              CONSTRAINT `fk_enrollment_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                              CONSTRAINT `fk_enrollment_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment`
--

LOCK TABLES `enrollment` WRITE;
/*!40000 ALTER TABLE `enrollment` DISABLE KEYS */;
INSERT INTO `enrollment` VALUES (1,'TG2000',1,'2026-09-01','Enrolled'),(2,'TG2000',2,'2026-09-01','Enrolled'),(3,'TG2001',1,'2026-09-01','Enrolled'),(4,'TG2001',3,'2026-09-01','Enrolled'),(5,'TG2002',1,'2026-09-02','Enrolled'),(6,'TG2003',4,'2026-09-02','Enrolled'),(7,'TG2004',5,'2026-09-02','Enrolled');
/*!40000 ALTER TABLE `enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lecturer`
--

DROP TABLE IF EXISTS `lecturer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lecturer` (
                            `lecturer_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                            `user_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                            `dept_id` int NOT NULL,
                            PRIMARY KEY (`lecturer_id`),
                            UNIQUE KEY `uq_lecturer_user` (`user_id`),
                            KEY `idx_lecturer_department` (`dept_id`),
                            CONSTRAINT `fk_lecturer_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
                            CONSTRAINT `fk_lecturer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lecturer`
--

LOCK TABLES `lecturer` WRITE;
/*!40000 ALTER TABLE `lecturer` DISABLE KEYS */;
INSERT INTO `lecturer` VALUES ('L001','L001',1),('L002','L002',2);
/*!40000 ALTER TABLE `lecturer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mark`
--

DROP TABLE IF EXISTS `mark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mark` (
                        `mark_id` int NOT NULL AUTO_INCREMENT,
                        `enrollment_id` int NOT NULL,
                        `end_exam_marks` decimal(5,2) DEFAULT NULL,
                        `total_CA_marks` decimal(5,2) DEFAULT NULL,
                        `total_marks` decimal(5,2) DEFAULT NULL,
                        `grade` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL,
                        `grade_point` decimal(3,2) DEFAULT NULL,
                        PRIMARY KEY (`mark_id`),
                        UNIQUE KEY `uq_mark_enrollment` (`enrollment_id`),
                        CONSTRAINT `fk_mark_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`enrollment_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mark`
--

LOCK TABLES `mark` WRITE;
/*!40000 ALTER TABLE `mark` DISABLE KEYS */;
INSERT INTO `mark` VALUES (1,1,18.00,74.00,92.00,'A+',4.00),(2,3,16.00,69.00,85.00,'A',4.00),(3,5,14.00,59.00,73.00,'B+',3.30);
/*!40000 ALTER TABLE `mark` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical`
--

DROP TABLE IF EXISTS `medical`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical` (
                           `medical_id` int NOT NULL AUTO_INCREMENT,
                           `reg_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                           `course_id` int NOT NULL,
                           `admin_id` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
                           `medical_date` date NOT NULL,
                           `reason` text COLLATE utf8mb4_general_ci NOT NULL,
                           `approved_by` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                           `status` enum('Pending','Approved','Rejected') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Pending',
                           PRIMARY KEY (`medical_id`),
                           KEY `idx_medical_reg_no` (`reg_no`),
                           KEY `idx_medical_course` (`course_id`),
                           KEY `idx_medical_admin` (`admin_id`),
                           CONSTRAINT `fk_medical_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON DELETE SET NULL ON UPDATE CASCADE,
                           CONSTRAINT `fk_medical_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                           CONSTRAINT `fk_medical_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical`
--

LOCK TABLES `medical` WRITE;
/*!40000 ALTER TABLE `medical` DISABLE KEYS */;
INSERT INTO `medical` VALUES (1,'TG2000',1,'A001','2026-10-10','Fever and medical rest',NULL,'Pending'),(2,'TG2001',3,'A001','2026-10-08','Hospital appointment','Nimal Perera','Approved'),(3,'TG2002',1,'A001','2026-10-12','Viral infection',NULL,'Pending');
/*!40000 ALTER TABLE `medical` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
                          `notice_id` int NOT NULL AUTO_INCREMENT,
                          `admin_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                          `title` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
                          `content` text COLLATE utf8mb4_general_ci NOT NULL,
                          `created_date` date NOT NULL,
                          `published_date` date DEFAULT NULL,
                          PRIMARY KEY (`notice_id`),
                          KEY `idx_notice_admin` (`admin_id`),
                          CONSTRAINT `fk_notice_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,'A001','Semester Examination Timetable','The semester examination timetable will be published soon. Please check the student portal regularly.','2026-10-01','2026-10-02'),(2,'A001','Library Opening Hours','The library will be open from 8.00 AM to 6.00 PM on weekdays.','2026-10-05','2026-10-05'),(3,'A001','Project Submission Reminder','Students should submit their mini project documents before the announced deadline.','2026-10-15',NULL);
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `semester_result`
--

DROP TABLE IF EXISTS `semester_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `semester_result` (
                                   `result_id` int NOT NULL AUTO_INCREMENT,
                                   `reg_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                                   `semester` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                                   `academic_year` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                                   `GPA` decimal(3,2) DEFAULT NULL,
                                   PRIMARY KEY (`result_id`),
                                   UNIQUE KEY `uq_semester_result` (`reg_no`,`semester`,`academic_year`),
                                   KEY `idx_result_reg_no` (`reg_no`),
                                   CONSTRAINT `fk_result_undergraduate` FOREIGN KEY (`reg_no`) REFERENCES `undergraduate` (`reg_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `semester_result`
--

LOCK TABLES `semester_result` WRITE;
/*!40000 ALTER TABLE `semester_result` DISABLE KEYS */;
INSERT INTO `semester_result` VALUES (1,'TG2000','Semester 1','2025/2026',3.65),(2,'TG2001','Semester 1','2025/2026',3.42),(3,'TG2002','Semester 1','2025/2026',3.18);
/*!40000 ALTER TABLE `semester_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessions` (
                            `session_id` int NOT NULL AUTO_INCREMENT,
                            `course_id` int NOT NULL,
                            `session_date` date NOT NULL,
                            `start_time` time NOT NULL,
                            `end_time` time NOT NULL,
                            `session_type` enum('Theory','Practical') COLLATE utf8mb4_general_ci NOT NULL,
                            PRIMARY KEY (`session_id`),
                            KEY `idx_sessions_course` (`course_id`),
                            CONSTRAINT `fk_sessions_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessions`
--

LOCK TABLES `sessions` WRITE;
/*!40000 ALTER TABLE `sessions` DISABLE KEYS */;
INSERT INTO `sessions` VALUES (1,1,'2026-10-20','08:30:00','10:30:00','Theory'),(2,2,'2026-10-21','10:30:00','12:30:00','Practical'),(3,3,'2026-10-22','13:00:00','15:00:00','Theory'),(4,4,'2026-10-23','08:30:00','10:30:00','Theory'),(5,5,'2026-10-24','10:30:00','12:30:00','Theory');
/*!40000 ALTER TABLE `sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `technical_officer`
--

DROP TABLE IF EXISTS `technical_officer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technical_officer` (
                                     `to_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                                     `user_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                                     `dept_id` int NOT NULL,
                                     PRIMARY KEY (`to_id`),
                                     UNIQUE KEY `uq_to_user` (`user_id`),
                                     KEY `idx_to_department` (`dept_id`),
                                     CONSTRAINT `fk_to_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
                                     CONSTRAINT `fk_to_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `technical_officer`
--

LOCK TABLES `technical_officer` WRITE;
/*!40000 ALTER TABLE `technical_officer` DISABLE KEYS */;
INSERT INTO `technical_officer` VALUES ('T001','T001',1);
/*!40000 ALTER TABLE `technical_officer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timetable`
--

DROP TABLE IF EXISTS `timetable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timetable` (
                             `timetable_id` int NOT NULL AUTO_INCREMENT,
                             `admin_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                             `course_id` int NOT NULL,
                             `day_of_week` enum('Monday','Tuesday','Wednesday','Thursday','Friday') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Monday',
                             `start_time` time NOT NULL,
                             `end_time` time NOT NULL,
                             `lecture_hall` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                             `session_type` enum('Theory','Practical') COLLATE utf8mb4_general_ci NOT NULL,
                             `academic_year` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                             `semester` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                             PRIMARY KEY (`timetable_id`),
                             KEY `idx_timetable_admin` (`admin_id`),
                             KEY `idx_timetable_course` (`course_id`),
                             CONSTRAINT `fk_timetable_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON UPDATE CASCADE,
                             CONSTRAINT `fk_timetable_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timetable`
--

LOCK TABLES `timetable` WRITE;
/*!40000 ALTER TABLE `timetable` DISABLE KEYS */;
INSERT INTO `timetable` VALUES (6,'A001',2,'Monday','09:00:00','11:00:00','ICT Lab 11','Theory','2026/2027','Semester 1'),(7,'A001',2,'Monday','11:00:00','13:00:00','ICT Lab 11','Practical','2026/2027','Semester 1'),(8,'A001',3,'Tuesday','09:00:00','12:00:00','ICT New Lab','Theory','2026/2027','Semester 1'),(9,'A001',6,'Tuesday','13:00:00','15:00:00','ICT Lab 12','Theory','2026/2027','Semester 1'),(10,'A001',1,'Wednesday','08:00:00','10:00:00','ICT ILT','Theory','2026/2027','Semester 1'),(11,'A001',9,'Wednesday','10:00:00','12:00:00','ICT Hardware Lab','Practical','2026/2027','Semester 1'),(12,'A001',7,'Thursday','09:00:00','11:00:00','ICT Lab 11','Theory','2026/2027','Semester 1'),(13,'A001',8,'Thursday','11:00:00','13:00:00','ICT Lab 11','Practical','2026/2027','Semester 1'),(14,'A001',8,'Thursday','14:00:00','16:00:00','ICT Lab 11','Practical','2026/2027','Semester 1'),(15,'A001',1,'Friday','08:00:00','10:00:00','ICT Hardware Lab','Theory','2026/2027','Semester 1'),(16,'A001',5,'Friday','10:00:00','12:00:00','ICT New Lab','Theory','2026/2027','Semester 1'),(17,'A001',4,'Friday','13:00:00','15:00:00','ICT Hardware Lab','Theory','2026/2027','Semester 1');
/*!40000 ALTER TABLE `timetable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `undergraduate`
--

DROP TABLE IF EXISTS `undergraduate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undergraduate` (
                                 `reg_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                                 `user_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                                 `batch_id` int NOT NULL,
                                 `dept_id` int NOT NULL,
                                 `is_repeat` tinyint(1) NOT NULL DEFAULT '0',
                                 `is_batchmissed` tinyint(1) NOT NULL DEFAULT '0',
                                 PRIMARY KEY (`reg_no`),
                                 UNIQUE KEY `uq_undergraduate_user` (`user_id`),
                                 KEY `idx_undergraduate_batch` (`batch_id`),
                                 KEY `idx_undergraduate_department` (`dept_id`),
                                 CONSTRAINT `fk_undergraduate_batch` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`) ON UPDATE CASCADE,
                                 CONSTRAINT `fk_undergraduate_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON UPDATE CASCADE,
                                 CONSTRAINT `fk_undergraduate_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `undergraduate`
--

LOCK TABLES `undergraduate` WRITE;
/*!40000 ALTER TABLE `undergraduate` DISABLE KEYS */;
INSERT INTO `undergraduate` VALUES ('TG2000','U001',1,1,0,0),('TG2001','U002',1,1,0,0),('TG2002','U003',2,1,0,0),('TG2003','U004',2,2,0,0),('TG2004','U005',3,2,0,0);
/*!40000 ALTER TABLE `undergraduate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `user_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
                        `user_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                        `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                        `first_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                        `last_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                        `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
                        `profile_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                        `role` enum('Undergraduate','Technical_Officer','Lecturer','Admin') COLLATE utf8mb4_general_ci NOT NULL,
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY `uq_user_username` (`user_name`),
                        UNIQUE KEY `uq_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('A001','admin01','Admin@123','Nimal','Perera','nimal.admin@example.com',NULL,'Admin'),('L001','lecturer01','Lecturer@123','Kamal','Silva','kamal.silva@example.com',NULL,'Lecturer'),('L002','lecturer02','Lecturer@123','Sanduni','Fernando','sanduni.fernando@example.com',NULL,'Lecturer'),('T001','tech01','Tech@123','Amara','Jayasinghe','amara.tech@example.com',NULL,'Technical_Officer'),('U001','student01','Student@123','Kasun','Wickramasinghe','kasun.student@example.com',NULL,'Undergraduate'),('U002','student02','Student@123','Tharushi','Bandara','tharushi.student@example.com',NULL,'Undergraduate'),('U003','student03','Student@123','Dilan','Peris','dilan.student@example.com',NULL,'Undergraduate'),('U004','student04','Student@123','Mihiri','Senanayake','mihiri.senanayake@example.com',NULL,'Undergraduate'),('U005','student05','Student@123','Ravindu','Dissanayake','ravindu.student@example.com',NULL,'Undergraduate');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'university_db'
--

--
-- Dumping routines for database 'university_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22 19:37:28
