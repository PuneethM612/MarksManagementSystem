package com.example.marksmanagement.controller;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.model.Student;
import com.example.marksmanagement.model.Subject;
import com.example.marksmanagement.service.MarksService;
import com.example.marksmanagement.service.StudentService;
import com.example.marksmanagement.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/marks")
public class MarksController {

    private final MarksService marksService;
    private final StudentService studentService;
    private final SubjectService subjectService;

    @Autowired
    public MarksController(MarksService marksService, StudentService studentService, SubjectService subjectService) {
        this.marksService = marksService;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public String getAllMarks(Model model) {
        List<Marks> marks = marksService.getAllMarks();
        model.addAttribute("marks", marks);
        return "marks";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("marks", new Marks());
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("examTypes", ExamType.values());
        return "add-marks";
    }

    @PostMapping("/add")
    public String addMarks(@RequestParam String rollNumber, @RequestParam Long subjectId,
                         @RequestParam ExamType examType, @RequestParam Double marks,
                         RedirectAttributes redirectAttributes) {
        
        Optional<Student> student = studentService.getStudentByRollNumber(rollNumber);
        Optional<Subject> subject = subjectService.getSubjectById(subjectId);
        
        if (student.isPresent() && subject.isPresent()) {
            Marks marksObj = new Marks(student.get(), subject.get(), examType, marks);
            marksService.saveMarks(marksObj);
            redirectAttributes.addFlashAttribute("success", "Marks added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Student or Subject not found!");
        }
        
        return "redirect:/marks";
    }

    @GetMapping("/search")
    public String searchMarksForm(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("examTypes", ExamType.values());
        return "search-marks";
    }

    @GetMapping("/search/results")
    public String searchMarks(@RequestParam(required = false) String rollNumber, 
                             @RequestParam(required = false) ExamType examType, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        System.out.println("Entering searchMarks with rollNumber=" + rollNumber + ", examType=" + examType);
        
        try {
            // Validate input parameters
            if (rollNumber == null || rollNumber.trim().isEmpty()) {
                System.out.println("Roll number is empty or null");
                redirectAttributes.addFlashAttribute("error", "Roll number cannot be empty");
                redirectAttributes.addFlashAttribute("students", studentService.getAllStudents());
                redirectAttributes.addFlashAttribute("examTypes", ExamType.values());
                return "redirect:/marks/search";
            }
            
            if (examType == null) {
                System.out.println("Exam type is null, defaulting to MID");
                examType = ExamType.MID; // Default to prevent NPE
            }
            
            System.out.println("Searching marks for roll number: " + rollNumber + ", exam type: " + examType);
            
            // First validate if the student exists - this is critical
            Optional<Student> studentOpt = studentService.getStudentByRollNumber(rollNumber);
            if (!studentOpt.isPresent()) {
                System.out.println("Student not found for roll number: " + rollNumber);
                redirectAttributes.addFlashAttribute("error", "Student not found for roll number: " + rollNumber);
                redirectAttributes.addFlashAttribute("students", studentService.getAllStudents());
                redirectAttributes.addFlashAttribute("examTypes", ExamType.values());
                return "redirect:/marks/search";
            }
            
            Student student = studentOpt.get();
            System.out.println("Found student: " + student.getName());
            
            // Try-catch specifically for the database operation
            List<Marks> marks;
            try {
                // Get marks for student and exam type
                marks = marksService.getMarksByStudentAndExamType(rollNumber, examType);
                System.out.println("Found " + (marks != null ? marks.size() : 0) + " marks for student");
            } catch (Exception e) {
                System.err.println("Database error retrieving marks: " + e.getMessage());
                e.printStackTrace();
                // Always ensure we have a valid list even on error
                marks = Collections.emptyList();
            }
            
            // Always ensure we don't have null lists
            if (marks == null) {
                System.out.println("Marks list is null, creating empty list");
                marks = Collections.emptyList();
            }
            
            // Add all required attributes to the model
            model.addAttribute("marks", marks);
            model.addAttribute("student", student);
            model.addAttribute("examType", examType);
            
            // Return the view without using a layout
            System.out.println("Returning marks-results template");
            return "marks-results";
        } catch (Exception e) {
            // Log the exception details for debugging
            System.err.println("Exception in searchMarks: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message and redirect to search page
            redirectAttributes.addFlashAttribute("error", "Error retrieving marks: " + e.getMessage());
            try {
                redirectAttributes.addFlashAttribute("students", studentService.getAllStudents());
                redirectAttributes.addFlashAttribute("examTypes", ExamType.values());
            } catch (Exception ex) {
                System.err.println("Error loading fallback data: " + ex.getMessage());
            }
            return "redirect:/marks/search";
        }
    }

    @GetMapping("/edit/{studentId}/{subjectId}/{examType}")
    public String showEditForm(@PathVariable String studentId, @PathVariable Long subjectId, 
                             @PathVariable ExamType examType, Model model) {
        
        Optional<Marks> marks = marksService.getMarksByStudentSubjectAndExamType(studentId, subjectId, examType);
        
        if (marks.isPresent()) {
            model.addAttribute("marks", marks.get());
            return "edit-marks";
        } else {
            return "redirect:/marks";
        }
    }

    @PostMapping("/edit")
    public String updateMarks(@ModelAttribute Marks marks) {
        marksService.updateMarks(marks);
        return "redirect:/marks";
    }

    // REST API endpoints

    @GetMapping("/api")
    @ResponseBody
    public List<Marks> getMarksApi() {
        return marksService.getAllMarks();
    }

    @GetMapping("/api/student/{rollNumber}")
    @ResponseBody
    public ResponseEntity<List<Marks>> getMarksByStudentApi(@PathVariable String rollNumber) {
        List<Marks> marks = marksService.getMarksByStudentRollNumber(rollNumber);
        if (marks.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(marks, HttpStatus.OK);
    }

    @GetMapping("/api/student/{rollNumber}/exam/{examType}")
    @ResponseBody
    public ResponseEntity<List<Marks>> getMarksByStudentAndExamTypeApi(
            @PathVariable String rollNumber, @PathVariable ExamType examType) {
        
        List<Marks> marks = marksService.getMarksByStudentAndExamType(rollNumber, examType);
        if (marks.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(marks, HttpStatus.OK);
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Marks> createMarksApi(@RequestBody Marks marks) {
        Marks savedMarks = marksService.saveMarks(marks);
        return new ResponseEntity<>(savedMarks, HttpStatus.CREATED);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Marks> updateMarksApi(@PathVariable Long id, @RequestBody Marks marks) {
        marks.setId(id);
        Marks updatedMarks = marksService.updateMarks(marks);
        return new ResponseEntity<>(updatedMarks, HttpStatus.OK);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteMarksApi(@PathVariable Long id) {
        marksService.deleteMarks(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/top-rankers")
    public String showTopRankers(Model model) {
        try {
            List<TopRankerDTO> topRankers = marksService.getTop3Rankers(ExamType.MONTHLY);
            if (topRankers == null || topRankers.isEmpty()) {
                model.addAttribute("message", "No marks data available for ranking.");
            } else {
                model.addAttribute("topRankers", topRankers);
            }
            return "marks/top-rankers";
        } catch (Exception e) {
            log.error("Error in showTopRankers: {}", e.getMessage());
            model.addAttribute("error", "Unable to fetch top rankers. Please try again later.");
            return "marks/top-rankers";
        }
    }

    @GetMapping("/top-rankers/results")
    public String showTopRankers(@RequestParam ExamType examType, 
                                @RequestParam(defaultValue = "average") String rankingType,
                                Model model) {
        try {
            List<TopRankerDTO> rankers;
            if ("total".equals(rankingType)) {
                rankers = marksService.getTop3RankersByTotalMarks(examType);
            } else {
                rankers = marksService.getTop3Rankers(examType);
            }
            
            if (rankers == null || rankers.isEmpty()) {
                model.addAttribute("message", "No marks data available for ranking.");
            } else {
                model.addAttribute("rankers", rankers);
            }
            
            model.addAttribute("selectedExamType", examType);
            model.addAttribute("selectedRankingType", rankingType);
            model.addAttribute("examTypes", ExamType.values());
            return "top-rankers";
        } catch (Exception e) {
            log.error("Error in showTopRankers results: {}", e.getMessage());
            model.addAttribute("error", "Unable to fetch top rankers. Please try again later.");
            model.addAttribute("examTypes", ExamType.values());
            return "top-rankers";
        }
    }
} 