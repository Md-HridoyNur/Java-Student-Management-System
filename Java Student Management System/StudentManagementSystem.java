import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

/**
 * Student Management System
 * A full CRUD desktop app built with Java Swing.
 *
 * Compile:  javac StudentManagementSystem.java
 * Run:      java StudentManagementSystem
 *
 * Requires: Java 8+  (no external libraries needed)
 */
public class StudentManagementSystem extends JFrame {

    // ── Palette ────────────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(0x0F1117);
    static final Color BG_CARD      = new Color(0x1A1D2E);
    static final Color BG_INPUT     = new Color(0x252840);
    static final Color ACCENT_BLUE  = new Color(0x4F8EF7);
    static final Color ACCENT_GREEN = new Color(0x3ECFA4);
    static final Color ACCENT_ORG   = new Color(0xF7874F);
    static final Color ACCENT_PURP  = new Color(0xA78BFA);
    static final Color TEXT_PRI     = new Color(0xEEF0FF);
    static final Color TEXT_SEC     = new Color(0x8890B5);
    static final Color BORDER_CLR   = new Color(0x2E3250);

    // ── Data ───────────────────────────────────────────────────────────────────
    static int nextId = 1000;

    static class Student {
        int id;
        String name, email, course, gender;
        int age;
        List<Grade> grades = new ArrayList<>();
        List<AttRecord> attendance = new ArrayList<>();

        Student(String name, String email, int age, String gender, String course) {
            this.id = nextId++;
            this.name = name; this.email = email;
            this.age = age; this.gender = gender; this.course = course;
        }

        double gpa() {
            if (grades.isEmpty()) return 0.0;
            return grades.stream().mapToDouble(g -> g.point).average().orElse(0.0);
        }

        double attendancePct() {
            if (attendance.isEmpty()) return 100.0;
            long present = attendance.stream().filter(a -> a.present).count();
            return (present * 100.0) / attendance.size();
        }
    }

    static class Grade {
        String subject; double score; String letter; double point;
        Grade(String s, double sc) {
            subject = s; score = sc;
            if (sc >= 80)      { letter = "A+"; point = 4.00; }
            else if (sc >= 75) { letter = "A";  point = 3.75; }
            else if (sc >= 70) { letter = "A-"; point = 3.50; }
            else if (sc >= 65) { letter = "B+"; point = 3.25; }
            else if (sc >= 60) { letter = "B";  point = 3.00; }
            else if (sc >= 55) { letter = "B-"; point = 2.75; }
            else if (sc >= 50) { letter = "C+"; point = 2.50; }
            else if (sc >= 45) { letter = "C";  point = 2.25; }
            else if (sc >= 40) { letter = "D";  point = 2.00; }
            else               { letter = "F";  point = 0.00; }
        }
    }

    static class AttRecord {
        String date; boolean present;
        AttRecord(String d, boolean p) { date = d; present = p; }
    }

    // ── State ──────────────────────────────────────────────────────────────────
    final List<Student> students = new ArrayList<>();
    DefaultTableModel studentModel, gradeModel, attModel;
    JTable studentTable, gradeTable, attTable;
    JLabel statTotal, statAvgGpa, statAvgAtt, statPassing;
    JComboBox<String> gradeStudentCombo, attStudentCombo;
    JPanel chartPanel;
    JTabbedPane tabs;

    // ── Constructor ────────────────────────────────────────────────────────────
    public StudentManagementSystem() {
        setTitle("Student Management System");
        setSize(1280, 820);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        seedData();
        buildUI();
        refreshAll();
        setVisible(true);
    }

