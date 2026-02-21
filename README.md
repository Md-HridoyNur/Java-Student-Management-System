# Student Management System (SMS) 🏫

![Java](https://img.shields.io/badge/Language-Java-red?logo=java&style=flat-square)
![Swing](https://img.shields.io/badge/UI-Swing-blue?style=flat-square)

A modern, high-fidelity desktop application built with **Java Swing**.  
This application provides a comprehensive **CRUD** interface for managing student records, academic performance, and attendance with a custom dark-themed UI.

---

## Features

### Dashboard Overview
- **Live Statistics**: Displays real-time counts for total students, average GPA, average attendance, and passing rates.  
- **Data Visualization**: Dynamic pie chart showing students by course.  
- **Leaderboard**: Displays top students based on GPA.  

### Students Tab
- **Records Management**: Add, edit, and delete student records.  
- **Live Search**: Filter students by name, email, or course in real time.  
- **Visual Status**: Color-coded GPA and attendance columns for quick assessment.  
- **Data Portability**: Export all student data to a CSV file.  

### Performance & Attendance
- **Grades**: Add or delete subject-specific grades (0-100) with automatic letter grade calculation (A/B/C/D/F).  
- **Attendance**: Mark students as Present or Absent with a date picker and view per-student history.  
- **CSV Export**: Dedicated export options for both grade and attendance logs.  

---

## Requirements
- **Java JDK 8** or newer (full Development Kit required, not just JRE)  
- **Pure Java**: No external libraries needed (built entirely with Java Swing)  

---

## How to Run

1. Open a terminal in the folder containing `StudentManagementSystem.java`.  
2. Compile the source code:
   ```bash
   javac StudentManagementSystem.java
   java StudentManagementSystem
   