    // ── Seed ───────────────────────────────────────────────────────────────────
    void seedData() {
        String[][] rows = {
            {"Alice Johnson",  "alice@uni.edu",  "21", "Female", "Computer Science"},
            {"Bob Martinez",   "bob@uni.edu",    "22", "Male",   "Mathematics"},
            {"Chen Wei",       "chen@uni.edu",   "20", "Male",   "Physics"},
            {"Diya Patel",     "diya@uni.edu",   "23", "Female", "Engineering"},
            {"Evan Brooks",    "evan@uni.edu",   "21", "Male",   "Computer Science"},
            {"Fatima Nour",    "fatima@uni.edu", "22", "Female", "Chemistry"},
        };
        String[] subjects = {"Math", "Science", "English", "History", "Programming"};
        String[] dates    = {"2024-01-15","2024-01-22","2024-01-29","2024-02-05",
                             "2024-02-12","2024-02-19","2024-02-26","2024-03-04"};
        Random rnd = new Random(42);

        for (String[] r : rows) {
            Student s = new Student(r[0], r[1], Integer.parseInt(r[2]), r[3], r[4]);
            for (String sub : subjects)
                s.grades.add(new Grade(sub, 55 + rnd.nextDouble() * 45));
            for (String d : dates)
                s.attendance.add(new AttRecord(d, rnd.nextDouble() > 0.15));
            students.add(s);
        }
    }

    // ── Build UI ───────────────────────────────────────────────────────────────
    void buildUI() {
        add(buildSidebar(), BorderLayout.WEST);

        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRI);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 13));

        tabs.addTab("  Dashboard  ",  buildDashboard());
        tabs.addTab("  Students   ",  buildStudentsTab());
        tabs.addTab("  Grades     ",  buildGradesTab());
        tabs.addTab("  Attendance ",  buildAttendanceTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────
    JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(BG_CARD);
        side.setPreferredSize(new Dimension(195, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_CLR));

        JLabel logo = new JLabel("<html><center><b>SMS</b><br><font size=2>Student Management</font></center></html>");
        logo.setFont(new Font("Monospaced", Font.BOLD, 20));
        logo.setForeground(ACCENT_BLUE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(28, 10, 28, 10));
        side.add(logo);

        side.add(sideBtn("Dashboard",  0));
        side.add(sideBtn("Students",   1));
        side.add(sideBtn("Grades",     2));
        side.add(sideBtn("Attendance", 3));
        side.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("SMS v1.0  |  Java Swing");
        footer.setForeground(TEXT_SEC);
        footer.setFont(new Font("Monospaced", Font.PLAIN, 10));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        side.add(footer);
        return side;
    }

    JButton sideBtn(String text, int tabIdx) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.PLAIN, 13));
        btn.setForeground(TEXT_SEC);
        btn.setBackground(BG_CARD);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> tabs.setSelectedIndex(tabIdx));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_BLUE); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_SEC); }
        });
        return btn;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────
    JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(TEXT_PRI);
        panel.add(title, BorderLayout.NORTH);

        // stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setBackground(BG_DARK);
        statTotal   = addStatCard(cards, "Total Students", "0",    ACCENT_BLUE);
        statAvgGpa  = addStatCard(cards, "Avg GPA",        "0.00", ACCENT_GREEN);
        statAvgAtt  = addStatCard(cards, "Avg Attendance", "0%",   ACCENT_ORG);
        statPassing = addStatCard(cards, "Passing Rate",   "0%",   ACCENT_PURP);
        panel.add(cards, BorderLayout.CENTER);

        // chart + top list
        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setBackground(BG_DARK);
        bottom.setPreferredSize(new Dimension(0, 260));

        chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart((Graphics2D) g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(BG_CARD);

        JPanel chartWrap = wrapWithTitle("Students by Course", chartPanel);
        bottom.add(chartWrap);
        bottom.add(buildTopStudents());
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    JLabel addStatCard(JPanel parent, String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent, 1, true),
            BorderFactory.createEmptyBorder(18, 16, 18, 16)));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 32));
        valLbl.setForeground(accent);

        JLabel lblLbl = new JLabel(label, SwingConstants.CENTER);
        lblLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblLbl.setForeground(TEXT_SEC);

        card.add(valLbl, BorderLayout.CENTER);
        card.add(lblLbl, BorderLayout.SOUTH);
        parent.add(card);
        return valLbl;
    }

    void drawPieChart(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Long> counts = students.stream()
            .collect(Collectors.groupingBy(s -> s.course, Collectors.counting()));
        if (counts.isEmpty()) return;

        Color[] clrs = {ACCENT_BLUE, ACCENT_GREEN, ACCENT_ORG, ACCENT_PURP,
                        new Color(0xF7D74F), new Color(0xFF6B9D)};

        int legendH = counts.size() * 18 + 6;
        int cx = w / 2;
        int cy = legendH + (h - legendH) / 2;
        int r = Math.min(cx - 10, (h - legendH) / 2 - 10);
        if (r < 20) return;

        double total = counts.values().stream().mapToLong(x -> x).sum();
        double start = -90;
        int ci = 0;

        for (Map.Entry<String, Long> e : counts.entrySet()) {
            double sweep = e.getValue() / total * 360.0;
            g2.setColor(clrs[ci % clrs.length]);
            g2.fillArc(cx - r, cy - r, 2 * r, 2 * r, (int) start, (int) sweep);
            g2.setColor(BG_DARK);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(cx - r, cy - r, 2 * r, 2 * r, (int) start, (int) sweep);
            start += sweep;
            ci++;
        }

        // legend at top
        ci = 0;
        int ly = 6;
        for (Map.Entry<String, Long> e : counts.entrySet()) {
            g2.setColor(clrs[ci % clrs.length]);
            g2.fillRoundRect(6, ly, 10, 10, 3, 3);
            g2.setColor(TEXT_SEC);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.drawString(e.getKey() + " (" + e.getValue() + ")", 22, ly + 9);
            ly += 18;
            ci++;
        }
    }

    JPanel buildTopStudents() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel title = new JLabel("Top Students by GPA");
        title.setFont(new Font("Monospaced", Font.BOLD, 13));
        title.setForeground(TEXT_PRI);
        panel.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setBackground(BG_CARD);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        Color[] rankColors = {new Color(0xFFD700), new Color(0xC0C0C0), new Color(0xCD7F32)};
        List<Student> sorted = students.stream()
            .sorted((a, b) -> Double.compare(b.gpa(), a.gpa()))
            .limit(6)
            .collect(Collectors.toList());

        int rank = 1;
        for (Student s : sorted) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(BG_CARD);
            row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            Color rc = rank <= 3 ? rankColors[rank - 1] : TEXT_SEC;
            JLabel nameLbl = new JLabel(rank + ".  " + s.name);
            nameLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
            nameLbl.setForeground(TEXT_PRI);

            JLabel gpaLbl = new JLabel(String.format("%.2f GPA", s.gpa()));
            gpaLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            gpaLbl.setForeground(rc);

            row.add(nameLbl, BorderLayout.WEST);
            row.add(gpaLbl, BorderLayout.EAST);
            list.add(row);
            rank++;
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    JPanel wrapWithTitle(String title, JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBackground(BG_CARD);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Monospaced", Font.BOLD, 13));
        t.setForeground(TEXT_PRI);
        wrap.add(t, BorderLayout.NORTH);
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    // ── Students Tab ───────────────────────────────────────────────────────────
    JPanel buildStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(BG_DARK);

        JTextField search = darkField(18);
        toolbar.add(darkLabel("Search:"));
        toolbar.add(search);
        toolbar.add(accentBtn("+ Add",      ACCENT_GREEN, e -> showStudentDialog(null)));
        toolbar.add(accentBtn("Edit",       ACCENT_BLUE,  e -> editStudent()));
        toolbar.add(accentBtn("Delete",     ACCENT_ORG,   e -> deleteStudent()));
        toolbar.add(accentBtn("Export CSV", ACCENT_PURP,  e -> exportStudents()));
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Age", "Gender", "Course", "GPA", "Attendance"};
        studentModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = darkTable(studentModel);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        studentTable.getColumnModel().getColumn(6).setCellRenderer(new GpaRenderer());
        studentTable.getColumnModel().getColumn(7).setCellRenderer(new AttRenderer());

        panel.add(darkScroll(studentTable), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { populateStudentTable(search.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { populateStudentTable(search.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { populateStudentTable(search.getText()); }
        });
        return panel;
    }

    void populateStudentTable(String q) {
        studentModel.setRowCount(0);
        String lq = q.toLowerCase();
        for (Student s : students) {
            if (q.isEmpty()
                || s.name.toLowerCase().contains(lq)
                || s.email.toLowerCase().contains(lq)
                || s.course.toLowerCase().contains(lq)) {
                studentModel.addRow(new Object[]{
                    s.id, s.name, s.email, s.age, s.gender, s.course,
                    String.format("%.2f", s.gpa()),
                    String.format("%.0f%%", s.attendancePct())
                });
            }
        }
    }

    void editStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) { warn("Please select a student to edit."); return; }
        int id = (int) studentModel.getValueAt(row, 0);
        students.stream().filter(s -> s.id == id).findFirst().ifPresent(this::showStudentDialog);
    }

    void showStudentDialog(Student existing) {
        boolean isNew = (existing == null);
        JDialog dlg = new JDialog(this, isNew ? "Add New Student" : "Edit Student", true);
        dlg.setSize(400, 370);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfName   = darkField(20);
        JTextField tfEmail  = darkField(20);
        JTextField tfAge    = darkField(20);
        JComboBox<String> cbGender = darkCombo(new String[]{"Male", "Female", "Other"});
        JComboBox<String> cbCourse = darkCombo(new String[]{
            "Computer Science", "Mathematics", "Physics",
            "Engineering", "Chemistry", "Biology", "Arts"});

        if (!isNew) {
            tfName.setText(existing.name);
            tfEmail.setText(existing.email);
            tfAge.setText(String.valueOf(existing.age));
            cbGender.setSelectedItem(existing.gender);
            cbCourse.setSelectedItem(existing.course);
        }

        String[] labels = {"Name:", "Email:", "Age:", "Gender:", "Course:"};
        JComponent[] fields = {tfName, tfEmail, tfAge, cbGender, cbCourse};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            p.add(darkLabel(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.7;
            p.add(fields[i], gc);
        }

        JButton saveBtn = accentBtn(isNew ? "Add Student" : "Save Changes", ACCENT_GREEN, e -> {
            try {
                String name   = tfName.getText().trim();
                String email  = tfEmail.getText().trim();
                int    age    = Integer.parseInt(tfAge.getText().trim());
                String gender = (String) cbGender.getSelectedItem();
                String course = (String) cbCourse.getSelectedItem();

                if (name.isEmpty())           throw new Exception("Name is required.");
                if (email.isEmpty())          throw new Exception("Email is required.");
                if (age < 1 || age > 120)     throw new Exception("Enter a valid age (1-120).");

                if (isNew) {
                    students.add(new Student(name, email, age, gender, course));
                } else {
                    existing.name = name; existing.email = email;
                    existing.age = age; existing.gender = gender; existing.course = course;
                }
                refreshAll();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Age must be a number.",
                    "Validation", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(),
                    "Validation", JOptionPane.ERROR_MESSAGE);
            }
        });

        gc.gridx = 0; gc.gridy = labels.length; gc.gridwidth = 2; gc.weightx = 1;
        p.add(saveBtn, gc);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) { warn("Please select a student to delete."); return; }
        String name = (String) studentModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + name + "\"? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) studentModel.getValueAt(row, 0);
            students.removeIf(s -> s.id == id);
            refreshAll();
        }
    }

    void exportStudents() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("students_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                pw.println("ID,Name,Email,Age,Gender,Course,GPA,Attendance%");
                for (Student s : students)
                    pw.printf("%d,\"%s\",\"%s\",%d,%s,\"%s\",%.2f,%.1f%n",
                        s.id, s.name, s.email, s.age, s.gender, s.course,
                        s.gpa(), s.attendancePct());
                JOptionPane.showMessageDialog(this, "Exported to: " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                warn("Export failed: " + ex.getMessage());
            }
        }
    }

    // ── Grades Tab ─────────────────────────────────────────────────────────────
    JPanel buildGradesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(BG_DARK);

        gradeStudentCombo = darkCombo(new String[]{});
        toolbar.add(darkLabel("Student:"));
        toolbar.add(gradeStudentCombo);
        toolbar.add(accentBtn("+ Add Grade",  ACCENT_GREEN, e -> addGradeForSelected()));
        toolbar.add(accentBtn("Delete Grade", ACCENT_ORG,   e -> deleteGradeForSelected()));
        toolbar.add(accentBtn("Export CSV",   ACCENT_PURP,  e -> exportGrades()));
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Subject", "Score", "Letter Grade", "Status"};
        gradeModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        gradeTable = darkTable(gradeModel);
        gradeTable.getColumnModel().getColumn(1).setCellRenderer(new ScoreBarRenderer());
        gradeTable.getColumnModel().getColumn(2).setCellRenderer(new LetterRenderer());
        gradeTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        panel.add(darkScroll(gradeTable), BorderLayout.CENTER);
        gradeStudentCombo.addActionListener(e -> refreshGradeTable());
        return panel;
    }

    void addGradeForSelected() {
        Student st = getSelectedStudentFrom(gradeStudentCombo);
        if (st == null) return;

        JDialog dlg = new JDialog(this, "Add Grade — " + st.name, true);
        dlg.setSize(320, 210);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> subj = darkCombo(new String[]{
            "Math", "Science", "English", "History", "Art",
            "Programming", "Physics", "Chemistry"});
        JTextField scoreField = darkField(10);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.4; p.add(darkLabel("Subject:"), gc);
        gc.gridx = 1; gc.weightx = 0.6; p.add(subj, gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0.4; p.add(darkLabel("Score (0-100):"), gc);
        gc.gridx = 1; gc.weightx = 0.6; p.add(scoreField, gc);
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2; gc.weightx = 1;

        p.add(accentBtn("Add Grade", ACCENT_GREEN, e -> {
            try {
                double sc = Double.parseDouble(scoreField.getText().trim());
                if (sc < 0 || sc > 100) throw new Exception("Score must be 0–100.");
                st.grades.add(new Grade((String) subj.getSelectedItem(), sc));
                refreshGradeTable();
                refreshDashboard();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid number.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage());
            }
        }), gc);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    void deleteGradeForSelected() {
        Student st = getSelectedStudentFrom(gradeStudentCombo);
        if (st == null) return;
        int row = gradeTable.getSelectedRow();
        if (row < 0) { warn("Select a grade row to delete."); return; }
        st.grades.remove(row);
        refreshGradeTable();
        refreshDashboard();
    }

    void refreshGradeTable() {
        gradeModel.setRowCount(0);
        Student st = getSelectedStudentFrom(gradeStudentCombo);
        if (st == null) return;
        for (Grade g : st.grades)
            gradeModel.addRow(new Object[]{
                g.subject,
                String.format("%.1f", g.score),
                g.letter,
                g.score >= 40 ? "Passing" : "Failing"
            });
    }

    void exportGrades() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("grades_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                pw.println("Student,Subject,Score,Grade,Status");
                for (Student s : students)
                    for (Grade g : s.grades)
                        pw.printf("\"%s\",\"%s\",%.1f,%s,%s%n",
                            s.name, g.subject, g.score, g.letter,
                            g.score >= 40 ? "Passing" : "Failing");
                JOptionPane.showMessageDialog(this, "Grades exported successfully.");
            } catch (Exception ex) { warn("Export failed: " + ex.getMessage()); }
        }
    }

    // ── Attendance Tab ─────────────────────────────────────────────────────────
    JPanel buildAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(BG_DARK);

        attStudentCombo = darkCombo(new String[]{});
        toolbar.add(darkLabel("Student:"));
        toolbar.add(attStudentCombo);
        toolbar.add(accentBtn("+ Mark Attendance", ACCENT_GREEN, e -> markAttendance()));
        toolbar.add(accentBtn("Delete Record",      ACCENT_ORG,   e -> deleteAttendance()));
        toolbar.add(accentBtn("Export CSV",         ACCENT_PURP,  e -> exportAttendance()));
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Date", "Status", "Mark"};
        attModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        attTable = darkTable(attModel);
        attTable.getColumnModel().getColumn(1).setCellRenderer(new StatusRenderer());
        attTable.getColumnModel().getColumn(2).setCellRenderer(new AttIconRenderer());

        panel.add(darkScroll(attTable), BorderLayout.CENTER);
        attStudentCombo.addActionListener(e -> refreshAttTable());
        return panel;
    }

    void markAttendance() {
        Student st = getSelectedStudentFrom(attStudentCombo);
        if (st == null) return;

        String date = (String) JOptionPane.showInputDialog(this,
            "Enter date (YYYY-MM-DD):", "Mark Attendance",
            JOptionPane.PLAIN_MESSAGE, null, null,
            new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        if (date == null || date.trim().isEmpty()) return;

        Object[] options = {"Present", "Absent"};
        int choice = JOptionPane.showOptionDialog(this,
            "Mark attendance for " + st.name + " on " + date.trim(),
            "Mark Attendance", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice < 0) return;

        st.attendance.add(new AttRecord(date.trim(), choice == 0));
        refreshAttTable();
        refreshDashboard();
    }

    void deleteAttendance() {
        Student st = getSelectedStudentFrom(attStudentCombo);
        if (st == null) return;
        int row = attTable.getSelectedRow();
        if (row < 0) { warn("Select a record to delete."); return; }
        st.attendance.remove(row);
        refreshAttTable();
        refreshDashboard();
    }

    void refreshAttTable() {
        attModel.setRowCount(0);
        Student st = getSelectedStudentFrom(attStudentCombo);
        if (st == null) return;
        for (AttRecord a : st.attendance)
            attModel.addRow(new Object[]{
                a.date,
                a.present ? "Present" : "Absent",
                a.present ? "P" : "A"
            });
    }

    void exportAttendance() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("attendance_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                pw.println("Student,Date,Status");
                for (Student s : students)
                    for (AttRecord a : s.attendance)
                        pw.printf("\"%s\",%s,%s%n", s.name, a.date, a.present ? "Present" : "Absent");
                JOptionPane.showMessageDialog(this, "Attendance exported successfully.");
            } catch (Exception ex) { warn("Export failed: " + ex.getMessage()); }
        }
    }

    // ── Refresh ────────────────────────────────────────────────────────────────
    void refreshAll() {
        populateStudentTable("");
        refreshDashboard();
        syncCombos();
        refreshGradeTable();
        refreshAttTable();
    }

    void syncCombos() {
        // Preserve selection indices
        int gi = Math.max(0, gradeStudentCombo.getSelectedIndex());
        int ai = Math.max(0, attStudentCombo.getSelectedIndex());

        // Remove old listeners temporarily
        ActionListener[] gal = gradeStudentCombo.getActionListeners();
        ActionListener[] aal = attStudentCombo.getActionListeners();
        for (ActionListener l : gal) gradeStudentCombo.removeActionListener(l);
        for (ActionListener l : aal) attStudentCombo.removeActionListener(l);

        gradeStudentCombo.removeAllItems();
        attStudentCombo.removeAllItems();
        for (Student s : students) {
            gradeStudentCombo.addItem(s.name);
            attStudentCombo.addItem(s.name);
        }
        if (!students.isEmpty()) {
            gradeStudentCombo.setSelectedIndex(Math.min(gi, students.size() - 1));
            attStudentCombo.setSelectedIndex(Math.min(ai, students.size() - 1));
        }

        // Restore listeners
        for (ActionListener l : gal) gradeStudentCombo.addActionListener(l);
        for (ActionListener l : aal) attStudentCombo.addActionListener(l);
    }

    void refreshDashboard() {
        if (students.isEmpty()) {
            statTotal.setText("0");
            statAvgGpa.setText("0.00");
            statAvgAtt.setText("0%");
            statPassing.setText("0%");
        } else {
            double avgGpa  = students.stream().mapToDouble(Student::gpa).average().orElse(0);
            double avgAtt  = students.stream().mapToDouble(Student::attendancePct).average().orElse(0);
            double passing = students.stream().filter(s -> s.gpa() >= 2.0).count() * 100.0 / students.size();
            statTotal.setText(String.valueOf(students.size()));
            statAvgGpa.setText(String.format("%.2f", avgGpa));
            statAvgAtt.setText(String.format("%.0f%%", avgAtt));
            statPassing.setText(String.format("%.0f%%", passing));
        }
        if (chartPanel != null) chartPanel.repaint();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    Student getSelectedStudentFrom(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx < 0 || idx >= students.size()) {
            if (!students.isEmpty()) warn("No student selected.");
            return null;
        }
        return students.get(idx);
    }

    void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── UI Factories ───────────────────────────────────────────────────────────
    JTextField darkField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRI);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    JLabel darkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SEC);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return l;
    }

    JComboBox<String> darkCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRI);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return cb;
    }

    JButton accentBtn(String text, Color color, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }

    JTable darkTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRI);
        t.setGridColor(BORDER_CLR);
        t.setSelectionBackground(new Color(0x2B4BAD));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(34);
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.getTableHeader().setBackground(BG_INPUT);
        t.getTableHeader().setForeground(TEXT_SEC);
        t.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        return t;
    }

    JScrollPane darkScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER_CLR, 1, true));
        return sp;
    }

    // ── Cell Renderers ─────────────────────────────────────────────────────────
    class GpaRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            try {
                double gpa = Double.parseDouble(v.toString());
                setForeground(gpa >= 3.5 ? ACCENT_GREEN : gpa >= 2.0 ? ACCENT_ORG : new Color(0xFF5C5C));
            } catch (Exception e) { setForeground(TEXT_PRI); }
            return this;
        }
    }

    class AttRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            try {
                double pct = Double.parseDouble(v.toString().replace("%", ""));
                setForeground(pct >= 80 ? ACCENT_GREEN : pct >= 60 ? ACCENT_ORG : new Color(0xFF5C5C));
            } catch (Exception e) { setForeground(TEXT_PRI); }
            return this;
        }
    }

    class ScoreBarRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            try {
                double sc = Double.parseDouble(v.toString());
                setForeground(sc >= 75 ? ACCENT_GREEN : sc >= 40 ? ACCENT_ORG : new Color(0xFF5C5C));
            } catch (Exception e) { setForeground(TEXT_PRI); }
            return this;
        }
    }

    class LetterRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Monospaced", Font.BOLD, 15));
            String letter = v == null ? "" : v.toString();
            if (letter.startsWith("A")) setForeground(ACCENT_GREEN);
            else if (letter.startsWith("B")) setForeground(ACCENT_BLUE);
            else if (letter.startsWith("C") || letter.startsWith("D")) setForeground(ACCENT_ORG);
            else setForeground(new Color(0xFF5C5C));
            return this;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            boolean positive = "Passing".equals(v) || "Present".equals(v);
            setForeground(positive ? ACCENT_GREEN : new Color(0xFF5C5C));
            return this;
        }
    }

    class AttIconRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBackground(sel ? new Color(0x2B4BAD) : BG_CARD);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Monospaced", Font.BOLD, 15));
            setForeground("P".equals(v) ? ACCENT_GREEN : new Color(0xFF5C5C));
            return this;
        }
    }

    // ── Entry Point ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(StudentManagementSystem::new);
    }
}
